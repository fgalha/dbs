package br.com.b3.digsta.dbs.redis.server;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import br.com.b3.digsta.dbs.concurrent.ProcessUnit;
import br.com.b3.digsta.dbs.network.ServerInfo;
import br.com.b3.digsta.dbs.network.ServerRegisterControl;

@Component
//@Lazy
public class ServerRegisterControlRedisImpl implements ServerRegisterControl {

	private static final org.apache.logging.log4j.Logger LOG = LogManager.getLogger(ServerRegisterControlRedisImpl.class);
	
	private static final String REDIS_KEY_SERVER_INFO = "serverInfo";

	private ServerInfo me;

	@Value("${expireServerTimeout:30000}")
	private long expireServerTimeout;
	
	@Autowired
	@Qualifier("redisTemplateWrite")
	private RedisTemplate<Serializable, Serializable> writeTemplate;

	@Autowired
	@Qualifier("redisTemplateRead")
	private RedisTemplate<Serializable, Serializable> readTemplate;

	private final ConcurrentHashMap<String, ProcessUnit<String>> processControlMap = new ConcurrentHashMap<String, ProcessUnit<String>>();
	
	public void informIAmAlive(ServerInfo serverInfo) {
		serverInfo.setLastInformAliveTimestamp(System.currentTimeMillis());
		writeTemplate.opsForHash().put(REDIS_KEY_SERVER_INFO, serverInfo.getId(), serverInfo);
		me = serverInfo;
		verifyAndRemoveDeadServers();
	}

	public ServerInfo whoAmI() {
		return me;
	}

	public List<ServerInfo> getClustersServers() {
		List<Object> values = readTemplate.opsForHash().values(REDIS_KEY_SERVER_INFO);
		List<ServerInfo> list = new ArrayList<ServerInfo>();
		for (Object object : values) {
			list.add((ServerInfo) object);	
		}
		return list;
	}

	private void verifyAndRemoveDeadServers() {
		Set<Object> keys = readTemplate.opsForHash().keys(REDIS_KEY_SERVER_INFO);
		for (Object object : keys) {
			Object obj = readTemplate.opsForHash().get(REDIS_KEY_SERVER_INFO, object);
			ServerInfo info = (ServerInfo) obj;
			if (info.getLastInformAliveTimestamp() + expireServerTimeout < System.currentTimeMillis()) {
				LOG.warn("Atencao, o servidor " + info.getId() + " - hostname: " + info.getHostname() + " sera removido da lista de servidores ativos.");
				writeTemplate.opsForHash().delete(REDIS_KEY_SERVER_INFO, info.getId());
			}
			
		}
	}

	@Override
	public void registerProcess(String processId, ProcessUnit<String> process) {
		process.processAssynchronous();
		processControlMap.put(processId, process);
	}

	public ProcessUnit<String> getProcessUnit(String processId) {
		return processControlMap.get(processId);
	}

	@Override
	public void finishProcess(String commandId) {
		processControlMap.remove(commandId);
	}
	
	
}
