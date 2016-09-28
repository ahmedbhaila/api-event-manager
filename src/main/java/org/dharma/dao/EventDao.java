package org.dharma.dao;

import java.util.List;
import java.util.Map;

import org.dharma.exception.EventException;
import org.dharma.model.Event;

public interface EventDao {
	public String save(Event event, String createdBy);
	public Event get(String eventId) throws EventException;
	public void delete(String eventId) throws EventException;
	public String update(String eventId, Event event) throws EventException;
	public List<Event> getAll(int startIndex, int pageSize, Map<String, String> searchCriteriaMap);
	public Long getTotalEvents();
}

