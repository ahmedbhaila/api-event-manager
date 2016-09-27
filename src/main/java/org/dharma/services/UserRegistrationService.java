package org.dharma.services;

import org.dharma.dao.UserDAO;
import org.dharma.exception.UserException;
import org.dharma.model.User;
import org.springframework.beans.factory.annotation.Autowired;

public class UserRegistrationService {
	
	@Autowired
	UserDAO userDao;
	
	@Autowired
	EventUserRegistrationService regEventUserService;
	
	public String registerUser(User user, String createdBy) {
		return userDao.save(user, createdBy);
	}
	
	public String updateUser(String userId, User u) {
		return userDao.update(userId, u);
	}
	
	public void deleteUser(String userKey) throws Exception {
		// delete all events registrations associated with this user
		regEventUserService.deleteEventsWithUser(userKey);
		userDao.delete(userKey);
	}
	
	public User getUser(String userKey) throws UserException {
		return userDao.get(userKey);
	} 
}