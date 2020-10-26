package com.orange.oss.osbreverseproxy;

import org.junit.jupiter.api.Test;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@DirtiesContext
@TestPropertySource(properties = {
	"spring.security.user.name=" + SecurityConfigTest.ADMIN_USER,
	"spring.security.user.password=" + SecurityConfigTest.ADMIN_PASSWORD,
	"osbreverseproxy.backendBrokerUri=https://remote_broker:443/prefix",
	//The later two are optional and not enforced
	"spring.cloud.gateway.httpclient.proxy.host=my-http-proxy",
	"spring.cloud.gateway.httpclient.proxy.port=3128"
})

class ApplicationTest {

	@Test
	void webapp_starts() {

	}

}