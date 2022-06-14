package uk.ac.man.cs.eventlite.testutil;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FormUtil {

	private final static Pattern CSRF = Pattern.compile("(?s).*name=\"_csrf\".*?value=\"([^\"]+).*");

	public static String getCsrfToken(String body) {
		Matcher matcher = CSRF.matcher(body);

		// matcher.matches() must be called to run the actual match!
		assertThat(matcher.matches(), equalTo(true));

		return matcher.group(1);
	}
}
