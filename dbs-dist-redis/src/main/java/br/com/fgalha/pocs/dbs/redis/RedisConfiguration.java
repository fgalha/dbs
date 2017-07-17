package br.com.fgalha.pocs.dbs.redis;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.Properties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;

import redis.clients.jedis.JedisPoolConfig;

@Configuration
public class RedisConfiguration {

	@Autowired
	private ApplicationContext context;
	
	@Bean
	public JedisPoolConfig jedisPoolConfig() {
		JedisPoolConfig cfg = new JedisPoolConfig();
		cfg.setMaxTotal(100);
		cfg.setMaxIdle(40);
		cfg.setMinIdle(20);
		return cfg;
	}
	
	@Bean(name = "conn1")
	@Primary
	public JedisConnectionFactory jedisConnectionFactoryServer1() {
		JedisConnectionFactory jedisConnectionFactory = new JedisConnectionFactory();
		jedisConnectionFactory.setPoolConfig(jedisPoolConfig());
		jedisConnectionFactory.setHostName("apspos05801d");
	    return jedisConnectionFactory;
	}

	@Bean(name = "conn2")
	public JedisConnectionFactory jedisConnectionFactoryServer2() {
		JedisConnectionFactory jedisConnectionFactory = new JedisConnectionFactory();
		jedisConnectionFactory.setPoolConfig(jedisPoolConfig());
		jedisConnectionFactory.setHostName("apspos12401d.internalenv.corp");
	    return jedisConnectionFactory;
	}
	
	@Bean
	public JdkSerializationRedisSerializer jdkSerializationRedisSerializer() {
		return new JdkSerializationRedisSerializer();
	}
	 
	@Bean(name = "redisTemplateWrite")
	@Primary
	public RedisTemplate<Serializable, Serializable> redisTemplateMaster() {
		JedisConnectionFactory master = null;
		Map<String, JedisConnectionFactory> beansOfType = context.getBeansOfType(JedisConnectionFactory.class);
		for (JedisConnectionFactory connFact : beansOfType.values()) {
			RedisConnection connection = null;
			try {
				connection = connFact.getConnection();
				Properties info = connection.info();
				if (info.getProperty("role").equals("master")) {
					master = connFact;
					break;
				}				
			} finally {
				if (connection != null) connection.close();
			}
		}
		if (master == null) {
			throw new IllegalStateException("Impossivel encontrar conexao com o node master do REDIS");
		}
		
	    RedisTemplate<Serializable, Serializable> template = new RedisTemplate<Serializable, Serializable>();
	    template.setConnectionFactory(master);
	    template.setKeySerializer(jdkSerializationRedisSerializer());
	    template.setValueSerializer(jdkSerializationRedisSerializer());
	    template.setHashValueSerializer(jdkSerializationRedisSerializer());
	    template.setHashValueSerializer(jdkSerializationRedisSerializer());
	    template.setEnableTransactionSupport(true);
	    return template;
	}

	@Bean(name = "redisTemplateRead")
	public RedisTemplate<Serializable, Serializable> redisTemplateLocal() throws UnknownHostException {
		JedisConnectionFactory slave = null;
		Map<String, JedisConnectionFactory> beansOfType = context.getBeansOfType(JedisConnectionFactory.class);
		
		// primeiro tenta encontrar uma conexao que seja do mesmo hostname
		for (JedisConnectionFactory connFact : beansOfType.values()) {
			if (connFact.getHostName().contains(InetAddress.getLocalHost().getHostName())) {
				slave = connFact;
				break;
			}
		}

		if (slave == null) {
			// senao tenta encontrar um slave
			for (JedisConnectionFactory connFact : beansOfType.values()) {
				RedisConnection connection = null;
				try {
					connection = connFact.getConnection();
					Properties info = connection.info();
					if (info.getProperty("role").equals("slave")) {
						slave = connFact;
						break;
					}				
				} finally {
					if (connection != null) connection.close();
				}
			}
		}
		
		if (slave == null) {
			throw new IllegalStateException("Impossivel encontrar conexao com o node slave do REDIS");
		}
	    RedisTemplate<Serializable, Serializable> template = new RedisTemplate<Serializable, Serializable>();
	    template.setConnectionFactory(slave);
	    template.setKeySerializer(jdkSerializationRedisSerializer());
	    template.setValueSerializer(jdkSerializationRedisSerializer());
	    template.setHashValueSerializer(jdkSerializationRedisSerializer());
	    template.setHashValueSerializer(jdkSerializationRedisSerializer());
	    return template;
	}

}
