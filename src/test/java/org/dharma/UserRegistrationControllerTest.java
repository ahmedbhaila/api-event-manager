package org.dharma;

import org.dharma.controller.EventRegistrationController;
import org.dharma.controller.UserRegistrationController;
import org.hamcrest.Matcher;
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
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@WebMvcTest(UserRegistrationController.class)
public class UserRegistrationControllerTest {

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
	public void postMandatoryParametersForCreatingUser() throws Exception {

		String json = "{\"firstName\": \"Mickey\", \"lastName\": \"Mouse\", \"password\": \"minnie\", \"email\": \"mickey.mouse@disney.com\"}";

		MvcResult result = this.mvc
				.perform(post("/v1/user").contentType(MediaType.APPLICATION_JSON).content(json)
						.with(httpBasic("admin", "admin")))
				.andExpect(status().isCreated()).andDo(document("postUser")).andReturn();

		String userId = result.getResponse().getContentAsString();

		// use the eventId above to do a GET
		this.mvc.perform(get("/v1/user/" + userId.split(":")[1]).with(httpBasic("admin", "admin")))
				.andExpect(status().isFound()).andExpect(jsonPath("$.firstName").value("Mickey"))
				.andExpect(jsonPath("$.lastName").value("Mouse")).andExpect(jsonPath("$.password").value("minnie"))
				.andExpect(jsonPath("$.email").value("mickey.mouse@disney.com"))
				.andDo(document("getUser"))
				.andReturn();
	}

	@Test
	public void postAllParametersForCreatingUser() throws Exception {

		String json = "{\"firstName\": \"Mickey\", " + "\"lastName\": \"Mouse\", " + "\"password\": \"minnie\", "
				+ "\"email\": \"mickey.mouse@disney.com\", " + "\"phone\": \"1-800-123-4567\"}";

		MvcResult result = this.mvc
				.perform(post("/v1/user").contentType(MediaType.APPLICATION_JSON).content(json)
						.with(httpBasic("admin", "admin")))
				.andExpect(status().isCreated()).andDo(document("postAllUser")).andReturn();

		String userId = result.getResponse().getContentAsString();

		// use the eventId above to do a GET
		this.mvc.perform(get("/v1/user/" + userId.split(":")[1]).with(httpBasic("admin", "admin")))
				.andExpect(status().isFound()).andExpect(jsonPath("$.firstName").value("Mickey"))
				.andExpect(jsonPath("$.lastName").value("Mouse")).andExpect(jsonPath("$.password").value("minnie"))
				.andExpect(jsonPath("$.email").value("mickey.mouse@disney.com"))
				.andExpect(jsonPath("$.phone").value("1-800-123-4567"))
				.andReturn();
	}

	@Test
	public void postMissingMandatoryParametersForCreatingUser() throws Exception {

		String json = "{\"lastName\": \"Mouse\", " + "\"password\": \"minnie\", "
				+ "\"email\": \"mickey.mouse@disney.com\", " + "\"phone\": \"1-800-123-4567\"}";

		this.mvc.perform(post("/v1/user").contentType(MediaType.APPLICATION_JSON).content(json)
				.with(httpBasic("admin", "admin"))).andExpect(status().isBadRequest());

	}

	@Test
	public void postBadFormattedParametersForCreatingUser() throws Exception {

		String json = "{\"lastName\": \"Mouse\", " + "\"password\": \"minnie\", " + "\"email\": \"mickey.disney.com\", "
				+ "\"phone\": \"1-800-123-4567\"}";

		this.mvc.perform(post("/v1/user").contentType(MediaType.APPLICATION_JSON).content(json)
				.with(httpBasic("admin", "admin"))).andExpect(status().isBadRequest());
	}

	@Test
	public void updateEvent() throws Exception {

		String json = "{\"firstName\": \"Mickey\", " + "\"lastName\": \"Mouse\", " + "\"password\": \"minnie\", "
				+ "\"email\": \"mickey.mouse@disney.com\", " + "\"phone\": \"1-800-123-4567\"}";

		MvcResult result = this.mvc
				.perform(post("/v1/user").contentType(MediaType.APPLICATION_JSON).content(json)
						.with(httpBasic("admin", "admin")))
				.andExpect(status().isCreated()).andDo(document("user")).andReturn();

		String userId = result.getResponse().getContentAsString();

		// use the eventId above to do a GET
		result = this.mvc.perform(get("/v1/user/" + userId.split(":")[1]).with(httpBasic("admin", "admin")))
				.andExpect(status().isFound()).andExpect(jsonPath("$.firstName").value("Mickey"))
				.andExpect(jsonPath("$.lastName").value("Mouse")).andExpect(jsonPath("$.password").value("minnie"))
				.andExpect(jsonPath("$.email").value("mickey.mouse@disney.com"))
				.andExpect(jsonPath("$.phone").value("1-800-123-4567"))
				.andReturn();

		String updatedJson = result.getResponse().getContentAsString().replace("Mickey", "Minnie");

		result = this.mvc
				.perform(put("/v1/user/" + userId.split(":")[1]).contentType(MediaType.APPLICATION_JSON)
						.content(updatedJson).with(httpBasic("admin", "admin")))
				.andExpect(status().isNoContent()).andDo(document("putUser")).andReturn();

		// do a GET again
		result = this.mvc.perform(get("/v1/user/" + userId.split(":")[1]).with(httpBasic("admin", "admin")))
				.andExpect(status().isFound()).andExpect(jsonPath("$.firstName").value("Minnie"))
				.andExpect(jsonPath("$.lastName").value("Mouse")).andExpect(jsonPath("$.password").value("minnie"))
				.andExpect(jsonPath("$.email").value("mickey.mouse@disney.com"))
				.andExpect(jsonPath("$.phone").value("1-800-123-4567"))
				.andReturn();

	}

	@Test
	public void deleteUser() throws Exception {

		String json = "{\"firstName\": \"Nicky\", " + "\"lastName\": \"Nouse\", " + "\"password\": \"minnie\", "
				+ "\"email\": \"nickey.nouse@disney.com\", " + "\"phone\": \"1-800-123-4567\"}";

		MvcResult result = this.mvc
				.perform(post("/v1/user").contentType(MediaType.APPLICATION_JSON).content(json)
						.with(httpBasic("admin", "admin")))
				.andExpect(status().isCreated()).andDo(document("user")).andReturn();

		String userId = result.getResponse().getContentAsString();

		// use the eventId above to do a GET
		result = this.mvc.perform(get("/v1/user/" + userId.split(":")[1]).with(httpBasic("admin", "admin")))
				.andExpect(status().isFound()).andExpect(jsonPath("$.firstName").value("Nicky"))
				.andExpect(jsonPath("$.lastName").value("Nouse")).andExpect(jsonPath("$.password").value("minnie"))
				.andExpect(jsonPath("$.email").value("nickey.nouse@disney.com"))
				.andExpect(jsonPath("$.phone").value("1-800-123-4567"))
				.andReturn();

		// delete this user
		this.mvc.perform(delete("/v1/user/" + userId.split(":")[1]).with(httpBasic("admin", "admin")))
				.andExpect(status().isNotFound())
				.andDo(document("deleteUser")).andReturn();

	}

	@Test
	public void testForNonExistantUser() throws Exception {
		this.mvc.perform(get("/v1/user/-1").with(httpBasic("admin", "admin"))).andExpect(status().isNotFound());
	}

}
