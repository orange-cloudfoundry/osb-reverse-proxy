package com.orange.oss.osbreverseproxy;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Profile("!offline-test")
@Configuration
@EnableConfigurationProperties
public class ReverseProxyRoutesConfiguration {


	@Bean
	public RouteLocator myRoutes(RouteLocatorBuilder builder) {
		return builder.routes()
			.route(p -> p
				.path("*")
//				.path("/get")
				.filters(f -> f.addRequestHeader("Hello", "World"))
//				.uri("https://shield-webui-y_9846bf97-0d6f-4ce9-adff-b06a3e19000b.redacted-domain.com:443"))
				.uri("https://shield-webui-cf-mysql.readacted-domain.com/"))
//				.uri("http://httpbin.org:80"))
			.build();
	}

//	@Bean
//	public RouteLocator myRoutes(RouteLocatorBuilder builder, OsbReverseProxyProperties osbReverseProxyProperties) {
//		return builder.routes()
//			.route(p -> p
//				.path("*")
////				.path("/get")
//				.filters(f -> f.addRequestHeader("Hello", "World"))
////				.uri("https://shield-webui-y_9846bf97-0d6f-4ce9-adff-b06a3e19000b.redacted-domain.com:443"))
//				.uri("https://shield-webui-cf-mysql.readacted-domain.com/"))
////				.uri("http://httpbin.org:80"))
//			.build();
//	}
//
//	@Bean
//	public RouteLocator customRouteLocator(RouteLocatorBuilder builder, SimpleLoggingFilter loggingFilter) {
//		return builder.routes().route(r -> r.cloudFoundryRouteService()
////			.and().header("Host", ".*")
//			.filters(f -> f.requestHeaderToRequestUri("X-CF-Forwarded-Url")
//						  .filter(loggingFilter))
//			.uri("https://example.com")).build();
//	}
}
