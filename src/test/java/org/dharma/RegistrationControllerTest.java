package org.dharma;

import org.dharma.controller.EventController;
import org.dharma.controller.RegistrationController;
import org.dharma.controller.UserController;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.restdocs.JUnitRestDocumentation;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.equalTo;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = { UserController.class, EventController.class, RegistrationController.class })
public class RegistrationControllerTest {

	@Autowired
	private MockMvc mvc;

	@Autowired
	private WebApplicationContext context;

	@Autowired
	JedisConnectionFactory jedisConnection;

	@Autowired
	RedisTemplate<String, String> redisTemplate;

	@Rule
	public JUnitRestDocumentation restDocumentation = new JUnitRestDocumentation("build/generated-snippets");

	@Before
	public void setUp() {
		this.mvc = MockMvcBuilders.webAppContextSetup(this.context).apply(springSecurity())
				.apply(documentationConfiguration(this.restDocumentation)).build();
		jedisConnection.getConnection().flushDb();
		redisTemplate.opsForValue().set("admin", "admin");
		jedisConnection.getConnection().close();
	}

	@Test
	public void postForRegistration() throws Exception {

		// create an event
		String json = "{\"name\": \"testEvent3\", \"description\": \"test description\", \"location\": \"Chicago,IL\", \"dateTime\": \"2017-09-26T20:11:43.00Z\", \"isPublic\" : \"false\"}";

		MvcResult result = this.mvc.perform(post("/v1/event").contentType(MediaType.APPLICATION_JSON).content(json)
				.with(httpBasic("admin", "admin"))).andExpect(status().isCreated()).andReturn();

		String eventId = result.getResponse().getContentAsString();

		// create a user
		json = "{\"firstName\": \"Mickey\", \"lastName\": \"Mouse\", \"password\": \"minnie\", \"email\": \"mickey.mouse@disney.com\"}";

		result = this.mvc.perform(post("/v1/user").contentType(MediaType.APPLICATION_JSON).content(json)
				.with(httpBasic("admin", "admin"))).andExpect(status().isCreated()).andReturn();

		String userId = result.getResponse().getContentAsString();

		// register user with event
		json = "{\"userId\": \"" + userId + "\", \"eventId\": \"" + eventId + "\"}";

		result = this.mvc
				.perform(post("/v1/register").contentType(MediaType.APPLICATION_JSON).content(json)
						.with(httpBasic("admin", "admin")))
				.andExpect(status().isCreated()).andDo(document("postUserRegistration")).andReturn();

		String registerId = result.getResponse().getContentAsString();

		// use the eventId above to do a GET
		this.mvc.perform(get("/v1/register/" + registerId.split(":")[1]).with(httpBasic("admin", "admin")))
				.andExpect(status().isFound()).andExpect(jsonPath("$.eventId").value(eventId))
				.andExpect(jsonPath("$.userId").value(userId)).andDo(document("getUserRegistration")).andReturn();

		// verify that we can retrieve this users registration by userId
		result = this.mvc.perform(get("/v1/register/user/" + userId).with(httpBasic("admin", "admin")))
				.andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(equalTo(1))))
				.andDo(document("getRegistrationsByUser")).andReturn();

	}

	@Test
	public void postMissingMandatoryParametersForCreatingRegisteration() throws Exception {

		// create an event
		String json = "{\"name\": \"testEvent3\", \"description\": \"test description\", \"location\": \"Chicago,IL\", \"dateTime\": \"2017-09-26T20:11:43.00Z\", \"isPublic\" : \"false\"}";

		MvcResult result = this.mvc.perform(post("/v1/event").contentType(MediaType.APPLICATION_JSON).content(json)
				.with(httpBasic("admin", "admin"))).andExpect(status().isCreated()).andReturn();

		// create a user
		json = "{\"firstName\": \"Mickey\", \"lastName\": \"Mouse\", \"password\": \"minnie\", \"email\": \"mickey.mouse@disney.com\"}";

		result = this.mvc.perform(post("/v1/user").contentType(MediaType.APPLICATION_JSON).content(json)
				.with(httpBasic("admin", "admin"))).andExpect(status().isCreated()).andReturn();

		json = "{\"userId\": \"" + "\", \"eventId\": \"" + "\"}";

		this.mvc.perform(post("/v1/user").contentType(MediaType.APPLICATION_JSON).content(json)
				.with(httpBasic("admin", "admin"))).andExpect(status().isBadRequest());
	}

	@Test
	public void deleteRegistration() throws Exception {

		// create an event
		String json = "{\"name\": \"testEvent3\", \"description\": \"test description\", \"location\": \"Chicago,IL\", \"dateTime\": \"2017-09-26T20:11:43.00Z\", \"isPublic\" : \"false\"}";

		MvcResult result = this.mvc.perform(post("/v1/event").contentType(MediaType.APPLICATION_JSON).content(json)
				.with(httpBasic("admin", "admin"))).andExpect(status().isCreated()).andReturn();

		String eventId = result.getResponse().getContentAsString();

		// create a user
		json = "{\"firstName\": \"Mickey\", \"lastName\": \"Mouse\", \"password\": \"minnie\", \"email\": \"mickey.mouse@disney.com\"}";

		result = this.mvc.perform(post("/v1/user").contentType(MediaType.APPLICATION_JSON).content(json)
				.with(httpBasic("admin", "admin"))).andExpect(status().isCreated()).andReturn();

		String userId = result.getResponse().getContentAsString();

		// register user with event
		json = "{\"userId\": \"" + userId + "\", \"eventId\": \"" + eventId + "\"}";

		result = this.mvc.perform(post("/v1/register").contentType(MediaType.APPLICATION_JSON).content(json)
				.with(httpBasic("admin", "admin"))).andExpect(status().isCreated()).andReturn();

		String registerId = result.getResponse().getContentAsString();

		// use the eventId above to do a GET
		this.mvc.perform(get("/v1/register/" + registerId.split(":")[1]).with(httpBasic("admin", "admin")))
				.andExpect(status().isFound()).andExpect(jsonPath("$.eventId").value(eventId))
				.andExpect(jsonPath("$.userId").value(userId)).andReturn();

		result = this.mvc.perform(get("/v1/register/total").with(httpBasic("admin", "admin")))
				.andExpect(status().isOk()).andDo(document("getTotalUsers")).andReturn();

		Long a = Long.valueOf(result.getResponse().getContentAsString());

		// delete this registration
		this.mvc.perform(delete("/v1/register/" + registerId.split(":")[1]).with(httpBasic("admin", "admin")))
				.andExpect(status().isNoContent()).andDo(document("deleteUserEventRegistration"));

		result = this.mvc.perform(get("/v1/register/total").with(httpBasic("admin", "admin")))
				.andExpect(status().isOk()).andDo(document("getTotalUsers")).andReturn();

		Long b = Long.valueOf(result.getResponse().getContentAsString());

		Assert.assertTrue(b == a - 1);

		// verify that we can retrieve this users registration by userId
		result = this.mvc.perform(get("/v1/register/user/" + userId).with(httpBasic("admin", "admin")))
				.andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(equalTo(0)))).andReturn();

	}

	@Test
	public void testForNonExistantRegistration() throws Exception {
		this.mvc.perform(get("/v1/register/-1").with(httpBasic("admin", "admin"))).andExpect(status().isNotFound());
	}

	@Test
	public void testGetTotalRegistrations() throws Exception {

		MvcResult result = this.mvc.perform(get("/v1/register/total").with(httpBasic("admin", "admin")))
				.andExpect(status().isOk()).andDo(document("getTotalUsers")).andReturn();

		Long a = Long.valueOf(result.getResponse().getContentAsString());

		// create an event
		String json = "{\"name\": \"testEvent3\", \"description\": \"test description\", \"location\": \"Chicago,IL\", \"dateTime\": \"2017-09-26T20:11:43.00Z\", \"isPublic\" : \"false\"}";

		result = this.mvc.perform(post("/v1/event").contentType(MediaType.APPLICATION_JSON).content(json)
				.with(httpBasic("admin", "admin"))).andExpect(status().isCreated()).andReturn();

		String eventId = result.getResponse().getContentAsString();

		// create a user
		json = "{\"firstName\": \"Mickey\", \"lastName\": \"Mouse\", \"password\": \"minnie\", \"email\": \"mickey.mouse@disney.com\"}";

		result = this.mvc.perform(post("/v1/user").contentType(MediaType.APPLICATION_JSON).content(json)
				.with(httpBasic("admin", "admin"))).andExpect(status().isCreated()).andReturn();

		String userId = result.getResponse().getContentAsString();

		// register user with event
		json = "{\"userId\": \"" + userId + "\", \"eventId\": \"" + eventId + "\"}";

		result = this.mvc
				.perform(post("/v1/register").contentType(MediaType.APPLICATION_JSON).content(json)
						.with(httpBasic("admin", "admin")))
				.andExpect(status().isCreated()).andDo(document("postUserRegistration")).andReturn();

		String registerId = result.getResponse().getContentAsString();

		// use the eventId above to do a GET
		this.mvc.perform(get("/v1/register/" + registerId.split(":")[1]).with(httpBasic("admin", "admin")))
				.andExpect(status().isFound()).andExpect(jsonPath("$.eventId").value(eventId))
				.andExpect(jsonPath("$.userId").value(userId)).andDo(document("getUserRegistration")).andReturn();

		result = this.mvc.perform(get("/v1/register/total").with(httpBasic("admin", "admin")))
				.andExpect(status().isOk()).andDo(document("getTotalUsers")).andReturn();

		Long b = Long.valueOf(result.getResponse().getContentAsString());

		Assert.assertTrue(b == a + 1);
	}

	@Test
	public void postForMultipleRegistration() throws Exception {

		// create an event
		String json = "{\"name\": \"testEvent3\", \"description\": \"test description\", \"location\": \"Chicago,IL\", \"dateTime\": \"2017-09-26T20:11:43.00Z\", \"isPublic\" : \"false\"}";

		MvcResult result = this.mvc.perform(post("/v1/event").contentType(MediaType.APPLICATION_JSON).content(json)
				.with(httpBasic("admin", "admin"))).andExpect(status().isCreated()).andReturn();

		String eventId = result.getResponse().getContentAsString();

		// create a user
		json = "{\"firstName\": \"Mickey\", \"lastName\": \"Mouse\", \"password\": \"minnie\", \"email\": \"mickey.mouse@disney.com\"}";

		result = this.mvc.perform(post("/v1/user").contentType(MediaType.APPLICATION_JSON).content(json)
				.with(httpBasic("admin", "admin"))).andExpect(status().isCreated()).andReturn();

		String userId = result.getResponse().getContentAsString();

		// register user with event
		json = "{\"userId\": \"" + userId + "\", \"eventId\": \"" + eventId + "\"}";

		result = this.mvc
				.perform(post("/v1/register").contentType(MediaType.APPLICATION_JSON).content(json)
						.with(httpBasic("admin", "admin")))
				.andExpect(status().isCreated()).andDo(document("postUserRegistration")).andReturn();

		String registerId = result.getResponse().getContentAsString();

		// use the eventId above to do a GET
		this.mvc.perform(get("/v1/register/" + registerId.split(":")[1]).with(httpBasic("admin", "admin")))
				.andExpect(status().isFound()).andExpect(jsonPath("$.eventId").value(eventId))
				.andExpect(jsonPath("$.userId").value(userId)).andDo(document("getUserRegistration")).andReturn();

		// create another event
		json = "{\"name\": \"testEvent3\", \"description\": \"test description\", \"location\": \"Chicago,IL\", \"dateTime\": \"2017-09-26T20:11:43.00Z\", \"isPublic\" : \"false\"}";

		result = this.mvc.perform(post("/v1/event").contentType(MediaType.APPLICATION_JSON).content(json)
				.with(httpBasic("admin", "admin"))).andExpect(status().isCreated()).andReturn();

		String eventId2 = result.getResponse().getContentAsString();

		// use same user to register with eventId2
		// register user with event
		json = "{\"userId\": \"" + userId + "\", \"eventId\": \"" + eventId2 + "\"}";

		result = this.mvc
				.perform(post("/v1/register").contentType(MediaType.APPLICATION_JSON).content(json)
						.with(httpBasic("admin", "admin")))
				.andExpect(status().isCreated()).andDo(document("postUserRegistration")).andReturn();

		// verify that we can retrieve this users registration by userId, this should return 2
		result = this.mvc.perform(get("/v1/register/user/" + userId).with(httpBasic("admin", "admin")))
				.andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(equalTo(2))))
				.andDo(document("getRegistrationsByUser")).andReturn();

	}

}
