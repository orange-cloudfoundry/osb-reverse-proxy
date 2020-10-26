/*
 * Copyright 2013-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.orange.oss.osbreverseproxy.gateway;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.web.reactive.function.BodyInserters;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

/**
 * POC Adapted from spring-cloud-gateway ModifyRequestBodyGatewayFilterFactoryTests to assert the modify-body* filters
 */
@SpringJUnitConfig
@SpringBootTest(webEnvironment = RANDOM_PORT)
@DirtiesContext
public class ModifyRequestBodyGatewayFilterFactoryTests extends BaseWebClientTests {

	@LocalServerPort
	int port;

	@Test
	public void postWithNonEmptyBodyGetsReturnedUnmodified() {

		testClient.post().uri("/post").header("Host", "www.justcopywithoutchanging.org")
				.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
				.body(BodyInserters.fromValue("request")).exchange().expectStatus().isEqualTo(HttpStatus.OK)
				.expectBody().jsonPath("headers.Content-Type").isEqualTo(MediaType.APPLICATION_JSON_VALUE)
				.jsonPath("data").isEqualTo("request");
		//TODO: invoke actuator/httptrace endpoint to assert the recorded request/response body is present
	}

	@Test
	public void postWithEmptyBodyGetsReturnedUnmodified() {

		testClient.post().uri("/post").header("Host", "www.justcopywithoutchanging.org")
				.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
				.exchange().expectStatus().isEqualTo(HttpStatus.OK)
				.expectBody().jsonPath("headers.Content-Type").isEqualTo(MediaType.APPLICATION_JSON_VALUE)
				.jsonPath("data").isEmpty();
		//TODO: invoke actuator/httptrace endpoint to assert the recorded request/response body is present
	}


	@EnableAutoConfiguration
	@SpringBootConfiguration
	@Import(DefaultTestConfig.class)
	public static class TestConfig {

		@Value("${test.uri:http://httpbin.org:80}")
//		@Value("${test.uri:lb://testservice}") //resolves to the testservice declared using @LoadBalancerService in
		// BaseWebClientTests.DefaultTestConfig
		String uri;

		@Bean
		public RouteLocator testRouteLocator(RouteLocatorBuilder builder) {
			return builder.routes().route("test_modify_request_body",
					r -> r.order(-1).host("**.modifyrequestbody.org").filters(f -> f.modifyRequestBody(String.class,
							String.class, MediaType.APPLICATION_JSON_VALUE, (serverWebExchange, aVoid) -> {
								return Mono.just("modifyrequest");
							})).uri(uri))
					.route("test_modify_request_body_empty",
							r -> r.order(-1).host("**.modifyrequestbodyempty.org")
									.filters(f -> f.modifyRequestBody(String.class, String.class,
											MediaType.APPLICATION_JSON_VALUE, (serverWebExchange, body) -> {
												if (body == null) {
													return Mono.just("modifyrequest");
												}
												return Mono.just(body.toUpperCase());
											}))
									.uri(uri))
					.route("test_modify_request_body_to_large",
							r -> r.order(-1).host("**.modifyrequestbodyemptytolarge.org")
									.filters(f -> f.modifyRequestBody(String.class, String.class,
											MediaType.APPLICATION_JSON_VALUE, (serverWebExchange, body) -> {
												return Mono.just(
														"tolarge-tolarge-tolarge-tolarge-tolarge-tolarge-tolarge-tolarge-tolarge-tolarge-tolarge-tolarge-tolarge-tolarge-tolarge-tolarge");
											}))
									.uri(uri))
					.route("test_modify_request_body_to_large",
							r -> r.order(-1).host("**.justcopywithoutchanging.org")
									.filters(f -> f
										.modifyResponseBody(String.class, String.class,
											(webExchange, originalBody) -> {
												if (originalBody != null) {
													//See https://stackoverflow.com/a/19975149/1484823 for abbreviation
													String abbreviatedBody = StringUtils.abbreviate(originalBody, 10000);
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
													String abbreviatedBody = StringUtils.abbreviate(originalBody, 10000);
													webExchange.getAttributes().put("cachedRequestBodyObject", abbreviatedBody);
													return Mono.just(originalBody);
												} else {
													return Mono.empty();
												}
											})

									)
									.uri(uri))
					.build();
		}

	}

}
