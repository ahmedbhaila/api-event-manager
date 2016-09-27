package org.dharma.controller;

import javax.validation.Valid;

import org.dharma.exception.EventException;
import org.dharma.exception.RegisterException;
import org.dharma.exception.UserException;
import org.dharma.model.User;
import org.dharma.services.UserService;
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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1")
public class UserController {

	@Autowired
	UserService userService;

	@PostMapping("/user")
	@ResponseStatus(code = HttpStatus.CREATED)
	public String registerUser(@Valid @RequestBody User user) {
		return userService.registerUser(user, SecurityContextHolder.getContext().getAuthentication().getName());
	}

	@PutMapping("/user/{user_id}")
	@ResponseStatus(code = HttpStatus.NO_CONTENT)
	public String updateUser(@PathVariable("user_id") String userId, @RequestBody User user) {
		return userService.updateUser(userId, user);
	}

	@DeleteMapping("/user/{user_id}")
	@ResponseStatus(code = HttpStatus.NOT_FOUND)
	public void deleteUser(@PathVariable("user_id") String userId) throws Exception {
		userService.deleteUser(userId);
	}

	@GetMapping("/user/{user_id}")
	@ResponseStatus(code = HttpStatus.FOUND)
	public User getUser(@PathVariable("user_id") String userId) throws UserException {
		return userService.getUser(userId);
	}
	
	@GetMapping("/users/total")
	@ResponseStatus(code = HttpStatus.OK)
	public Long getTotalUser() throws Exception {
		return userService.getTotalUsers();
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
