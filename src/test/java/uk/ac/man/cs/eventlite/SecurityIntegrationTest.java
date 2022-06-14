package uk.ac.man.cs.eventlite;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.StringEndsWith.endsWith;
import static uk.ac.man.cs.eventlite.testutil.FormUtil.getCsrfToken;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = EventLite.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class SecurityIntegrationTest {

	// This class isn't strictly necessary. We wouldn't normally make a habit of
	// testing functionality provided by a library, but these tests serve as good
	// examples of how to (integration) test with and around security.

	private final static String SESSION_KEY = "JSESSIONID";
	private final static String LOGIN_PATH = "/sign-in";
	private final static String LOGIN_ERROR = LOGIN_PATH + "?error";
	private final static String LOGOUT_PATH = "/sign-out";

	@LocalServerPort
	private int port;

	private String baseUri;

	private WebTestClient client;

	@BeforeEach
	public void setUp() {
		baseUri = "http://localhost:" + port;
		client = WebTestClient.bindToServer().baseUrl(baseUri).build();
	}

	@Test
	public void signIn() throws Exception {
		// GET the login form so we can read the CSRF token and session cookie.
		EntityExchangeResult<String> result = client.get().uri(LOGIN_PATH).accept(MediaType.TEXT_HTML).exchange()
				.expectStatus().isOk().expectBody(String.class).returnResult();
		String csrfToken = getCsrfToken(result.getResponseBody());
		String cookie = result.getResponseCookies().getFirst(SESSION_KEY).getValue();

		// Populate the form with the required data.
		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("_csrf", csrfToken);
		form.add("username", "Rob");
		form.add("password", "Haines");

		// POST the populated login form.
		client.post().uri(LOGIN_PATH).accept(MediaType.TEXT_HTML).contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.cookie(SESSION_KEY, cookie).bodyValue(form).exchange().expectStatus().isFound().expectHeader()
				.value("Location", equalTo(baseUri + "/"));
	}

	@Test
	public void signInBadUser() throws Exception {
		// GET the login form so we can read the CSRF token and session cookie.
		EntityExchangeResult<String> result = client.get().uri(LOGIN_PATH).accept(MediaType.TEXT_HTML).exchange()
				.expectStatus().isOk().expectBody(String.class).returnResult();
		String csrfToken = getCsrfToken(result.getResponseBody());
		String cookie = result.getResponseCookies().getFirst(SESSION_KEY).getValue();

		// Populate the form with the required data.
		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("_csrf", csrfToken);
		form.add("username", "Robert");
		form.add("password", "Haines");

		// POST the populated login form.
		client.post().uri(LOGIN_PATH).accept(MediaType.TEXT_HTML).contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.cookie(SESSION_KEY, cookie).bodyValue(form).exchange().expectStatus().isFound().expectHeader()
				.value("Location", endsWith(LOGIN_ERROR));
	}

	@Test
	public void signOut() throws Exception {
		// GET the login form so we can read the CSRF token and session cookie.
		EntityExchangeResult<String> result = client.get().uri(LOGIN_PATH).accept(MediaType.TEXT_HTML).exchange()
				.expectStatus().isOk().expectBody(String.class).returnResult();
		String csrfToken = getCsrfToken(result.getResponseBody());
		String cookie = result.getResponseCookies().getFirst(SESSION_KEY).getValue();

		// Populate the form with the required data.
		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("_csrf", csrfToken);
		form.add("username", "Rob");
		form.add("password", "Haines");

		// POST the populated login form and get the updated session cookie.
		result = client.post().uri(LOGIN_PATH).accept(MediaType.TEXT_HTML)
				.contentType(MediaType.APPLICATION_FORM_URLENCODED).cookie(SESSION_KEY, cookie).bodyValue(form)
				.exchange().expectStatus().isFound().expectHeader().value("Location", equalTo(baseUri + "/"))
				.expectBody(String.class).returnResult();
		cookie = result.getResponseCookies().getFirst(SESSION_KEY).getValue();

		// Populate the logout form with the CSRF token.
		MultiValueMap<String, String> logout = new LinkedMultiValueMap<>();
		logout.add("_csrf", csrfToken);
	}
}
