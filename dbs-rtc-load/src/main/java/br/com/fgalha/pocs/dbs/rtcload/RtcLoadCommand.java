package br.com.fgalha.pocs.dbs.rtcload;

import java.util.List;
import java.util.Random;

import javax.annotation.PostConstruct;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import br.com.fgalha.pocs.dbs.network.CommandTemplate;
import br.com.fgalha.pocs.dbs.network.ServerRegisterControl;

@Service("--rtc-load")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class RtcLoadCommand implements CommandTemplate {

	private static final Logger LOG = LogManager.getLogger(RtcLoadCommand.class);
	
	private int retCode;
	private String result = "";
	
	@Autowired
	private ApplicationContext context;
	
	@PostConstruct
	public void post() {
		System.out.println("Post...");
	}
	
	public int execute(List<String> args) {
		try {
			ServerRegisterControl serverRegisterControl = context.getBean(ServerRegisterControl.class);
			LOG.info("Iniciando comando " + getCommandName() + " no servidor "
					+ serverRegisterControl.whoAmI().getId() + "-"
					+ serverRegisterControl.whoAmI().getHostname());
			try {
				Thread.sleep(new Random().nextInt(10000) + 1000);
			} catch (InterruptedException e) {
				return retCode = 1;
			}
			LOG.info("Sucesso  comando " + getCommandName() + " no servidor "
					+ serverRegisterControl.whoAmI().getId() + "-"
					+ serverRegisterControl.whoAmI().getHostname());
			
			result = "Executado com sucesso no servidor: " + serverRegisterControl.whoAmI().getId();
			
			return retCode = 0;
			
		} catch (Exception e) {
			LOG.error("Erro ao executar comando " + getCommandName(), e);
			return retCode = 2;
		}
	}

	public void showHelp() {
		// TODO Auto-generated method stub
	}

	public String getCommandName() {
		return "--rtc-load";
	}

	public int getMaxSimultaneous() {
		return 1;
	}

	@Override
	public int getReturnCode() {
		return retCode;
	}

	@Override
	public String getResult() {
		return result;
	}

	@Override
	public boolean isAssynchronous() {
		return true;
	}

	@Override
	public boolean isAConsumerWaitStrategy() {
		return false;
	}

	@Override
	public boolean isProducersFinished() {
		return false;
	}

}
