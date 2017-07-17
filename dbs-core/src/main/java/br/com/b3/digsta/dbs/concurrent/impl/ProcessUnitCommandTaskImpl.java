package br.com.b3.digsta.dbs.concurrent.impl;

import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import br.com.b3.digsta.dbs.concurrent.ProcessUnit;
import br.com.b3.digsta.dbs.network.CommandTemplate;

@Component("processUnitCommandTaskImpl")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class ProcessUnitCommandTaskImpl extends ProcessUnit<String> {

	private CommandTemplate commandTemplate;
	private List<String> arguments;
	
	public ProcessUnitCommandTaskImpl(String threadName, CommandTemplate commandTemplate, List<String> arguments) {
		super(threadName);
		this.commandTemplate = commandTemplate;
		this.arguments = arguments;
	}

	@Override
	public String process() {
		commandTemplate.execute(arguments);
		return "Executing";
	}

	@Override
	public String getProcessReturn() {
		return commandTemplate.getResult();
	}
	
	public CommandTemplate getCommandTemplate() {
		return commandTemplate;
	}

	public List<String> getArguments() {
		return Collections.unmodifiableList(arguments);
	}
	
}
