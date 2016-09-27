package org.dharma.dao;

import org.dharma.exception.UserException;
import org.dharma.model.User;

public interface UserDAO {
	public String save(User user, String createdBy);
	public User get(String userId) throws UserException;
	public void delete(String userId);
	public String update(String userId, User user);
//	public List<Event> getAll(int startIndex, int pageSize);
//	public List<Event> getByUser(String userId, int startIndex, int pageSize);
//	public List<Event> search(String filterString, int startIndex, int pageSize);
}
