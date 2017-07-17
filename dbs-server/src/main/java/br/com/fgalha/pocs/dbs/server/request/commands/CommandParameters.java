package br.com.fgalha.pocs.dbs.server.request.commands;

import java.io.Serializable;
import java.util.List;

public class CommandParameters implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private String name;
	
	private List<String> arguments;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<String> getArguments() {
		return arguments;
	}

	public void setArguments(List<String> arguments) {
		this.arguments = arguments;
	}
	

}
