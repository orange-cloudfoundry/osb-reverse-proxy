package com.orange.oss.osbreverseproxy;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

import com.orange.oss.osbreverseproxy.actuator.ExtendedReadBodyPredicateFactory;
import reactor.core.publisher.Mono;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.gateway.handler.AsyncPredicate;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.server.ServerWebExchange;

@Profile("!offline-test")
@Configuration
@EnableConfigurationProperties(OsbReverseProxyProperties.class)
@EnableAutoConfiguration // necessary for unit tests not starting a springboot app but merely a spring context
public class ReverseProxyRouteConfiguration {



	@Bean
	public RouteLocator osbApiRoute(RouteLocatorBuilder builder,
		OsbReverseProxyProperties osbReverseProxyProperties,
		ExtendedReadBodyPredicateFactory rdpf) {
		return builder.routes()
			.route("osb-api",
				p -> p
					.path("/v2/**", "**/v2/**")
					.and().asyncPredicate(saveRequestBodyInExchangePredicate(rdpf))
					.filters(f -> f.modifyResponseBody(String.class, String.class,
						(webExchange, originalResponse) -> {
							webExchange.getAttributes().put("cachedResponseBodyObject", originalResponse);
							return Mono.just(originalResponse);
						}))
					.uri(osbReverseProxyProperties.getBackendBrokerUri())
			)
			.build();
	}

	//	@Bean
//	public RouteLocator myRoutes(RouteLocatorBuilder builder, OsbReverseProxyProperties osbReverseProxyProperties) {
//		return builder.routes().route("modify_response_java_test",
//			r -> r.path("/").and().host("www.modifyresponsebodyjava.org")
//				.filters(f -> f.prefixPath("/httpbin").modifyResponseBody(
//					String.class, Map.class,
//					(webExchange, originalResponse) -> {
//						Map<String, Object> modifiedResponse = new HashMap<>();
//						modifiedResponse.put("value", originalResponse);
//						modifiedResponse.put("length",
//							originalResponse.length());
//						return Mono.just(modifiedResponse);
//					}))
//				.uri(uri))
//			.route("modify_response_java_test_to_large", r -> r.path("/").and()
//				.host("www.modifyresponsebodyjavatoolarge.org")
//				.filters(f -> f.prefixPath("/httpbin").modifyResponseBody(
//					String.class, String.class,
//					(webExchange, originalResponse) -> {
//						return Mono.just(toLarge);
//					}))
//				.uri(uri))
//			.build();
//	}
//
	private AsyncPredicate<ServerWebExchange> saveRequestBodyInExchangePredicate(
		ExtendedReadBodyPredicateFactory rdpf) {
		return rdpf.applyAsync(alwaysMatchReadyBodyConfig());
	}

	private ExtendedReadBodyPredicateFactory.Config alwaysMatchReadyBodyConfig() {
		ExtendedReadBodyPredicateFactory.Config config = new ExtendedReadBodyPredicateFactory.Config();
		config.setInClass(String.class);
		config.setPredicate(alwaysMatches());
		return config;
	}

	@SuppressWarnings("rawtypes")
	private Predicate alwaysMatches() {
		//See https://stackoverflow.com/questions/26549659/built-in-java-8-predicate-that-always-returns-true
		return x -> true;
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
