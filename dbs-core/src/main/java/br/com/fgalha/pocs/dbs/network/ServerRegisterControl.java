package br.com.fgalha.pocs.dbs.network;

import java.util.List;

import br.com.fgalha.pocs.dbs.concurrent.ProcessUnit;

public interface ServerRegisterControl {

	public void informIAmAlive(ServerInfo serverInfo);
	public ServerInfo whoAmI();
	public List<ServerInfo> getClustersServers();
	public void registerProcess(String processId, ProcessUnit<String> process);
	public ProcessUnit<String> getProcessUnit(String processId);
	public void finishProcess(String commandId);
	
}
