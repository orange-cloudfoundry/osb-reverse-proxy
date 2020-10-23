package com.orange.oss.osbreverseproxy;

import com.orange.oss.osbreverseproxy.actuator.ExtendedHttpTraceWebFilter;

import org.springframework.boot.actuate.autoconfigure.trace.http.HttpTraceProperties;
import org.springframework.boot.actuate.trace.http.HttpExchangeTracer;
import org.springframework.boot.actuate.trace.http.HttpTraceRepository;
import org.springframework.boot.actuate.trace.http.InMemoryHttpTraceRepository;
import org.springframework.boot.actuate.web.trace.reactive.HttpTraceWebFilter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ActuatorConfig {

	/**
	 * Required to activate http tracing, see https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#production-ready-http-tracing
	 */
	@Bean
	public HttpTraceRepository httpTraceRepository() {
		return new InMemoryHttpTraceRepository();
	}


	@Bean
	ExtendedHttpTraceWebFilter httpTraceWebFilter(HttpTraceRepository repository, HttpExchangeTracer tracer,
		HttpTraceProperties traceProperties) {
		return new ExtendedHttpTraceWebFilter(repository, tracer, traceProperties.getInclude());
	}

}
