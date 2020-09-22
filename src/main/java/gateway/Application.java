package gateway;

import reactor.core.publisher.Hooks;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {

	public static void main(String[] args) {
		Hooks.onOperatorDebug();
		SpringApplication.run(Application.class, args);
	}

//	@Bean
//	public RouteLocator myRoutes(RouteLocatorBuilder builder) {
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

//	@Bean
//	public RouteLocator customRouteLocator(RouteLocatorBuilder builder, SimpleLoggingFilter loggingFilter) {
//		return builder.routes().route(r -> r.cloudFoundryRouteService()
////			.and().header("Host", ".*")
//			.filters(f -> f.requestHeaderToRequestUri("X-CF-Forwarded-Url")
//						  .filter(loggingFilter))
//			.uri("https://example.com")).build();
//	}


}
