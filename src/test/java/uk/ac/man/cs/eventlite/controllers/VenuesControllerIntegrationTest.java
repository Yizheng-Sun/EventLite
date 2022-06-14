package uk.ac.man.cs.eventlite.controllers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.StringContains.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.StringEndsWith.endsWith;
import static org.springframework.web.reactive.function.client.ExchangeFilterFunctions.basicAuthentication;


import java.util.regex.Pattern;
import java.util.regex.Matcher;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import uk.ac.man.cs.eventlite.dao.VenueService;

import uk.ac.man.cs.eventlite.EventLite;
import uk.ac.man.cs.eventlite.entities.Venue;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = EventLite.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("test")
public class VenuesControllerIntegrationTest extends AbstractTransactionalJUnit4SpringContextTests {
	

	
	private static Pattern CSRF = Pattern.compile("(?s).*name=\"_csrf\".*?value=\"([^\"]+).*");
	private static String SESSION_KEY = "JSESSIONID";
	
	@LocalServerPort
	private int port;
	private int currentRows;
	
	private WebTestClient client;
	
	@BeforeEach
	public void setup() {
		currentRows = countRowsInTable("event");
		client = WebTestClient.bindToServer().baseUrl("http://localhost:" + port).build();
	}
	
	@Test
	public void getVenueFound() {
		client.get().uri("/venues/detail/3").accept(MediaType.TEXT_HTML).exchange().expectStatus().isOk().expectHeader()
		.contentTypeCompatibleWith(MediaType.TEXT_HTML).expectBody(String.class).consumeWith(result -> {
			assertThat(result.getResponseBody(), containsString("1"));
		});
	}
		
	@Test
	public void getVenueNotFound() {
		client.get().uri("/venues/detail/99").accept(MediaType.TEXT_HTML).exchange().expectStatus().isNotFound().expectHeader()
		.contentTypeCompatibleWith(MediaType.TEXT_HTML).expectBody(String.class).consumeWith(result -> {
			assertThat(result.getResponseBody(), containsString("99"));
		});

	}
	
	@Test
	public void getAllVenues() {
		client.get().uri("/venues/index").accept(MediaType.TEXT_HTML).exchange().expectStatus().isFound();
	}
	
	@Test
	public void getAddVenue() {
		client.mutate().filter(basicAuthentication("Rob", "Haines")).build().get().uri("/venues/add")
		.accept(MediaType.TEXT_HTML).exchange().expectStatus().isOk().expectBody(String.class)
		.consumeWith(result -> {
			assertThat(result.getResponseBody(), containsString("_csrf"));
		});
	}

	}
	