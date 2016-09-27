package org.dharma.dao;

import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.dharma.ApplicationConstants;
import org.dharma.dao.RedisUserDAO.UserIdKeyGenerator;
import org.dharma.exception.RegisterException;
import org.dharma.model.Registration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.ZSetOperations;

public class RedisRegistrationDao implements RegistrationDao {
	private static HashOperations<String, String, String> opsForHash;
	private static ZSetOperations<String, String> opsForZSet;
	private static ValueOperations<String, String> opsForValue;
	private static SetOperations<String, String> opsForSet;

	static class RegistrationIdKeyGenerator {
		public static String generateRegistrationId() {
			return ApplicationConstants.REGISTRATION_ID + ":"
					+ opsForValue.increment(ApplicationConstants.REGISTRATION_ID, ApplicationConstants.REGISTRATION_ID_DELTA);
		}
		public static String generateRegistrationId(String regIndex) {
			return ApplicationConstants.REGISTRATION_ID + ":" + regIndex;
		}
	}

	@Autowired
	RedisTemplate<String, String> redisTemplate;

	@PostConstruct
	public void init() {
		opsForHash = redisTemplate.opsForHash();
		opsForZSet = redisTemplate.opsForZSet();
		opsForValue = redisTemplate.opsForValue();
		opsForSet = redisTemplate.opsForSet();
	}

	private String persist(String registrationKey, String createdBy, Registration registration) {
		opsForHash.put(registrationKey, ApplicationConstants.EVENT_ID, registration.getEventId());
		opsForHash.put(registrationKey, ApplicationConstants.CREATED_BY, createdBy);
		opsForHash.put(registrationKey, ApplicationConstants.USER_ID, registration.getUserId());
		opsForHash.put(registrationKey, ApplicationConstants.REGISTRATION_ID, registrationKey);

		return registrationKey;
	}

	private void saveScore(String registrationKey) {
		opsForZSet.add(ApplicationConstants.EVENT_SCORE_INDEX, registrationKey,
				Double.valueOf(opsForValue.get(ApplicationConstants.EVENT_ID)));
	}
	
	private void saveUserRegScore(Registration registration) {
		opsForSet.add(ApplicationConstants.USER_REGISTRATION_INDEX + registration.getUserId(), registration.getId());
	}
	
	private void saveEventRegScore(Registration registration) {
		opsForSet.add(ApplicationConstants.EVENT_REGISTRATION_INDEX + registration.getEventId(), registration.getId());
	}

	@Override
	public String save(Registration registration, String createdBy) {
		String registrationKey = RegistrationIdKeyGenerator.generateRegistrationId();
		persist(registrationKey, createdBy, registration);
		registration.setId(registrationKey);
		saveScore(registrationKey);
		saveUserRegScore(registration);
		saveEventRegScore(registration);
		return registrationKey;
	}

	@Override
	public void delete(String registrationId) throws RegisterException {
		String regKey = null;
		if(!registrationId.startsWith(ApplicationConstants.REGISTRATION_ID)) {
			regKey = RegistrationIdKeyGenerator.generateRegistrationId(registrationId);
		}
		else {
			regKey = registrationId;
		}
		
		Registration reg = load(regKey);
		opsForZSet.remove(ApplicationConstants.EVENT_SCORE_INDEX, regKey);
		opsForSet.remove(ApplicationConstants.USER_REGISTRATION_INDEX + reg.getUserId(), regKey);
		opsForSet.remove(ApplicationConstants.EVENT_REGISTRATION_INDEX + reg.getEventId(), regKey);
		redisTemplate.delete(regKey);
	}

	@Override
	public Registration load(String registrationId) throws RegisterException {
		if(!registrationId.startsWith(ApplicationConstants.REGISTRATION_ID)) {
			registrationId = RegistrationIdKeyGenerator.generateRegistrationId(registrationId);
		}
		Map<String, String> valueMap = opsForHash.entries(registrationId);
		if(valueMap.size() == 0) {
			throw new RegisterException(ApplicationConstants.REGISTRATION_NOT_FOUND_EXCEPTION);
		}
		return Registration.builder()
			.createdBy(valueMap.get(ApplicationConstants.CREATED_BY))
			.eventId(valueMap.get(ApplicationConstants.EVENT_ID))
			.userId(valueMap.get(ApplicationConstants.USER_ID))
			.id(registrationId)
			.build();
	}

	@Override
	public Set<String> loadByUser(String userId) {
		return opsForSet.members(ApplicationConstants.USER_REGISTRATION_INDEX + UserIdKeyGenerator.generateEventId(userId));
	}
}
