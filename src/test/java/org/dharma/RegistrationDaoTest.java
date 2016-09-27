package org.dharma;

import org.dharma.dao.EventDao;
import org.dharma.dao.RegistrationDao;
import org.dharma.dao.UserDao;
import org.dharma.exception.RegisterException;
import org.dharma.exception.UserException;
import org.dharma.model.Event;
import org.dharma.model.Registration;
import org.dharma.model.User;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class RegistrationDaoTest {

	private static String REGISTRATION_ID_PREFIX = "registrationId:";

	@Autowired
	UserDao userDao;

	@Autowired
	EventDao eventDao;

	@Autowired
	RegistrationDao regDao;

	@Test
	public void testSaveRegistration() {

		// create a user
		String userId = userDao.save(User.builder().email("test@test.com").firstName("Ahmed").lastName("Bhaila")
				.password("password").phone("1-800-456-4567").build(), "test");

		// create an event
		String eventId = eventDao.save(Event.builder().dateTime("09-12-2016 12:00:00")
				.description("This is a test event").geoLocation("124,124").isPublic("yes").location("Chicago, IL")
				.name("Test Event").photo("http://photourl").webUrl("http://www.test.com").build(), "test");

		// register user with an event
		Registration reg = new Registration();
		reg.setEventId(eventId);
		reg.setUserId(userId);
		String regId = regDao.save(reg, "test");

		Assert.assertTrue(regId.startsWith(REGISTRATION_ID_PREFIX));

		// do a GET to check for data inserted
		try {
			Registration loadedReg = regDao.load(regId);

			Assert.assertTrue(loadedReg.getCreatedBy().equals("test"));
			Assert.assertTrue(loadedReg.getEventId().equals(eventId));
			Assert.assertTrue(loadedReg.getUserId().equals(userId));

		} catch (Exception e) {
			System.err.println(e.getMessage());
		}

	}

	@Test(expected = RegisterException.class)
	public void testDeleteRegistration() throws RegisterException {

		// create a user
		String userId = userDao.save(User.builder().email("test@test.com").firstName("Ahmed").lastName("Bhaila")
				.password("password").phone("1-800-456-4567").build(), "test");

		// create an event
		String eventId = eventDao.save(Event.builder().dateTime("09-12-2016 12:00:00")
				.description("This is a test event").geoLocation("124,124").isPublic("yes").location("Chicago, IL")
				.name("Test Event").photo("http://photourl").webUrl("http://www.test.com").build(), "test");

		// register user with an event
		Registration reg = new Registration();
		reg.setEventId(eventId);
		reg.setUserId(userId);
		String regId = regDao.save(reg, "test");

		// delete event
		regDao.delete(regId);

		// do a get on the deleted event
		Registration loadedReg = regDao.load(regId);

		Assert.assertNull(loadedReg);
	}

}
