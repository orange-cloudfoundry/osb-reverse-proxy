package com.orange.oss.osbreverseproxy;

import reactor.util.Logger;
import reactor.util.Loggers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.security.reactive.EndpointRequest;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.server.SecurityWebFilterChain;

/**
 * See https://stackoverflow.com/questions/60603772/spring-security-configuration-basic-auth-spring-cloud-gateway for
 * general question about securing actuactor for a spring-cloud-gateway app while letting other credentials be
 * propagated downstream
 * See https://docs.spring.io/spring-security/site/docs/5.3.4.RELEASE/reference/html5/#explicit-webflux-security-configuration
 * for spring security context for a reactive stack
 */
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

	private final Logger log = Loggers.getLogger(SecurityConfig.class);

	public static final String SPRINGBOOT_SECURITY_USER_PROP_NAME = "spring.security.user.name";
	public static final String SPRINGBOOT_SECURITY_PASSWORD_PROP_NAME = "spring.security.user.password";

	//Note: could be moving this into a properties bean, referenced using @EnableConfigurationProperties
	@Value("${" + SPRINGBOOT_SECURITY_USER_PROP_NAME + "}")
	private String osbUser;

	@Value("${" + SPRINGBOOT_SECURITY_PASSWORD_PROP_NAME + "}")
	private String osbPassword;

	@Bean
	public MapReactiveUserDetailsService userDetailsService() {
		UserDetails user = User.withDefaultPasswordEncoder()
			.username(osbUser)
			.password(osbPassword)
			.roles("USER")
			.build();
		return new MapReactiveUserDetailsService(user);
	}

	@Bean
	public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
		http
			// See https://docs.spring.io/spring-security/site/docs/5.3.0.RELEASE/reference/html5/#csrf-when
			//   Our recommendation is to use CSRF protection for any request that could be processed by a browser
			//   by normal users. If you are only creating a service that is used by non-browser clients,
			//   you will likely want to disable CSRF protection.
			.csrf().disable()
			.authorizeExchange()
			    //actuator config
				.matchers(EndpointRequest.toAnyEndpoint()
					.excluding(HealthEndpoint.class))
					.authenticated()
				.matchers(EndpointRequest.to(HealthEndpoint.class))
					.permitAll()
			// all other reversed-proxied endpoints (OSB or dashboards) should be propagated through
			// spring cloud gateway without spring security interfering
			.anyExchange().permitAll();
		return http.build();
	}

}
