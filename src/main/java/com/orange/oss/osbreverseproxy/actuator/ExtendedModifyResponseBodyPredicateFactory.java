package com.orange.oss.osbreverseproxy.actuator;

import java.util.List;
import java.util.Set;

import reactor.core.publisher.Mono;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.rewrite.MessageBodyDecoder;
import org.springframework.cloud.gateway.filter.factory.rewrite.MessageBodyEncoder;
import org.springframework.cloud.gateway.filter.factory.rewrite.ModifyResponseBodyGatewayFilterFactory;
import org.springframework.cloud.gateway.filter.factory.rewrite.RewriteFunction;
import org.springframework.cloud.gateway.handler.predicate.RoutePredicateFactory;
import org.springframework.http.codec.HttpMessageReader;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.stereotype.Component;

@Component
public class ExtendedModifyResponseBodyPredicateFactory extends ModifyResponseBodyGatewayFilterFactory{

	public ExtendedModifyResponseBodyPredicateFactory(
		List<HttpMessageReader<?>> messageReaders,
		Set<MessageBodyDecoder> messageBodyDecoders,
		Set<MessageBodyEncoder> messageBodyEncoders) {
		super(messageReaders, messageBodyDecoders, messageBodyEncoders);
	}

	public ExtendedModifyResponseBodyPredicateFactory() {
		super();
	}

	public ExtendedModifyResponseBodyPredicateFactory(
		ServerCodecConfigurer codecConfigurer) {
		super(codecConfigurer);
	}

	public GatewayFilter apply(ModifyResponseBodyGatewayFilterFactory.Config originalConfig) {
		originalConfig.setInClass(String.class);
		originalConfig.setOutClass(String.class);
		RewriteFunction<String,String> cachedResponseBodyObject = (webExchange, originalResponse) -> {
			webExchange.getAttributes().put("cachedResponseBodyObject", originalResponse);

			return Mono.just(originalResponse);
		};
		originalConfig.setRewriteFunction(cachedResponseBodyObject);
		return super.apply(originalConfig);
	}

	public static class Config {
		//Put the configuration properties for your filter here
	}

}
