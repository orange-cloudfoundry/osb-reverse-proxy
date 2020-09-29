package com.orange.oss.osbreverseproxy;

import org.junit.jupiter.api.Test;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@DirtiesContext
@TestPropertySource(properties = {
	"osbreverseproxy.http_proxy.host=localhost",
	"osbreverseproxy.http_proxy.port=3128",
//	"spring.security.user.name=" + SecurityConfigTest.USER,
//	"spring.security.user.password=" + SecurityConfigTest.PASSWORD,
//
//	"osbcmdb.admin.user=" + SecurityConfigTest.ADMIN_USER,
//	"osbcmdb.admin.password=" + SecurityConfigTest.ADMIN_PASSWORD,
	"anotherKey=value"
})

class ApplicationTest {

	@Test
	void webapp_starts() {

	}

}