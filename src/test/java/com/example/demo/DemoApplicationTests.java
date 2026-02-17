package com.example.demo;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class DemoApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@BeforeEach
	void setUp() {
		userRepository.deleteAll();
	}

	@Test
	void contextLoads() {
	}

	@Test
	void registerShouldCreateUser() throws Exception {
		Map<String, String> request = Map.of("username", "alice", "password", "secret123");

		mockMvc.perform(post("/auth/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.username").value("alice"));
	}

	@Test
	void loginShouldReturnTokenForValidCredentials() throws Exception {
		User user = new User("alice", passwordEncoder.encode("secret123"));
		userRepository.save(user);

		Map<String, String> request = Map.of("username", "alice", "password", "secret123");

		mockMvc.perform(post("/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.token").isNotEmpty());
	}

	@Test
	void loginShouldReturnUnauthorizedForInvalidPassword() throws Exception {
		User user = new User("alice", passwordEncoder.encode("secret123"));
		userRepository.save(user);

		Map<String, String> request = Map.of("username", "alice", "password", "wrong-password");

		mockMvc.perform(post("/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isUnauthorized());
	}

	@Test
	void loginShouldReturnUnauthorizedForUnknownUser() throws Exception {
		Map<String, String> request = Map.of("username", "unknown", "password", "secret123");

		mockMvc.perform(post("/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isUnauthorized());
	}

	@Test
	void protectedEndpointShouldRequireJwt() throws Exception {
		mockMvc.perform(get("/api/profile"))
			.andExpect(status().isUnauthorized());
	}

	@Test
	void protectedEndpointShouldWorkWithValidJwt() throws Exception {
		mockMvc.perform(post("/auth/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(Map.of("username", "bob", "password", "secret123"))))
			.andExpect(status().isCreated());

		MvcResult loginResult = mockMvc.perform(post("/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(Map.of("username", "bob", "password", "secret123"))))
			.andExpect(status().isOk())
			.andReturn();

		String token = objectMapper.readTree(loginResult.getResponse().getContentAsString()).get("token").asText();

		mockMvc.perform(get("/api/profile")
				.header("Authorization", "Bearer " + token))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.username").value("bob"));
	}

}
