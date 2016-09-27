package org.dharma.controller;

import java.util.Set;

import javax.validation.Valid;

import org.dharma.exception.EventException;
import org.dharma.exception.RegisterException;
import org.dharma.exception.UserException;
import org.dharma.model.Registration;
import org.dharma.services.RegistrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1")
public class RegistrationController {
	@Autowired
	RegistrationService service;
	
	@PostMapping("/register")
	@ResponseStatus(code=HttpStatus.CREATED)
	public String register(@Valid @RequestBody Registration reg) throws Exception {
		return service.registerUserWithEvent(reg, SecurityContextHolder.getContext().getAuthentication().getName());
	}
	
	@GetMapping("/register/{register_id}")
	@ResponseStatus(code=HttpStatus.FOUND)
	public Registration getRegistration(@PathVariable("register_id") String registerationId) throws Exception {
		return service.getRegistration(registerationId);
	}
	
	@DeleteMapping("/register/{register_id}")
	@ResponseStatus(code = HttpStatus.NO_CONTENT)
	public void delete(@PathVariable("register_id") String regId) throws Exception {
		service.delete(regId);
	}
	
	@GetMapping("/register/total")
	@ResponseStatus(code=HttpStatus.OK)
	public Long getTotalRegistrations()  {
		return service.getRegistrations();
	}
	
	@GetMapping("/register/user/{user_id}")
	public Set<Registration> getRegistrationsByUser(@PathVariable("user_id") String userId) throws Exception {
		return service.getRegistrationsWithUser(userId);
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
