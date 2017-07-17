package br.com.fgalha.pocs.dbs.redis.server;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import br.com.fgalha.pocs.dbs.concurrent.ProcessUnit;
import br.com.fgalha.pocs.dbs.concurrent.ProcessUnitStatus;
import br.com.fgalha.pocs.dbs.network.CommandControl;
import br.com.fgalha.pocs.dbs.network.CommandStatus;
import br.com.fgalha.pocs.dbs.network.CommandTemplate;
import br.com.fgalha.pocs.dbs.network.CommandUnit;
import br.com.fgalha.pocs.dbs.network.ServerInfo;
import br.com.fgalha.pocs.dbs.network.ServerRegisterControl;

@Component
public class CommandControlRedisImpl implements CommandControl {

	private static final Logger LOG = LogManager.getLogger(CommandControlRedisImpl.class);
	
	public static final String REDIS_KEY_COMMANDS = "commands";

	@Autowired
	private ApplicationContext context;

	@Autowired
	@Qualifier("redisTemplateWrite")
	private RedisTemplate<Serializable, Serializable> writeTemplate;

	@Autowired
	@Qualifier("redisTemplateRead")
	private RedisTemplate<Serializable, Serializable> readTemplate;

	@Autowired
	private ServerRegisterControl serverRegisterControl;

	public CommandUnit run(String commandName, List<String> args) {
		CommandTemplate commandTemplate = null;
		try {
			commandTemplate = getCommandByName(commandName);			
		} catch (Exception e) {
			LOG.error("Erro ao encontrar comando " + commandName, e);
			return errorCommand("Comando nao encontrado. Verifique se o comando " + commandName + " esta plugado no servidor DBS.");
		}
		if (!commandTemplate.isAssynchronous()) {
			return processCommandSynchronous(commandTemplate, args);
		}
		
		// TODO implementar controle de numero de comandos simultaneos

		String id = UUID.randomUUID().toString();

		CommandUnit commandUnit = new CommandUnit();
		commandUnit.setCommandId(id);
		commandUnit.setCommandName(commandName);
		commandUnit.setStatus(CommandStatus.REGISTERED);

		List<ServerInfo> clustersServers = serverRegisterControl.getClustersServers();

		writeTemplate.multi();
		for (ServerInfo serverInfo : clustersServers) {
			writeTemplate.opsForList().rightPush("commands:" + serverInfo.getId(), commandName + ":" + id);
			writeTemplate.opsForHash().put("executing", serverInfo.getId() + ":" + commandName + ":" + id, commandUnit);
		}
		writeTemplate.opsForHash().put("commands", id, commandName);
		writeTemplate.exec();
		return commandUnit;
	}

	private CommandUnit processCommandSynchronous(CommandTemplate commandTemplate, List<String> args) {
		try {
			int execute = commandTemplate.execute(args);
			String id = UUID.randomUUID().toString();
			CommandUnit commandUnit = new CommandUnit();
			commandUnit.setCommandId(id);
			commandUnit.setCommandName(commandTemplate.getCommandName());
			commandUnit.setStatus(execute == 0 ? CommandStatus.SUCCESS : CommandStatus.ERROR);
			commandUnit.setResponse(commandTemplate.getResult());
			return commandUnit;
		} catch (Exception e) {
			LOG.error("Erro ao processar comando " + commandTemplate.getCommandName());
			return errorCommand("Erro ao processar comando " + commandTemplate.getCommandName());
		}
	}

	public CommandUnit getInfo(String id) {
		String command = (String) readTemplate.opsForHash().get("commands", id);
		if (command == null) {
			return errorCommand("Comando " + id + " nao encontrado");
		}
		List<ServerInfo> clustersServers = serverRegisterControl.getClustersServers();
		boolean ok = true;
		String result = "";
		for (ServerInfo serverInfo : clustersServers) {
			CommandUnit c = (CommandUnit) readTemplate.opsForHash()
					.get("executing", serverInfo.getId() + ":" + command + ":" + id);
			if (c != null) {
				if (c.getStatus().equals(CommandStatus.ERROR)) {
					return c;
				}
				if (!c.getStatus().equals(CommandStatus.SUCCESS)) {
					ok = false;
				} else {
					result += c.getResponse() + "\t";
				}
			}
		}
		if (ok) {
			return successCommand(id, result);
		}

		return null;
	}

	public void verifyCommandsToProcess(String serverId) {
		String c = (String) writeTemplate.opsForList().leftPop("commands:" + serverId);
		if (c != null) {
			String[] split = c.split(":");
			String commandName = split[0];
			String commandId = split[1];
			CommandTemplate command = (CommandTemplate) context.getBean(split[0]);
			CommandUnit unit = (CommandUnit) readTemplate.opsForHash().get("executing", serverId + ":" + commandName + ":" + commandId);
			
			if (unit.getStatus().equals(CommandStatus.REGISTERED)) {
				unit.setStatus(CommandStatus.RUNNING);
				writeTemplate.opsForHash().put("executing", serverId + ":" + commandName + ":" + commandId, unit);
				processCommandAssynchronous(unit.getCommandId(), command);			
			
			} else if (unit.getStatus().equals(CommandStatus.RUNNING)) {
				ProcessUnit<String> processUnit = serverRegisterControl.getProcessUnit(unit.getCommandId());
				if (processUnit != null) {
					if (processUnit.getStatus().equals(ProcessUnitStatus.SUCCESS)) {
						unit.setStatus(CommandStatus.SUCCESS);
						unit.setResponse(processUnit.getProcessReturn());
						writeTemplate.opsForHash().put("executing", serverId + ":" + commandName + ":" + commandId, unit);
						finishCommand(unit.getCommandId());
					} else if (processUnit.getStatus().equals(ProcessUnitStatus.ERROR)) {
						unit.setStatus(CommandStatus.ERROR);
						unit.setResponse(processUnit.getProcessReturn());
						writeTemplate.opsForHash().put("executing", serverId + ":" + commandName + ":" + commandId, unit);
						finishCommand(unit.getCommandId());
					}
				}
			}
		}
	}

	private void finishCommand(String commandId) {
		serverRegisterControl.finishProcess(commandId);		
	}

	@SuppressWarnings("unchecked")
	private void processCommandAssynchronous(String processId, CommandTemplate command) {
		ProcessUnit<String> process = (ProcessUnit<String>) context.getBean("processUnitCommandTaskImpl");
		serverRegisterControl.registerProcess(processId, process);
	}

	private CommandUnit successCommand(String id, String result) {
		CommandUnit c = new CommandUnit();
		c.setCommandId(id);
		c.setResponse(result);
		c.setStatus(CommandStatus.SUCCESS);
		return c;
	}

	private CommandUnit errorCommand(String error) {
		CommandUnit c = new CommandUnit();
		c.setCommandId("");
		c.setError(error);
		c.setStatus(CommandStatus.ERROR);
		return c;
	}

	@Override
	public CommandTemplate getCommandByName(String commandName) {
		CommandTemplate command = null;
		try {
			command = (CommandTemplate) context.getBean(commandName);
		} catch (Exception e) {
			LOG.debug("Aviso, command " + command + " nao encontrado diretamente pelo nome: " + command);
		}
		if (command != null) {
			return command;
		}
		Map<String, CommandTemplate> map = context.getBeansOfType(CommandTemplate.class);
		for (CommandTemplate comm : map.values()) {
			if (comm.getCommandName().equals(commandName)) {
				return comm;
			}
		}
		throw new IllegalArgumentException("Comando " + commandName + " nao encontrado");
	}
}
