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

import uk.ac.man.cs.eventlite.EventLite;
import uk.ac.man.cs.eventlite.entities.Venue;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = EventLite.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("test")
public class EventsControllerIntegrationTest extends AbstractTransactionalJUnit4SpringContextTests {

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
	public void testGetAllEvents() {
		client.get().uri("/events").accept(MediaType.TEXT_HTML).exchange().expectStatus().isOk();
	}

	@Test
	public void getEventNotFound() {
		client.get().uri("/events/99").accept(MediaType.TEXT_HTML).exchange().expectStatus().isNotFound().expectHeader()
				.contentTypeCompatibleWith(MediaType.TEXT_HTML).expectBody(String.class).consumeWith(result -> {
					assertThat(result.getResponseBody(), containsString("99"));
				});
	}
	
	@Test
	public void getEventFound() {
		client.get().uri("/events/7").accept(MediaType.TEXT_HTML).exchange().expectStatus().isOk().expectHeader()
		.contentTypeCompatibleWith(MediaType.TEXT_HTML).expectBody(String.class).consumeWith(result -> {
			assertThat(result.getResponseBody(), containsString("1"));
		});
	}
	
	@Test
	public void  getAddEvent() {
		client.mutate().filter(basicAuthentication("Rob", "Haines")).build().get().uri("/events/add")
		.accept(MediaType.TEXT_HTML).exchange().expectStatus().isOk().expectBody(String.class)
		.consumeWith(result -> {
			assertThat(result.getResponseBody(), containsString("_csrf"));
		});

	}
	
	@Test
	public void  postAddEvent() {
		String[] tokens = login();

		// Attempt to POST a valid greeting.
		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		Venue v = new Venue();
		form.add("_csrf", tokens[0]);
		form.add("name", "TestEventName");
		form.add("date", "2023-06-10");
		form.add("venue", "3");

		// The session ID cookie holds our login credentials.
		client.mutate().filter(basicAuthentication("Rob", "Haines")).build().post().uri("/events").accept(MediaType.TEXT_HTML).contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.bodyValue(form).cookies(cookies -> {
					cookies.add(SESSION_KEY, tokens[1]);
				}).exchange().expectStatus().isFound().expectHeader().value("Location", endsWith("/events"));

		// Check one row is added to the database.
		assertThat(currentRows + 1, equalTo(countRowsInTable("event")));

	}
	
	@Test 
	public void getUpdateEvent() {
		client.mutate().filter(basicAuthentication("Rob", "Haines")).build().get().uri("/events/update/6")
		.accept(MediaType.TEXT_HTML).exchange().expectStatus().isOk().expectBody(String.class)
		.consumeWith(result -> {
			assertThat(result.getResponseBody(), containsString("_csrf"));
		}); 
	}
	
	@Test
	public void postUpdateEvent() {
		String[] tokens = login();

		// Attempt to POST a valid greeting.
		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		Venue v = new Venue();
		form.add("_csrf", tokens[0]);
		form.add("name", "TestEventName");
		form.add("date", "2023-06-10");
		form.add("venue", "3");

		// The session ID cookie holds our login credentials.
		client.mutate().filter(basicAuthentication("Rob", "Haines")).build().post().uri("/events/update/6").accept(MediaType.TEXT_HTML).contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.bodyValue(form).cookies(cookies -> {
					cookies.add(SESSION_KEY, tokens[1]);
				}).exchange().expectStatus().isFound().expectHeader().value("Location", endsWith("/events/detail/6"));

		// Check one row is not added to the database.
		assertThat(currentRows , equalTo(countRowsInTable("event")));

	}
	
	private String[] login() {
		String[] tokens = new String[2];

		// Although this doesn't POST the log in form it effectively logs us in.
		// If we provide the correct credentials here, we get a session ID back which
		// keeps us logged in.
		EntityExchangeResult<String> result = client.mutate().filter(basicAuthentication("Rob", "Haines")).build().get()
				.uri("/events").accept(MediaType.TEXT_HTML).exchange().expectBody(String.class).returnResult();
		tokens[0] = getCsrfToken(result.getResponseBody());
		tokens[1] = result.getResponseCookies().getFirst(SESSION_KEY).getValue();

		return tokens;
	}

	private String getCsrfToken(String body) {
		Matcher matcher = CSRF.matcher(body);

		// matcher.matches() must be called; might as well assert something as well...
		assertThat(matcher.matches(), equalTo(true));

		return matcher.group(1);
	}

}
