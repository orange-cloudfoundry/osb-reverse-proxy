package com.orange.oss.osbreverseproxy;

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
		OsbReverseProxyProperties osbReverseProxyProperties) {
		return builder.routes()
			.route("osb-api",
				p -> p
					.path("/v2/**", "**/v2/**")
					.filters(f -> f
						.modifyResponseBody(String.class, String.class,
							(webExchange, originalBody) -> {
								if (originalBody != null) {
									webExchange.getAttributes().put("cachedResponseBodyObject", originalBody);
									return Mono.just(originalBody);
								} else {
									return Mono.empty();
								}
							})
						.modifyRequestBody(String.class, String.class,
							(webExchange, originalBody) -> {
								if (originalBody != null) {
									webExchange.getAttributes().put("cachedRequestBodyObject", originalBody);
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
