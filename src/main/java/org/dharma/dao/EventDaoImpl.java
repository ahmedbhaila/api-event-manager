package org.dharma.dao;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;

import org.dharma.ApplicationConstants;
import org.dharma.exception.EventException;
import org.dharma.model.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.ZSetOperations;

import lombok.extern.apachecommons.CommonsLog;


@CommonsLog
public class EventDaoImpl implements EventDao {
	
	private static HashOperations<String, String, String> opsForHash;
	private static ZSetOperations<String, String> opsForZSet;
	private static SetOperations<String, String> opsForSet;
	private static ValueOperations<String, String> opsForValue;
	
	static class EventIdKeyGenerator {
		public static String generateEventId() {
			return ApplicationConstants.EVENT_ID + ":" + opsForValue.increment(ApplicationConstants.EVENT_ID, ApplicationConstants.EVENT_ID_DELTA);
		}
		public static String generateEventId(String eventIndex) {
			return ApplicationConstants.EVENT_ID + ":" + eventIndex;
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

	@Override
	public String save(Event event, String createdBy) {
		log.debug("Saving event");
		String eventKey = EventIdKeyGenerator.generateEventId();
		persist(eventKey, createdBy, event);
		saveIndexes(eventKey, event);
		log.debug("Event created with key " + eventKey);
		return eventKey;
	}
	
	private void saveIndexes(String eventKey, Event event) {
		log.debug("Creating search indexes for event " + eventKey);
		opsForZSet.add(ApplicationConstants.EVENT_SCORE_INDEX, eventKey, Double.valueOf(opsForValue.get(ApplicationConstants.EVENT_ID)));
		//opsForZSet.add(ApplicationConstants.EVENT_DATE_TIME_INDEX, eventKey, Long.valueOf(event.getDateTime()));
		opsForSet.add(ApplicationConstants.EVENT_GEOLOCATION_INDEX + event.getGeoLocation(), eventKey);
		opsForSet.add(ApplicationConstants.EVENT_IS_PUBLIC_INDEX + event.getIsPublic().toLowerCase().trim(), eventKey);
		opsForSet.add(ApplicationConstants.EVENT_LOCATION_INDEX + event.getLocation().toLowerCase().trim(), eventKey);
		opsForSet.add(ApplicationConstants.EVENT_NAME_INDEX + event.getName(), eventKey);
	}
	
	private void deleteIndexes(Event event) {
		log.debug("Deleting search indexes for event " + event.getId());
		String eventKey = EventIdKeyGenerator.generateEventId(event.getId());
		opsForSet.remove(ApplicationConstants.EVENT_GEOLOCATION_INDEX + event.getGeoLocation(), eventKey);
		opsForSet.remove(ApplicationConstants.EVENT_IS_PUBLIC_INDEX + event.getIsPublic(), eventKey);
		opsForSet.remove(ApplicationConstants.EVENT_LOCATION_INDEX + event.getLocation(), eventKey);
		opsForSet.remove(ApplicationConstants.EVENT_NAME_INDEX + event.getName(), eventKey);
	}
	
	private String persist(String eventKey, String createdBy, Event event) {
		opsForHash.put(eventKey, ApplicationConstants.EVENT_NAME, event.getName());
		opsForHash.put(eventKey, ApplicationConstants.CREATED_BY, createdBy);
		opsForHash.put(eventKey, ApplicationConstants.EVENT_DATE_TIME, event.getDateTime());
		opsForHash.put(eventKey, ApplicationConstants.EVENT_DESC, event.getDescription());
		if(event.getGeoLocation() != null) {
			opsForHash.put(eventKey, ApplicationConstants.EVENT_GEO_LOCATION, event.getGeoLocation().trim());
		}
		opsForHash.put(eventKey, ApplicationConstants.EVENT_LOCATION, event.getLocation().toLowerCase().trim());
		if(event.getPhoto() != null) {
			opsForHash.put(eventKey, ApplicationConstants.EVENT_PHOTO,event.getPhoto());
		}
		opsForHash.put(eventKey, ApplicationConstants.EVENT_PUBLIC_FLAG,event.getIsPublic().toLowerCase());
		if(event.getWebUrl() != null) {
			opsForHash.put(eventKey, ApplicationConstants.EVENT_URL,event.getWebUrl());
		}
		opsForHash.put(eventKey, ApplicationConstants.EVENT_ID, eventKey);
		return eventKey;
	}

	@Override
	public Event get(String eventId) throws EventException {
		String eventKey = eventId;
		if(!eventId.startsWith(ApplicationConstants.EVENT_ID)) {
			eventKey = EventIdKeyGenerator.generateEventId(eventId);
		}
		Map<String, String> valueMap = opsForHash.entries(eventKey);
		if(valueMap.size() == 0) {
			log.debug("Event could not be found with eventId " + eventKey);
			throw new EventException(ApplicationConstants.EVENT_NOT_FOUND_EXCEPTION);
		}
		return Event.builder()
			.createdby(valueMap.get(ApplicationConstants.CREATED_BY))
			.dateTime(valueMap.get(ApplicationConstants.EVENT_DATE_TIME))
			.description(valueMap.get(ApplicationConstants.EVENT_DESC))
			.geoLocation(valueMap.get(ApplicationConstants.EVENT_GEO_LOCATION))
			.id(eventId)
			.isPublic(valueMap.get(ApplicationConstants.EVENT_PUBLIC_FLAG))
			.location(valueMap.get(ApplicationConstants.EVENT_LOCATION))
			.name(valueMap.get(ApplicationConstants.EVENT_NAME))
			.photo(valueMap.get(ApplicationConstants.EVENT_PHOTO))
			.webUrl(valueMap.get(ApplicationConstants.EVENT_URL))
			.build();
	}
	
	private void deleteScore(String eventKey) {
		log.debug("Deleting Event Index with eventKey " + eventKey);
		opsForZSet.remove(ApplicationConstants.EVENT_SCORE_INDEX, eventKey);
	}

	@Override
	public void delete(String eventId) throws EventException {
		String eventKey = eventId;
		if(!eventId.startsWith(ApplicationConstants.EVENT_ID)) {
			eventKey = EventIdKeyGenerator.generateEventId(eventId);
		}
		log.debug("Deleting Event with evenKey " + eventKey);
		deleteIndexes(this.get(eventId));
		deleteScore(eventKey);
		redisTemplate.delete(eventKey);
	}

	@Override
	public String update(String eventId, Event event) throws EventException {
		String eventKey = EventIdKeyGenerator.generateEventId(eventId);
		log.debug("Updating event with evenKey " + eventKey);
		deleteIndexes(get(eventKey));
		String key = persist(eventKey, event.getCreatedby(), event);
		saveIndexes(key, event);
		return key;
	}
	
	private List<Event> getEvents(Stream<String> eventKeys) {
		return eventKeys.map(e -> opsForHash.entries(e)).map(valueMap -> Event.builder()
				.createdby(valueMap.get(ApplicationConstants.CREATED_BY))
				.dateTime(valueMap.get(ApplicationConstants.EVENT_DATE_TIME))
				.description(valueMap.get(ApplicationConstants.EVENT_DESC))
				.geoLocation(valueMap.get(ApplicationConstants.EVENT_GEO_LOCATION))
				.isPublic(valueMap.get(ApplicationConstants.EVENT_PUBLIC_FLAG))
				.location(valueMap.get(ApplicationConstants.EVENT_LOCATION))
				.name(valueMap.get(ApplicationConstants.EVENT_NAME))
				.photo(valueMap.get(ApplicationConstants.EVENT_PHOTO))
				.webUrl(valueMap.get(ApplicationConstants.EVENT_URL))
				.id(valueMap.get(ApplicationConstants.EVENT_ID))
				.build()
			)
			.collect(Collectors.toList());
	}

	@Override
	public List<Event> getAll(int startIndex, int pageSize, Map<String, String> searchCriteria) {
		int start = startIndex - 1;
		int end = startIndex + pageSize - 2;
		
		if(searchCriteria != null) {
			Set<String> intersection = opsForSet.intersect(null, searchCriteria.keySet()
					.stream().map(e -> "event:" + e + ":" + searchCriteria.get(e))
					.collect(Collectors.toList()));
			if(start == 0 && end == -1) {
				return getEvents(intersection.stream());
			}
			else {
				// determine total size of our stream
				end = end >= intersection.size() ? intersection.size(): end + 1;
				return getEvents(intersection.stream().collect(Collectors.toList()).subList(start, end).stream());
			}
		}
		else {
			return getEvents(opsForZSet.range(ApplicationConstants.EVENT_SCORE_INDEX, start, end).stream());
		}
	}

	@Override
	public Long getTotalEvents() {
		return opsForZSet.zCard(ApplicationConstants.EVENT_SCORE_INDEX);
	}
}
