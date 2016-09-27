package org.dharma;

import java.util.List;

import org.dharma.exception.EventException;
import org.dharma.model.Event;
import org.dharma.services.EventService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class EventServiceTest {

	private static String EVENT_ID_PREFIX = "eventId:";

	@Autowired
	EventService eventService;

	@Test
	public void testSaveEvent() {
		String eventId = eventService.saveEvent(Event.builder().dateTime("09-12-2016 12:00:00")
				.description("This is a test event").geoLocation("124,124").isPublic("yes").location("Chicago, IL")
				.name("Test Event").photo("http://photourl").webUrl("http://www.test.com").build(), "test");

		Assert.assertTrue(eventId.startsWith(EVENT_ID_PREFIX));

		// do a GET to check for data inserted
		try {
			Event event = eventService.getEvent(eventId);

			Assert.assertTrue(event.getCreatedby().equals("test"));
			Assert.assertTrue(event.getDateTime().equals("09-12-2016 12:00:00"));
			Assert.assertTrue(event.getDescription().equals("This is a test event"));
			Assert.assertTrue(event.getGeoLocation().equals("124,124"));
			Assert.assertTrue(event.getIsPublic().equals("yes"));
			Assert.assertTrue(event.getLocation().equals("chicago, il"));
			Assert.assertTrue(event.getName().equals("Test Event"));
			Assert.assertTrue(event.getPhoto().equals("http://photourl"));
			Assert.assertTrue(event.getWebUrl().equals("http://www.test.com"));

		} catch (Exception e) {
			System.err.println(e.getMessage());
		}

	}

	@Test(expected = EventException.class)
	public void testDeleteEvent() throws EventException {

		// get all events
		List<Event> events = eventService.getEvents(1, 1, null);
		int total = events.size();

		String eventId = eventService.saveEvent(Event.builder().dateTime("09-12-2016 12:00:00")
				.description("This is a temp test event").geoLocation("124,124").isPublic("yes").location("Chicago, IL")
				.name("Temp Test Event").photo("http://photourl").webUrl("http://www.test.com").build(), "test");

		// delete event
		eventService.deleteEvent(eventId);

		// do a get on the deleted event
		Event event = eventService.getEvent(eventId);

		Assert.assertNull(event);

		// get all events again
		events = eventService.getEvents(1, 1, null);
		Assert.assertTrue(events.size() == total - 1);
	}

	@Test
	public void testUpdateEvent() {
		String eventId = eventService.saveEvent(Event.builder().dateTime("09-12-2016 12:00:00")
				.description("This is a temp test event").geoLocation("124,124").isPublic("yes").location("Chicago, IL")
				.name("Temp Test Event").photo("http://photourl").webUrl("http://www.test.com").build(), "test");

		Assert.assertTrue(eventId.startsWith(EVENT_ID_PREFIX));

		// do a GET to check for data inserted
		try {
			Event event = eventService.getEvent(eventId);

			// update values
			event.setName("Updated Name");
			event.setDateTime("10-12-2016 12:00:00");
			event.setDescription("This is a updated temp test event");
			event.setGeoLocation("125,125");
			event.setIsPublic("no");
			event.setLocation("Burbank, CA");
			event.setPhoto("https://photourl");
			event.setWebUrl("https://www.test.com");

			// update event
			String updatedEventId = eventService.updateEvent(eventId, event);

			// eventId shouldnt be updated
			Assert.assertTrue(updatedEventId.equals(eventId));

			// assert all other values
			Assert.assertTrue(event.getCreatedby().equals("test"));
			Assert.assertTrue(event.getDateTime().equals("10-12-2016 12:00:00"));
			Assert.assertTrue(event.getDescription().equals("This is a updated temp test event"));
			Assert.assertTrue(event.getGeoLocation().equals("125,125"));
			Assert.assertTrue(event.getIsPublic().equals("no"));
			Assert.assertTrue(event.getLocation().equals("Burbank, CA"));
			Assert.assertTrue(event.getName().equals("Updated Name"));
			Assert.assertTrue(event.getPhoto().equals("https://photourl"));
			Assert.assertTrue(event.getWebUrl().equals("https://www.test.com"));

		} catch (Exception e) {
			System.err.println(e.getMessage());
		}

	}

	@Test
	public void testForAll() {

		// insert two events
		testSaveEvent();
		testSaveEvent();

		List<Event> events = eventService.getEvents(1, 2, null);
		Assert.assertTrue(events.size() == 2);
	}

	@Test
	public void testWithCriteria() {
		// insert two events
		testSaveEvent();
		testSaveEvent();
		
		List<Event> events = eventService.getEvents(1, 2, "name:Test Event");
		
	}
}
