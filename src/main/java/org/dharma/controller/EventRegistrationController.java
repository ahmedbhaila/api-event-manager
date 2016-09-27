package org.dharma.controller;

import java.util.List;

import javax.validation.Valid;

import org.dharma.exception.EventException;
import org.dharma.exception.RegisterException;
import org.dharma.exception.UserException;
import org.dharma.model.Event;
import org.dharma.services.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1")
public class EventRegistrationController {

	@Autowired
	EventService eventService;

	@PostMapping(path = "/event")
	@ResponseStatus(code = HttpStatus.CREATED)
	public String create(@Valid @RequestBody Event event) {
		return eventService.saveEvent(event, SecurityContextHolder.getContext().getAuthentication().getName());
	}

	@PutMapping(path = "/event/{event_id}")
	@ResponseStatus(code = HttpStatus.NO_CONTENT)
	public void update(@PathVariable("event_id") String eventId, @RequestBody Event event) throws EventException {
		eventService.updateEvent(eventId, event);
	}

	@GetMapping(path = "/event/{event_id}")
	@ResponseStatus(code = HttpStatus.FOUND)
	public Event get(@PathVariable("event_id") String eventId) throws EventException {
		return eventService.getEvent(eventId);
	}

	@DeleteMapping(path = "/event/{event_id}")
	@ResponseStatus(code = HttpStatus.NO_CONTENT)
	public void delete(@PathVariable("event_id") String eventId) throws EventException {
		eventService.deleteEvent(eventId);
	}

	@GetMapping(path = "/events")
	@ResponseStatus(code = HttpStatus.FOUND)
	public List<Event> getEvents(@RequestParam(name = "startIndex", defaultValue = "1") Integer startIndex,
			@RequestParam(name = "pageSize", defaultValue = "0") Integer pageSize,
			@RequestParam(required = false, name = "searchCriteria") String searchCriteria) {
		return eventService.getEvents(startIndex, pageSize, searchCriteria);
	}

	@ExceptionHandler(UserException.class)
	@ResponseStatus(org.springframework.http.HttpStatus.NOT_FOUND)
	public String userNotFound() {
		return "Resource Not Found";
	}
	@ExceptionHandler(EventException.class)
	@ResponseStatus(org.springframework.http.HttpStatus.NOT_FOUND)
	public String eventNotFound() {
		return "Resource Not Found";
	}
	
	@ExceptionHandler(RegisterException.class)
	@ResponseStatus(org.springframework.http.HttpStatus.NOT_FOUND)
	public String registrationNotFound() {
		return "Resource Not Found";
	}
}
