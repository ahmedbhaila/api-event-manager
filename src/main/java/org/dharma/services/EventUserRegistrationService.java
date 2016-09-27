package org.dharma.services;

import java.util.Set;

import org.dharma.dao.RegistrationDao;
import org.dharma.exception.EventException;
import org.dharma.exception.RegisterException;
import org.dharma.exception.UserException;
import org.dharma.model.Registration;
import org.springframework.beans.factory.annotation.Autowired;

public class EventUserRegistrationService {
	@Autowired
	RegistrationDao regDao;

	@Autowired
	UserRegistrationService userRegService;

	@Autowired
	EventService eventService;

	public String registerUserWithEvent(Registration reg, String createdBy) throws UserException, EventException {
		try {
			userRegService.getUser(reg.getUserId());
			eventService.getEvent(reg.getEventId());
		} catch (UserException ue) {
			throw ue;
		} catch (EventException ee) {
			throw ee;
		}
		return regDao.save(reg, createdBy);
	}

	public void delete(String registrationId) throws Exception {
		regDao.delete(registrationId);
	}

	public void deleteEventsWithUser(String userId) throws Exception {
		getEventsWithUser(userId).stream().forEach(reg -> {
			try {
				regDao.delete(reg);
			} catch (Exception e) {
			}
		});
	}

	public Set<String> getEventsWithUser(String userId) {
		return regDao.loadByUser(userId);
	}

	public Registration getRegistration(String regId) throws RegisterException {
		return regDao.load(regId);
	}
}
