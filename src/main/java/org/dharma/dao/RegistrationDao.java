package org.dharma.dao;

import java.util.Set;

import org.dharma.exception.RegisterException;
import org.dharma.model.Registration;

public interface RegistrationDao {
	public String save(Registration registration, String createdBy);
	public void delete(String registrationId) throws RegisterException;
	public Registration load(String registrationId) throws RegisterException;
	public Set<String> loadByUser(String userId);
	public Long getTotalRegistrations();
}
