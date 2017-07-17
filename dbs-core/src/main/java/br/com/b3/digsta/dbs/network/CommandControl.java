package br.com.b3.digsta.dbs.network;

import java.util.List;

public interface CommandControl {

	public CommandUnit run(String command, List<String> args);
	public CommandUnit getInfo(String id);
	public void verifyCommandsToProcess(String id);
	public CommandTemplate getCommandByName(String commandName);
}
