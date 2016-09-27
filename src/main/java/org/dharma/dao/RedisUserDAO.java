package org.dharma.dao;

import java.util.Map;

import javax.annotation.PostConstruct;

import org.dharma.ApplicationConstants;
import org.dharma.dao.RedisEventDAO.EventIdKeyGenerator;
import org.dharma.exception.UserException;
import org.dharma.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.ZSetOperations;

public class RedisUserDAO implements UserDAO {
	
	private static HashOperations<String, String, String> opsForHash;
	private static ZSetOperations<String, String> opsForZSet;
	private static ValueOperations<String, String> opsForValue;
	
	static class UserIdKeyGenerator {
		public static String generateUserId() {
			return ApplicationConstants.USER_ID + ":" + opsForValue.increment(ApplicationConstants.USER_ID, ApplicationConstants.USER_ID_DELTA);
		}
		public static String generateEventId(String userIndex) {
			return ApplicationConstants.USER_ID + ":" + userIndex;
		}
	}
	
	@Autowired
	RedisTemplate<String, String> redisTemplate;
	
	@PostConstruct
	public void init() {
		opsForHash = redisTemplate.opsForHash();
		opsForZSet = redisTemplate.opsForZSet();
		opsForValue = redisTemplate.opsForValue();
	}
	
	private void saveScore(String userKey) {
		opsForZSet.add(ApplicationConstants.USER_SCORE_INDEX, userKey, Double.valueOf(opsForValue.get(ApplicationConstants.USER_ID)));
	}
	
	private void deleteScore(String userKey) {
		opsForZSet.remove(ApplicationConstants.USER_SCORE_INDEX, userKey);
	}
	
	private String persist(String userKey, String createdBy, User user) {
		opsForHash.put(userKey, ApplicationConstants.USER_EMAIL, user.getEmail());
		opsForHash.put(userKey, ApplicationConstants.CREATED_BY, createdBy);
		opsForHash.put(userKey, ApplicationConstants.USER_FIRST_NAME, user.getFirstName());
		opsForHash.put(userKey, ApplicationConstants.USER_ID, userKey);
		opsForHash.put(userKey, ApplicationConstants.USER_LAST_NAME, user.getLastName());
		opsForHash.put(userKey, ApplicationConstants.USER_PASSWORD, user.getPassword());
		if(user.getPhone() != null) {
			opsForHash.put(userKey, ApplicationConstants.USER_PHONE, user.getPhone());
		}
		
		return userKey;
	}

	@Override
	public String save(User user, String createdBy) {
		String userKey = UserIdKeyGenerator.generateUserId();
		persist(userKey, createdBy, user);
		saveScore(userKey);
		return userKey;
	}

	@Override
	public User get(String userId) throws UserException {
		String userKey = userId;
		if(!userKey.startsWith(ApplicationConstants.USER_ID)) {
			userKey = UserIdKeyGenerator.generateEventId(userId);
		}
		Map<String, String> valueMap = opsForHash.entries(userKey);
		if(valueMap.size() == 0) {
			throw new UserException(ApplicationConstants.USER_NOT_FOUND_EXCEPTION);
		}
		return User.builder()
			.createdBy(valueMap.get(ApplicationConstants.CREATED_BY))
			.email(valueMap.get(ApplicationConstants.USER_EMAIL))
			.firstName(valueMap.get(ApplicationConstants.USER_FIRST_NAME))
			.lastName(valueMap.get(ApplicationConstants.USER_LAST_NAME))
			.password(valueMap.get(ApplicationConstants.USER_PASSWORD))
			.phone(valueMap.get(ApplicationConstants.USER_PHONE))
			.id(valueMap.get(ApplicationConstants.USER_ID))
			.build();
	}

	@Override
	public void delete(String userId) {
		String userKey = userId;
		if(!userId.startsWith(ApplicationConstants.USER_ID)) {
			userKey = UserIdKeyGenerator.generateEventId(userId);
		}
		deleteScore(userKey);
		redisTemplate.delete(userKey);
	}

	@Override
	public String update(String userId, User user) {
		String userKey = userId;
		if(!userId.startsWith(ApplicationConstants.USER_ID)) {
			userKey = UserIdKeyGenerator.generateEventId(userId);
		}
		return persist(userKey, user.getCreatedBy(), user);
	}

}
