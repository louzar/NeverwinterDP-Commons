package com.neverwinterdp.hadoop.wordcount;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hdfs.HdfsConfiguration;
import org.apache.hadoop.hdfs.MiniDFSCluster;
import org.apache.hadoop.yarn.conf.YarnConfiguration;
import org.apache.hadoop.yarn.server.MiniYARNCluster;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class MiniClusterStartsWithCountJobTest {

  private Configuration   conf = new HdfsConfiguration(new YarnConfiguration());
  private MiniDFSCluster  dfsCluster;
  private MiniYARNCluster yarnCluster;

  @Before
  public void beforeTest() throws IOException {
    dfsCluster = new MiniDFSCluster.Builder(conf).build();
    dfsCluster.waitClusterUp();

    yarnCluster = new MiniYARNCluster(
        "MiniClusterStartsWithCountJobTest", // testName
        2, // number of node managers
        2, // number of local log dirs per node manager
        2  // number of hdfs dirs per node manager
    ); 
    yarnCluster.init(conf);
    yarnCluster.start();
  }

  @After
  public void afterTest() {
    dfsCluster.shutdown();
    yarnCluster.stop();
  }

  @Test
  public void testWithMiniCluster() throws Exception {
    FsHelper fsHelper = new FsHelper(dfsCluster.getConfiguration(0));
    Path input = new Path("/tests/wordcount/input/in.txt");
    Path output = new Path("/tests/wordcount/output/");
    fsHelper.writeStringToFile(input, "first line\nsecond line\nthird line");

    StartsWithCountJob underTest = new StartsWithCountJob();
    underTest.setConf(conf);

    int exitCode = underTest.run(new String[] { input.toString(), output.toString() });
    assertEquals("Returned error code.", 0, exitCode);

    FileSystem fs = dfsCluster.getFileSystem();
    assertTrue(fs.exists(new Path(output, "_SUCCESS")));
    String outputAsStr = fsHelper.readStringFroomFile(new Path(output, "part-r-00000"));
    Map<String, Integer> resAsMap = getResultAsMap(outputAsStr);

    assertEquals(4, resAsMap.size());
    assertEquals(1, resAsMap.get("f").intValue());
    assertEquals(1, resAsMap.get("s").intValue());
    assertEquals(1, resAsMap.get("t").intValue());
    assertEquals(3, resAsMap.get("l").intValue());
  }

  private Map<String, Integer> getResultAsMap(String outputAsStr) throws IOException {
    System.out.println("\n\n");
    System.out.println(" outputAsStr = " +  outputAsStr);
    System.out.println("\n\n");
    Map<String, Integer> result = new HashMap<String, Integer>();
    for (String line : outputAsStr.split("\n")) {
      String[] tokens = line.split("\t");
      result.put(tokens[0], Integer.parseInt(tokens[1]));
    }
    return result;
  }
}