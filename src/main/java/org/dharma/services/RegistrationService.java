package org.dharma.services;

import java.util.Set;
import java.util.stream.Collectors;

import org.dharma.dao.RegistrationDao;
import org.dharma.exception.EventException;
import org.dharma.exception.RegisterException;
import org.dharma.exception.UserException;
import org.dharma.model.Registration;
import org.springframework.beans.factory.annotation.Autowired;

import lombok.extern.apachecommons.CommonsLog;

@CommonsLog
public class RegistrationService {
	@Autowired
	RegistrationDao regDao;

	@Autowired
	UserService userRegService;

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
		log.debug("Deleting registration");
		regDao.delete(registrationId);
	}

	public void deleteEventsWithUser(String userId) throws Exception {
		getRegistrationIdsWithUser(userId).stream().forEach(reg -> {
			try {
				regDao.delete(reg);
			} catch (Exception e) {
				log.error(e.getMessage());
			}
		});
	}

	public Set<String> getRegistrationIdsWithUser(String userId) {
		return regDao.loadByUser(userId);
	}

	public Set<Registration> getRegistrationsWithUser(String userId) {
		return regDao.loadByUser(userId).stream().map(reg -> 
			
				{
					try {
						return regDao.load(reg);
					}
					catch(Exception e) {
						log.error(e.getMessage());
						return null;
					}
				}
			
		).collect(Collectors.toSet());
	}

	public Registration getRegistration(String regId) throws RegisterException {
		return regDao.load(regId);
	}

	public Long getRegistrations() {
		return regDao.getTotalRegistrations();
	}
}
