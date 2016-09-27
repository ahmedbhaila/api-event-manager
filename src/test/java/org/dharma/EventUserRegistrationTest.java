package org.dharma;

import org.dharma.dao.EventDAO;
import org.dharma.dao.RegistrationDao;
import org.dharma.dao.UserDAO;
import org.dharma.exception.RegisterException;
import org.dharma.model.Event;
import org.dharma.model.Registration;
import org.dharma.model.User;
import org.dharma.services.EventService;
import org.dharma.services.EventUserRegistrationService;
import org.dharma.services.UserRegistrationService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;


@RunWith(SpringRunner.class)
@SpringBootTest
public class EventUserRegistrationTest {
	private static String REGISTRATION_ID_PREFIX = "registrationId:";

	@Autowired
	UserRegistrationService userService;

	@Autowired
	EventService eventService;
	
	@Autowired
	EventUserRegistrationService eventUserRegService;

	@Test
	public void testSaveRegistration() throws Exception {

		// create a user
		String userId = userService.registerUser(User.builder().email("test@test.com").firstName("Ahmed").lastName("Bhaila")
				.password("password").phone("1-800-456-4567").build(), "test");

		// create an event
		String eventId = eventService.saveEvent(Event.builder().dateTime("09-12-2016 12:00:00")
				.description("This is a test event").geoLocation("124,124").isPublic("yes").location("Chicago, IL")
				.name("Test Event").photo("http://photourl").webUrl("http://www.test.com").build(), "test");

		// register user with an event
		Registration reg = new Registration();
		reg.setEventId(eventId);
		reg.setUserId(userId);
		String regId = eventUserRegService.registerUserWithEvent(reg, "test");

		Assert.assertTrue(regId.startsWith(REGISTRATION_ID_PREFIX));

		// do a GET to check for data inserted
		try {
			Registration loadedReg = eventUserRegService.getRegistration(regId);

			Assert.assertTrue(loadedReg.getCreatedBy().equals("test"));
			Assert.assertTrue(loadedReg.getEventId().equals(eventId));
			Assert.assertTrue(loadedReg.getUserId().equals(userId));

		} catch (Exception e) {
			System.err.println(e.getMessage());
		}

	}

	@Test(expected = RegisterException.class)
	public void testDeleteRegistration() throws Exception {

		// create a user
		String userId = userService.registerUser(User.builder().email("test@test.com").firstName("Ahmed").lastName("Bhaila")
				.password("password").phone("1-800-456-4567").build(), "test");

		// create an event
		String eventId = eventService.saveEvent(Event.builder().dateTime("09-12-2016 12:00:00")
				.description("This is a test event").geoLocation("124,124").isPublic("yes").location("Chicago, IL")
				.name("Test Event").photo("http://photourl").webUrl("http://www.test.com").build(), "test");

		// register user with an event
		Registration reg = new Registration();
		reg.setEventId(eventId);
		reg.setUserId(userId);
		String regId = eventUserRegService.registerUserWithEvent(reg, "test");

		// delete event
		eventUserRegService.delete(regId);

		// do a get on the deleted event
		Registration loadedReg = eventUserRegService.getRegistration(regId);

		Assert.assertNull(loadedReg);
	}
}
