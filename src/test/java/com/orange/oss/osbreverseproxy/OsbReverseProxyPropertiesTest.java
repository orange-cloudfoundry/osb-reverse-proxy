package com.orange.oss.osbreverseproxy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.logging.ConditionEvaluationReportLoggingListener;
import org.springframework.boot.logging.LogLevel;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

class OsbReverseProxyPropertiesTest {


	//See https://docs.spring.io/spring-boot/docs/2.1.12.RELEASE/reference/html/boot-features-developing-auto-configuration.html#boot-features-test-autoconfig
	ConditionEvaluationReportLoggingListener conditionEvaluationReportLoggingListener = new ConditionEvaluationReportLoggingListener(
		LogLevel.INFO);

	private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
		.withInitializer(conditionEvaluationReportLoggingListener)
		.withConfiguration(AutoConfigurations.of(ReverseProxyRoutesConfiguration.class))
		.withUserConfiguration(InjectMockBeansConfiguration.class);


	@Configuration
	public static class InjectMockBeansConfiguration {

//		@Bean
//		public CloudFoundryTargetProperties targetProperties() {
//			return Mockito.mock(CloudFoundryTargetProperties.class, Mockito.RETURNS_SMART_NULLS);
//		}
	}

	@Test
	void fails_when_no_broker_declared() {
		this.contextRunner
			.withPropertyValues(
				"foo=bar"
			)
			.run((context) -> assertThat(context).hasFailed());
	}

	@Test
	void loads_broker_uri() {
		this.contextRunner
			.withPropertyValues(
				"osbreverseproxy.backendBrokerUri=https://remote_broker:443/prefix",
				"osbreverseproxy.httpProxyHost=localhost",
				"osbreverseproxy.httpProxyPort=3128"
			)
			.withPropertyValues(requiredProperties())
			.run((context) -> {
				assertThat(context).hasSingleBean(OsbReverseProxyProperties.class);
				OsbReverseProxyProperties osbReverseProxyProperties = context.getBean(OsbReverseProxyProperties.class);
				assertThat(osbReverseProxyProperties.getBackendBrokerUri()).isEqualTo(
					"https://remote_broker:443/prefix");
			});
	}


	private String[] requiredProperties() {
		return new String[] {
			//some required properties
//			"foo=bar"
		};
	}


}