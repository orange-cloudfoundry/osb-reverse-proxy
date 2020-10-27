package com.orange.oss.osbreverseproxy;

import java.nio.charset.Charset;
import java.util.Base64;

import org.bouncycastle.util.Arrays;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.web.reactive.server.WebTestClient;

/**
 * Spring cloud security integration test with reactive flavor imposed by spring-cloud-gateway expecting reactive
 * engine
 * See https://docs.spring.io/spring-framework/docs/current/spring-framework-reference/testing.html#webtestclient
 * Mock MVC used in non reactive stack does not seem ready for reactive stack https://stackoverflow.com/questions/57631829/is-mockmvc-eligible-for-webflux-controllers-testing
 *
 * Therefore, the following non reactive resources previously used are not relevant anymore:
 * Uses spring web mvc mock support. See inspiration from https://www.baeldung.com/spring-security-integration-tests
 * Reference doc https://docs.spring.io/spring-security/site/docs/5.3.0.RELEASE/reference/html5/#running-as-a-user-in-spring-mvc-test-with-annotations
 * https://docs.spring.io/spring-security/site/docs/5.3.0.RELEASE/reference/html5/#testing-http-basic-authentication
 */
@SpringJUnitConfig
@ActiveProfiles(
	{"offline-test-without-cf" //disable service key workflow so that we can start without CF config
	})
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT) // real web service is needed
@TestPropertySource(properties = {
	"spring.security.user.name=" + SecurityConfigTest.ADMIN_USER,
	"spring.security.user.password=" + SecurityConfigTest.ADMIN_PASSWORD,

	"osbreverseproxy.backendBrokerUri=https://remote_broker:443/prefix",
	"osbreverseproxy.serviceProviderUser=" + SecurityConfigTest.SERVICE_PROVIDER_USER,
	"osbreverseproxy.serviceProviderPassword=" + SecurityConfigTest.SERVICE_PROVIDER_PASSWORD,

	//don't start spring cloud gateway in this unit test focused on spring security
	"spring.profiles.active=offline-test"
})
public class SecurityConfigTest {

	public static final String ADMIN_PASSWORD = "admin-osb-password";
	public static final String ADMIN_USER = "admin-osb-user";
	public static final String SERVICE_PROVIDER_PASSWORD = "service-provider-password";
	public static final String SERVICE_PROVIDER_USER = "service-provider-user";

	public static final String[] SENSITIVE_ACTUATOR_ENDPOINTS = {
		"info",
//			"gateway", //404
//			"conditions",
		"httptrace",
		"loggers",
		"metrics",
//			"threaddump"
	};
	public static final String[] SENSITIVE_ACTUATOR_ENDPOINTS_REFUSED_TO_SERVICE_CONSUMMERS = {
		"info",
//			"gateway", //404
//			"conditions",
		"loggers",
		"metrics",
//			"threaddump"
	};
	public static final String[] ALL_EXPOSED_ACTUATOR_ENDPOINTS = Arrays.append(SENSITIVE_ACTUATOR_ENDPOINTS, "health");


	@Autowired
	private WebTestClient webClient;

	@Test
	public void unAuthenticatedActuactorHealth_shouldSucceedWith200() {
		webClient.get()
			.uri("/actuator/health")
			.header("Content-Type", MediaType.APPLICATION_JSON.toString())
			.exchange()
			.expectStatus().isOk();
	}

	@Test
	public void unAuthenticatedSensitiveActuactorEndPoints_shouldFailWith401() {
		for (String endpoint : SENSITIVE_ACTUATOR_ENDPOINTS) {
			//without auth
			webClient.get()
				.uri("/actuator/" + endpoint)
				.header("Content-Type", MediaType.APPLICATION_JSON.toString())
				.exchange()
				.expectStatus().isUnauthorized();
			//with invalid auth
			webClient.get()
				.uri("/actuator/" + endpoint)
				.header("Content-Type", MediaType.APPLICATION_JSON.toString())
				.header("Authorization", "Basic " + base64Encode("invalid-login" + ":" + "invalid-password"))
				.exchange()
				.expectStatus().isUnauthorized();
		}
	}

	private String base64Encode(String value) {
		return Base64.getEncoder().encodeToString(value.getBytes(Charset.defaultCharset()));
	}

	@Test
	public void adminAuthenticated_to_ActuactorEndpoints_shouldSucceedWith200() {
		//Note: did not find proper basic auth support in WebTestClient
		//Workaround with header inspired from https://github.com/spring-projects/spring-security-reactive/blob/37749a64f782c2b2f81afb3db1b30cea3e956839/sample/src/test/java/sample/SecurityTests.java#L118
		for (String endpoint : ALL_EXPOSED_ACTUATOR_ENDPOINTS) {
			webClient
				.get()
				.uri("/actuator/" + endpoint)
				.header("Authorization", "Basic " + base64Encode(ADMIN_USER + ":" + ADMIN_PASSWORD))
				.header("Content-Type", MediaType.APPLICATION_JSON.toString())
				.exchange()
				.expectStatus().isOk();
		}
	}

	@Test
	public void serviceProvider_Authenticated_to_HttpTrace_ActuactorEndpoint_shouldSucceedWith200() {
		//Note: did not find proper basic auth support in WebTestClient
		//Workaround with header inspired from https://github.com/spring-projects/spring-security-reactive/blob/37749a64f782c2b2f81afb3db1b30cea3e956839/sample/src/test/java/sample/SecurityTests.java#L118
		webClient
			.get()
			.uri("/actuator/httptrace")
			.header("Authorization", "Basic " + base64Encode(SERVICE_PROVIDER_USER + ":" + SERVICE_PROVIDER_PASSWORD))
			.header("Content-Type", MediaType.APPLICATION_JSON.toString())
			.exchange()
			.expectStatus().isOk();
		for (String endpoint : SENSITIVE_ACTUATOR_ENDPOINTS_REFUSED_TO_SERVICE_CONSUMMERS) {
			webClient
				.get()
				.uri("/actuator/" + endpoint)
				.header("Authorization", "Basic " + base64Encode(SERVICE_PROVIDER_USER + ":" + SERVICE_PROVIDER_PASSWORD))
				.header("Content-Type", MediaType.APPLICATION_JSON.toString())
				.exchange()
				.expectStatus().isForbidden();
		}
	}

}
