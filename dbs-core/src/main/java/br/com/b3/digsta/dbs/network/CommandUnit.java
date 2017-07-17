package br.com.b3.digsta.dbs.network;

import java.io.Serializable;

/**
 * Representa uma unidade de comando sendo processado em um dos servidores DBS.

 * @author Fernando R Galha.
 * 
 */
public class CommandUnit implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String commandId;
	private String commandName;
	private String response;
	private CommandStatus status;
	private String error;

	public String getCommandId() {
		return commandId;
	}

	public void setCommandId(String commandId) {
		this.commandId = commandId;
	}

	public CommandStatus getStatus() {
		return status;
	}

	public void setStatus(CommandStatus status) {
		this.status = status;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

	public String getResponse() {
		return response;
	}

	public void setResponse(String response) {
		this.response = response;
	}

	public String getCommandName() {
		return commandName;
	}

	public void setCommandName(String commandName) {
		this.commandName = commandName;
	}

}
