package com.orange.oss.osbreverseproxy;

import reactor.util.Logger;
import reactor.util.Loggers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.security.reactive.EndpointRequest;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.actuate.info.InfoEndpoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;
import org.springframework.security.web.server.savedrequest.NoOpServerRequestCache;

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

	//Default spring-boot-actuator config authenticates any actuator endpoint except info and health endpoints
	//See https://github.com/spring-projects/spring-boot/blob/3b28b1cadeaf0c2112de90a7662883afc0901c9e/spring-boot-project/spring-boot-actuator-autoconfigure/src/main/java/org/springframework/boot/actuate/autoconfigure/security/reactive/ReactiveManagementWebSecurityAutoConfiguration.java#L60

	//In the future, we'll want to further restrict roles to distinguish between osb-cmdb admins (getting
	//permissions to/act/on
	//osb-reverse-proxy: e.g. increase log levels, threadumps...) and osb-providers (only getting read access to
	//selected endpoints such as httptrace)

	//See https://spring.io/guides/topicals/spring-security-architecture for a primer

	//SpringBootActuator default spring security autoconfig does not kick in:
	//See sources at https://github.com/spring-projects/spring-boot/blob/3b28b1cadeaf0c2112de90a7662883afc0901c9e/spring-boot-project/spring-boot-actuator-autoconfigure/src/main/java/org/springframework/boot/actuate/autoconfigure/security/reactive/ReactiveManagementWebSecurityAutoConfiguration.java#L57
	//Observed symptom:
	//	ReactiveSecurityAutoConfiguration.EnableWebFluxSecurityConfiguration:
	//	Did not match:
	//		- @ConditionalOnMissingBean (types: org.springframework.security.web.server.WebFilterChainProxy; SearchStrategy: all) found beans of type 'org.springframework.security.web.server.WebFilterChainProxy' org.springframework.security.config.annotation.web.reactive.WebFluxSecurityConfiguration.WebFilterChainFilter (OnBeanCondition)
	//	Matched:
	//		- found ReactiveWebApplicationContext (OnWebApplicationCondition)


	@Bean
	public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
		http
			.authorizeExchange((exchanges) -> {
				exchanges.matchers(EndpointRequest.to(HealthEndpoint.class)).permitAll();
				exchanges.anyExchange().authenticated();
		});
		http.httpBasic(Customizer.withDefaults());
		http.formLogin(Customizer.withDefaults());
		return http.build();
	}

	// The commented variant below was a failed attempt to craft similar config from stack overflow hints.
	// We likely need to remove this.

//
//	@Bean
//	public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
//		http
//			//Disable websession (polutting debug traces and slightly impacting performance
//			//See https://stackoverflow.com/questions/56056404/disable-websession-creation-when-using-spring-security-with-spring-webflux
//			.requestCache()
//				.requestCache(NoOpServerRequestCache.getInstance())
//			.and()
//				.securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
//			// See https://docs.spring.io/spring-security/site/docs/5.3.0.RELEASE/reference/html5/#csrf-when
//			//   Our recommendation is to use CSRF protection for any request that could be processed by a browser
//			//   by normal users. If you are only creating a service that is used by non-browser clients,
//			//   you will likely want to disable CSRF protection.
//			.csrf().disable()
//			.authorizeExchange()
//			    //actuator config
//				.matchers(EndpointRequest.toAnyEndpoint()
//					.excluding(HealthEndpoint.class))
//					.hasRole("USER")
//				.matchers(EndpointRequest.to(HealthEndpoint.class))
//					.permitAll()
//				// all other reversed-proxied endpoints (OSB or dashboards) should be propagated through
//				// spring cloud gateway without spring security interfering
//				.anyExchange().permitAll();
//		return http.build();
//	}


}
