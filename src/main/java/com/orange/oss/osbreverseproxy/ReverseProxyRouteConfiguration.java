package com.orange.oss.osbreverseproxy;

import org.apache.commons.lang3.StringUtils;
import reactor.core.publisher.Mono;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Profile("!offline-test")
@Configuration
@EnableConfigurationProperties(OsbReverseProxyProperties.class)
@EnableAutoConfiguration // necessary for unit tests not starting a springboot app but merely a spring context
public class ReverseProxyRouteConfiguration {



	@Bean
	public RouteLocator osbApiRoute(RouteLocatorBuilder builder,
		final OsbReverseProxyProperties osbReverseProxyProperties) {
		return builder.routes()
			.route("osb-api",
				p -> p
					.host("**" + (osbReverseProxyProperties.getWhiteListedOsbDomain() == null ? "" :
						"." + osbReverseProxyProperties.getWhiteListedOsbDomain()))
					.and().path("/v2/**", "**/v2/**")

					.filters(f -> f
						.modifyResponseBody(String.class, String.class,
							(webExchange, originalBody) -> {
								if (originalBody != null) {
									//See https://stackoverflow.com/a/19975149/1484823 for abbreviation
									String abbreviatedBody = StringUtils.abbreviate(originalBody,
										osbReverseProxyProperties.getAbbreviateHttpTraceLargerThanBytes());
									webExchange.getAttributes().put("cachedResponseBodyObject", abbreviatedBody);
									return Mono.just(originalBody);
								} else {
									return Mono.empty();
								}
							})
						.modifyRequestBody(String.class, String.class,
							(webExchange, originalBody) -> {
								if (originalBody != null) {
									//See https://stackoverflow.com/a/19975149/1484823 for abbreviation
									String abbreviatedBody = StringUtils.abbreviate(originalBody,
										osbReverseProxyProperties.getAbbreviateHttpTraceLargerThanBytes());
									webExchange.getAttributes().put("cachedRequestBodyObject", abbreviatedBody);
									return Mono.just(originalBody);
								} else {
									return Mono.empty();
								}
							})

					)
					.uri(osbReverseProxyProperties.getBackendBrokerUri())
			)
			.build();
	}


//	@Bean
//	public RouteLocator customRouteLocator(RouteLocatorBuilder builder, SimpleLoggingFilter loggingFilter) {
//		return builder.routes().route(r -> r.cloudFoundryRouteService()
////			.and().header("Host", ".*")
//			.filters(f -> f.requestHeaderToRequestUri("X-CF-Forwarded-Url")
//						  .filter(loggingFilter))
//			.uri("https://example.com")).build();
//	}
}
