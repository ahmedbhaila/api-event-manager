package org.dharma.services;

import java.util.Arrays;
import java.util.Collection;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public class RedisUserDetailsService implements UserDetailsService {
	
	@Autowired
	RedisTemplate<String, String> redisTemplate;
	
	private static ValueOperations<String, String> opsForValue;
	private static final String BAD_CREDENTIALS = "Incorrect Username/Password";
	
	@PostConstruct
	public void init() {
		opsForValue = redisTemplate.opsForValue();
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		Collection<SimpleGrantedAuthority> authorities;
//	    if (username.equals(adminUsername)) {
//	      authorities = Arrays.asList(new SimpleGrantedAuthority("ROLE_ADMIN"));
//	      password = adminPassword;
//	    } else {
	      authorities = Arrays.asList(new SimpleGrantedAuthority("ROLE_USER"));
	      String password = opsForValue.get(username);
	      if(password == null) {
	    	  throw new UsernameNotFoundException(BAD_CREDENTIALS);
	      }
	    //}
	    UserDetails userDetails = new User(username, password, true, true, true, true, authorities);
	    return userDetails;
	}

}
