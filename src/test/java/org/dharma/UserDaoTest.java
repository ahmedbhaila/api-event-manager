package org.dharma;

import org.dharma.dao.EventDao;
import org.dharma.dao.UserDao;
import org.dharma.exception.UserException;
import org.dharma.model.Event;
import org.dharma.model.User;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class UserDaoTest {

	private static String USER_ID_PREFIX = "userId:";

	@Autowired
	UserDao userDao;

	@Test
	public void testSaveEvent() {
		String userId = userDao.save(User.builder().email("test@test.com").firstName("Ahmed").lastName("Bhaila")
				.password("password").phone("1-800-456-4567").build(), "test");

		Assert.assertTrue(userId.startsWith(USER_ID_PREFIX));

		// do a GET to check for data inserted
		try {
			User user = userDao.get(userId);

			Assert.assertTrue(user.getCreatedBy().equals("test"));
			Assert.assertTrue(user.getEmail().equals("test@test.com"));
			Assert.assertTrue(user.getFirstName().equals("Ahmed"));
			Assert.assertTrue(user.getLastName().equals("Bhaila"));
			Assert.assertTrue(user.getPassword().equals("password"));
			Assert.assertTrue(user.getPhone().equals("1-800-456-4567"));

		} catch (Exception e) {
			System.err.println(e.getMessage());
		}

	}

	@Test(expected = UserException.class)
	public void testDeleteUser() throws UserException {

		String userId = userDao.save(User.builder().email("test@test.com").firstName("Ahmed").lastName("Bhaila")
				.password("password").phone("1-800-456-4567").build(), "test");

		// delete event
		userDao.delete(userId);

		// do a get on the deleted event
		User user = userDao.get(userId);

		Assert.assertNull(user);
	}

	@Test
	public void testUpdateEvent() {
		String userId = userDao.save(User.builder().email("test@test.com").firstName("Ahmed").lastName("Bhaila")
				.password("password").phone("1-800-456-4567").build(), "test");

		Assert.assertTrue(userId.startsWith(USER_ID_PREFIX));

		// do a GET to check for data inserted
		try {
			User user = userDao.get(userId);

			// update values
			user.setEmail("updatedtest@test.com");
			user.setFirstName("Tester");
			user.setLastName("McTesterFace");
			user.setPassword("updated_password");
			user.setPhone("1-800-111-1111");

			// update event
			String updatedEventId = userDao.update(userId, user);

			// eventId shouldnt be updated
			Assert.assertTrue(updatedEventId.equals(userId));

			// assert all other values
			Assert.assertTrue(user.getCreatedBy().equals("test"));
			Assert.assertTrue(user.getEmail().equals("updatedtest@test.com"));
			Assert.assertTrue(user.getFirstName().equals("Tester"));
			Assert.assertTrue(user.getLastName().equals("McTesterFace"));
			Assert.assertTrue(user.getPassword().equals("updated_password"));
			Assert.assertTrue(user.getPhone().equals("1-800-111-1111"));

		} catch (Exception e) {
			System.err.println(e.getMessage());
		}

	}

}
