package com.neverwinterdp.server.command;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

import com.neverwinterdp.server.cluster.ClusterMember;

/**
 * @author Tuan Nguyen
 * @email  tuan08@gmail.com
 */
public class ServiceCommandResult <T> {
  private T result ;
  private String error ;
  private ClusterMember member ;
  
  public T getResult() {
    return result;
  }

  public void setResult(T result) {
    this.result = result;
  }
  
  public boolean hasError() { return error != null ; }
  
  public String getError() {
    return error;
  }
  
  public void setError(String error) {
    this.error = error ;
  }
  
  public void setError(Exception error) {
    StringWriter swriter = new StringWriter() ;
    error.printStackTrace(new PrintWriter(swriter));
    this.error = swriter.toString();
  }

  public ClusterMember getFromMember() {
    return member;
  }

  public void setFromMember(ClusterMember member) {
    this.member = member;
  }
}
