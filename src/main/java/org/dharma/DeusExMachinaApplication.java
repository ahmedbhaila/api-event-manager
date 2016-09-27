package org.dharma;

import java.net.URI;

import javax.annotation.PostConstruct;

import org.dharma.dao.EventDAO;
import org.dharma.dao.RedisEventDAO;
import org.dharma.dao.RedisRegistrationDao;
import org.dharma.dao.RedisUserDAO;
import org.dharma.dao.RegistrationDao;
import org.dharma.dao.UserDAO;
import org.dharma.services.EventService;
import org.dharma.services.EventUserRegistrationService;
import org.dharma.services.RedisUserDetailsService;
import org.dharma.services.UserRegistrationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.security.core.userdetails.UserDetailsService;

@SpringBootApplication
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
	public EventDAO redisEventDAO() {
		return new RedisEventDAO();
	}

	@Bean
	public UserDAO redisUserDao() {
		return new RedisUserDAO();
	}

	@Bean
	public RegistrationDao registrationDao() {
		return new RedisRegistrationDao();
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
	public UserRegistrationService userRegistrationService() {
		return new UserRegistrationService();
	}

	@Bean
	public EventUserRegistrationService eventUserRegistrationService() {
		return new EventUserRegistrationService();
	}

	@PostConstruct
	public void init() throws Exception {
		// add admin user for spring security
		redisTemplate().opsForValue().set(adminUser, adminPassword);
	}

	public static void main(String[] args) {
		SpringApplication.run(DeusExMachinaApplication.class, args);
	}
}
