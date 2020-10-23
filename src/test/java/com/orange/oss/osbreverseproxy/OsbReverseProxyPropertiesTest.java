package com.orange.oss.osbreverseproxy;

import org.junit.jupiter.api.Test;

import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.logging.ConditionEvaluationReportLoggingListener;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
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
		.withConfiguration(AutoConfigurations.of(ReverseProxyRouteConfiguration.class))
		.withUserConfiguration(LoadPropertiesConfiguration.class)
		.withPropertyValues(
			"spring.profiles.active=offline-test" //don't start spring cloud gateway in this unit test focused on OsbReverseProxyProperties
		);


	@Configuration
	@EnableConfigurationProperties(OsbReverseProxyProperties.class)
	public static class LoadPropertiesConfiguration {

	}

	@Test
	void fails_when_broker_missing() {
		this.contextRunner
			.withPropertyValues(
				"osbreverseproxy.httpProxyHost=localhost",
				"osbreverseproxy.httpProxyPort=3128"
			)
			.run((context) -> assertThat(context).hasFailed());
	}

	@Test
	void fails_when_proxy_missing() {
		this.contextRunner
			.withPropertyValues(
				"osbreverseproxy.backendBrokerUri=https://remote_broker:443/prefix"
			)
			.run((context) -> assertThat(context).hasFailed());
	}

	@Test
	void fails_when_invalid_proxy() {
		this.contextRunner
			.withPropertyValues(
				"osbreverseproxy.backendBrokerUri=https://remote_broker:443/prefix",
				"osbreverseproxy.httpProxyHost=localhost",
				"osbreverseproxy.httpProxyPort=-300"
			)
			.run((context) -> assertThat(context).hasFailed());
	}

	@Test
	void loads_brokeruri_and_proxy() {
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
				assertThat(osbReverseProxyProperties.getHttpProxyHost()).isEqualTo("localhost");
				assertThat(osbReverseProxyProperties.getHttpProxyPort()).isEqualTo(3128);
			});
	}


	private String[] requiredProperties() {
		return new String[] {
			//some required properties
//			"foo=bar"
		};
	}


}