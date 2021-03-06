package com.neverwinterdp.server.gateway;

import java.util.List;
import java.util.Map;

import com.neverwinterdp.server.command.ServerCommand;
import com.neverwinterdp.server.command.ServerCommandResult;
import com.neverwinterdp.server.command.ServerModuleCommands;
import com.neverwinterdp.server.module.ModuleRegistration;

public class Module extends Plugin {
  protected Object doCall(String commandName, CommandParams params) throws Exception {
    ServerCommandResult<?>[] results = null ;
    if("list".equals(commandName)) results = list(params) ;
    else if("install".equals(commandName)) results = install(params) ;
    else if("uninstall".equals(commandName)) results = uninstall(params) ;
    if(results != null) return results ;
    return "{ 'success': false, 'message': 'unknown command'}" ;
  }
  
  public ServerCommandResult<ModuleRegistration[]>[] list(CommandParams params) {
    MemberSelector memberSelector = new MemberSelector(params) ;
    String type = params.getString("type") ;
    return list(memberSelector, type) ;
  }
  
  public ServerCommandResult<ModuleRegistration[]>[] list(MemberSelector memberSelector, String type) {
    ServerCommand<ModuleRegistration[]> cmd = null ;
    if("installed".equals(type)) {
      cmd = new ServerModuleCommands.GetInstallModule() ;
    } else {
      cmd = new ServerModuleCommands.GetAvailableModule() ;
    }
    return memberSelector.execute(clusterClient, cmd) ;
  }
  
  public ServerCommandResult<ModuleRegistration[]>[] install(CommandParams params) {
    MemberSelector memberSelector = new MemberSelector(params) ;
    boolean autostart = params.getBoolean("autostart", false) ;
    List<String> modules = params.getStringList("module") ;
    Map<String, String> properties = params.getProperties() ;
    return install(memberSelector, modules, autostart, properties) ;
  }
  
  public ServerCommandResult<ModuleRegistration[]>[] install(MemberSelector memberSelector, List<String> modules, 
                                                             boolean autostart, Map<String, String> properties) {
    ServerCommand<ModuleRegistration[]> cmd = 
        new ServerModuleCommands.InstallModule(modules, autostart, properties) ;
    return memberSelector.execute(clusterClient, cmd) ;
  }
  
  public ServerCommandResult<ModuleRegistration[]>[] uninstall(CommandParams params) {
    MemberSelector memberSelector = new MemberSelector(params) ;
    List<String> modules = params.getStringList("module") ;
    return uninstall(memberSelector, modules) ;
  }
  
  public ServerCommandResult<ModuleRegistration[]>[] uninstall(MemberSelector memberSelector, List<String> modules) {
    ServerCommand<ModuleRegistration[]> cmd = new ServerModuleCommands.UninstallModule(modules) ;
    return memberSelector.execute(clusterClient, cmd) ;
  }
}