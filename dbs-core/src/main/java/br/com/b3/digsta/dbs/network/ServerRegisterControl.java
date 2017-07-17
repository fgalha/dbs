package br.com.b3.digsta.dbs.network;

import java.util.List;

import br.com.b3.digsta.dbs.concurrent.ProcessUnit;

public interface ServerRegisterControl {

	public void informIAmAlive(ServerInfo serverInfo);
	public ServerInfo whoAmI();
	public List<ServerInfo> getClustersServers();
	public void registerProcess(String processId, ProcessUnit<String> process);
	public ProcessUnit<String> getProcessUnit(String processId);
	public void finishProcess(String commandId);
	
}
