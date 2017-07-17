package br.com.b3.digsta.dbs.server;

import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import br.com.b3.digsta.dbs.concurrent.ContinuousProcessUnit;
import br.com.b3.digsta.dbs.network.ServerInfo;
import br.com.b3.digsta.dbs.network.ServerRegisterControl;
import br.com.b3.digsta.dbs.server.request.commands.CommandPool;
import br.com.b3.digsta.dbs.server.utils.NetworkUtils;

@Component
public class ApplicationBoot {

	private static final Logger LOG = LogManager.getLogger(ApplicationBoot.class);

	@Autowired
	private ApplicationContext context;
	
	public <T> void start() {

		final ServerRegisterControl registerControl = context.getBean(ServerRegisterControl.class);
		
		final ServerInfo info = new ServerInfo(UUID.randomUUID().toString(), NetworkUtils.getHostname());
		LOG.info("Servidor " + info + " iniciando...");
		ContinuousProcessUnit<Boolean> continuousProcessUnit = new ContinuousProcessUnit<Boolean>("InformAlive", 1000, true) {
			@Override
			public void doIteration() {
				registerControl.informIAmAlive(info);
			}
		};
		continuousProcessUnit.processAssynchronous();
		
		LOG.info("Servidor " + info + " iniciado com sucesso!");
		
		
		CommandPool commandPool = null;
		int tries = 0;
		while (commandPool == null && tries < 5) {
			try {
				commandPool = context.getBean(CommandPool.class, "commandPool" + registerControl.whoAmI().getId(), 100, false);						
			} catch (Exception e) {
				sleep(1000);
			}
			tries++;
		}
		if (commandPool == null) {
			throw new IllegalArgumentException("Erro ao carregar pool de comandos do servidor");
		}
		commandPool.processAssynchronous();
	}

	private void sleep(long mili) {
		try {
			Thread.sleep(mili);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
}
