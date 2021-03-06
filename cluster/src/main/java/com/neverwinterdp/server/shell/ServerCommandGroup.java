package com.neverwinterdp.server.shell;

import java.util.List;
import java.util.Map;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.ParametersDelegate;
import com.neverwinterdp.server.ServerRegistration;
import com.neverwinterdp.server.ServerState;
import com.neverwinterdp.server.command.ServerCommandResult;
import com.neverwinterdp.server.gateway.MemberSelector;
import com.neverwinterdp.server.service.ServiceRegistration;
import com.neverwinterdp.server.service.ServiceState;
import com.neverwinterdp.util.monitor.snapshot.ApplicationMonitorSnapshot;
import com.neverwinterdp.util.monitor.snapshot.MetricFormater;
import com.neverwinterdp.util.monitor.snapshot.TimerSnapshot;
import com.neverwinterdp.util.text.TabularPrinter;

@CommandGroupConfig(name = "server")
public class ServerCommandGroup extends CommandGroup {
  public ServerCommandGroup() {
    super("cluster") ;
    add("ping", Ping.class);
    add("shutdown", Shutdown.class);
    add("exit", Exit.class);
    add("registration", Registration.class);
    add("metric", Metric.class);
    add("metric-clear", MetricClear.class);
  }
  
  static public class Ping extends Command {
    @ParametersDelegate
    MemberSelector memberSelector = new MemberSelector();
    
    public void execute(ShellContext ctx) {
      printServerStateResults(ctx, ctx.getClusterGateway().server.ping(memberSelector)) ;
    }
  }
  
  @Parameters(commandDescription = "List the server registration and service registration of each member")
  static public class Registration extends Command {
    @Parameter(names = {"-f", "--filter"}, description = "filter expression to match host or ip")
    String filter;
    
    public void execute(ShellContext ctx) {
      ServerRegistration[] registration = ctx.getClusterGateway().getClusterRegistration().getServerRegistration() ;
      Console console = ctx.console() ;
      String indent = "  " ;
      for(ServerRegistration server : registration) {
        console.header("Member " + server.getClusterMember().toString());
        TabularPrinter iprinter = console.tabularPrinter(20, 30) ;
        iprinter.setIndent(indent) ;
        iprinter.row("UUID:", server.getClusterMember().getUuid());
        iprinter.row("Host:", server.getClusterMember().getHost());
        iprinter.row("IP Address:", server.getClusterMember().getIpAddress());
        iprinter.row("Port:", server.getClusterMember().getPort());
        iprinter.row("State", server.getServerState());
        iprinter.row("Roles", server.getRoles());
        List<ServiceRegistration> services = server.getServices() ;
        TabularPrinter sprinter = console.tabularPrinter(20, 30, 10) ;
        sprinter.setIndent(indent);
        console.println();
        console.println(indent, "Service Registration");
        sprinter.header("Module", "Service", "State");
        for(ServiceRegistration service : services) {
          String module = service.getModule() ;
          String serviceId = service.getServiceId() ;
          ServiceState state = service.getState() ;
          sprinter.row(module, serviceId, state);
        }
      }
    }
  }
  
  static public class Shutdown extends Command {
    @ParametersDelegate
    MemberSelector memberSelector = new MemberSelector();
    
    public void execute(ShellContext ctx) {
      printServerStateResults(ctx, ctx.getClusterGateway().server.shutdown(memberSelector)) ;
    }
  }

  static public class Exit extends Command {
    @Parameter(names = {"-t","--wait-time"}, description = "Wait time before shutdown")
    long waitTime = 3000 ;
    
    @ParametersDelegate
    MemberSelector memberSelector = new MemberSelector();
    
    public void execute(ShellContext ctx) {
      printServerStateResults(ctx, ctx.getClusterGateway().server.exit(memberSelector)) ;
    }
  }
  
  static public class Metric extends Command {
    @ParametersDelegate
    MemberSelector memberSelector = new MemberSelector();
    
    @Parameter(names = {"-f","--filter"}, description = "filter by regrex syntax")
    String filter = "*" ;
    
    
    public void execute(ShellContext ctx) {
      ServerCommandResult<ApplicationMonitorSnapshot>[] results = 
          ctx.getClusterGateway().server.metric(memberSelector, filter) ;
      MetricFormater formater = new MetricFormater("  ") ;
      for(ServerCommandResult<ApplicationMonitorSnapshot> sel : results) {
        ApplicationMonitorSnapshot snapshot = sel.getResult() ;
        ctx.console().header(sel.getFromMember().toString() + " - member name " + sel.getFromMember().getMemberName());
        Map<String, TimerSnapshot> timers = snapshot.getRegistry().getTimers() ;
        ctx.console().println(formater.format(timers));
      }
    }
  }
  
  static public class MetricClear extends Command {
    @ParametersDelegate
    MemberSelector memberSelector = new MemberSelector();
    
    @Parameter(names = {"--name-exp"}, description = "filter by regrex syntax")
    String nameExp = "*" ;
    
    public void execute(ShellContext ctx) {
      ServerCommandResult<Integer>[] results = 
          ctx.getClusterGateway().server.clearMetric(memberSelector, nameExp) ;
      for(ServerCommandResult<Integer> sel : results) {
        Integer match = sel.getResult() ;
        ctx.console().println("Clear " + match + " on " + sel.getFromMember()) ;
      }
    }
  }
  
  static void printServerStateResults(ShellContext ctx, ServerCommandResult<ServerState>[] results) {
    TabularPrinter tprinter = ctx.console().tabularPrinter(30, 10, 10) ;
    tprinter.header("Host IP", "Port", "State");
    for(int i = 0; i < results.length; i++) {
      ServerCommandResult<ServerState> sel = results[i] ;
      String host = sel.getFromMember().getIpAddress() ;
      int    port = sel.getFromMember().getPort() ;
      ServerState state = sel.getResult() ;
      tprinter.row(host, port, state);
    }
    
    for(int i = 0; i < results.length; i++) {
      ServerCommandResult<ServerState> sel = results[i] ;
      if(sel.hasError()) {
        ctx.console().println(sel.getError());
      }
    }
  }
}