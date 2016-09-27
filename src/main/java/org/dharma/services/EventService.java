package org.dharma.services;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.dharma.dao.EventDao;
import org.dharma.exception.EventException;
import org.dharma.model.Event;
import org.springframework.beans.factory.annotation.Autowired;

import lombok.extern.apachecommons.CommonsLog;

@CommonsLog
public class EventService {
	
	@Autowired
	EventDao eventDAO;
	
	public String saveEvent(Event e, String createdBy) {
		//e.setDateTime(String.valueOf(Instant.parse(e.getDateTime()).toEpochMilli()));
		return eventDAO.save(e, createdBy);
	}
	
	public String updateEvent(String eventId, Event e) throws EventException {
		//e.setDateTime(String.valueOf(Instant.parse(e.getDateTime()).toEpochMilli()));
		return eventDAO.update(eventId, e);
	}
	
	public void deleteEvent(String eventKey) throws EventException {
		eventDAO.delete(eventKey);
	}
	
	public Event getEvent(String eventKey) throws EventException {
		Event e = eventDAO.get(eventKey);
		//e.setDateTime(LocalDateTime.ofInstant(Instant.ofEpochMilli(Long.valueOf(e.getDateTime())), ZoneId.systemDefault()).toString());
		return e;
	}
	
	public List<Event> getEvents(int startIndex, int pageSize, String searchCriteria) {
		Map<String, String> searchCriteriaMap = null;
		if(searchCriteria != null && !searchCriteria.equals("")) {
			searchCriteriaMap = Pattern.compile(";")
			.splitAsStream(searchCriteria)
			.map(s -> s.split(":"))
			.collect(Collectors.toMap(e -> e[0], e -> e[1]));
		}
		log.debug("Searching for events with criteria " + searchCriteria);
		return eventDAO.getAll(startIndex, pageSize, searchCriteriaMap);
	}
	
	public Long getTotalEvents() {
		return eventDAO.getTotalEvents();
	}
}
