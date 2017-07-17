package br.com.fgalha.pocs.dbs.server.request.commands;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import br.com.fgalha.pocs.dbs.concurrent.ContinuousProcessUnit;
import br.com.fgalha.pocs.dbs.network.CommandControl;
import br.com.fgalha.pocs.dbs.network.ServerRegisterControl;

@Service
@Lazy
public class CommandPool extends ContinuousProcessUnit<Boolean> {

	@Autowired
	private ApplicationContext context;
	
	public CommandPool(String threadName, long interval, boolean stopOnError) {
		super(threadName, interval, stopOnError);
	}

	@Override
	public void doIteration() {
		ServerRegisterControl serverRegisterControl = context.getBean(ServerRegisterControl.class);
		CommandControl commandControl = context.getBean(CommandControl.class);
		commandControl.verifyCommandsToProcess(serverRegisterControl.whoAmI().getId());
	}

}
