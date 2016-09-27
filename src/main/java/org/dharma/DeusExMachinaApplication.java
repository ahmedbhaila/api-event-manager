package org.dharma;

import java.net.URI;

import javax.annotation.PostConstruct;

import org.dharma.dao.EventDao;
import org.dharma.dao.EventDaoImpl;
import org.dharma.dao.RegistrationDaoImpl;
import org.dharma.dao.UserDaoImpl;
import org.dharma.dao.RegistrationDao;
import org.dharma.dao.UserDao;
import org.dharma.services.EventService;
import org.dharma.services.RegistrationService;
import org.dharma.services.RedisUserDetailsService;
import org.dharma.services.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.security.core.userdetails.UserDetailsService;

import lombok.extern.apachecommons.CommonsLog;

@SpringBootApplication
@CommonsLog
public class DeusExMachinaApplication {

	@Value("${http.security.admin.user}")
	String adminUser;

	@Value("${http.security.admin.password}")
	String adminPassword;
	

	@Bean
	public JedisConnectionFactory jedisConnectionFactory() throws Exception {
		URI redisUri = new URI(System.getenv("REDISCLOUD_URL"));
		JedisConnectionFactory jedisFactory = new JedisConnectionFactory();
		jedisFactory.setHostName(redisUri.getHost());
		jedisFactory.setPort(redisUri.getPort());
		//jedisFactory.setDatabase(2);
		jedisFactory.setPassword(redisUri.getUserInfo().split(":", 2)[1]);
		return jedisFactory;
	}

	@Bean
	public StringRedisSerializer stringSerializer() {
		return new StringRedisSerializer();
	}

	@Bean
	public RedisTemplate<String, String> redisTemplate() throws Exception {
		RedisTemplate<String, String> redisTemplate = new RedisTemplate<String, String>();
		redisTemplate.setConnectionFactory(jedisConnectionFactory());
		redisTemplate.setKeySerializer(stringSerializer());
		redisTemplate.setHashKeySerializer(stringSerializer());
		redisTemplate.setHashValueSerializer(stringSerializer());
		redisTemplate.setValueSerializer(stringSerializer());
		return redisTemplate;
	}

	@Bean
	public EventDao redisEventDAO() {
		return new EventDaoImpl();
	}

	@Bean
	public UserDao redisUserDao() {
		return new UserDaoImpl();
	}

	@Bean
	public RegistrationDao registrationDao() {
		return new RegistrationDaoImpl();
	}

	@Bean
	public EventService eventService() {
		return new EventService();
	}

	@Bean
	public UserDetailsService userDetailsService() {
		return new RedisUserDetailsService();
	}

	@Bean
	public UserService userRegistrationService() {
		return new UserService();
	}

	@Bean
	public RegistrationService eventUserRegistrationService() {
		return new RegistrationService();
	}

	@PostConstruct
	public void init() throws Exception {
		// add admin user for spring security
		log.debug("Adding default admin user");
		redisTemplate().opsForValue().set(adminUser, adminPassword);
	}

	public static void main(String[] args) {
		SpringApplication.run(DeusExMachinaApplication.class, args);
	}
}
