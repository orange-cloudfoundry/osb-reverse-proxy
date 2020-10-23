package com.orange.oss.osbreverseproxy;

import javax.validation.constraints.NotNull;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.validation.annotation.Validated;

@Profile("!offline-test")
@ConfigurationProperties(prefix = "osbreverseproxy")
@Validated
public class OsbReverseProxyProperties {

	@NotNull
	private String backendBrokerUri;

	public String getBackendBrokerUri() {
		return backendBrokerUri;
	}

	public void setBackendBrokerUri(String backendBrokerUri) {
		this.backendBrokerUri = backendBrokerUri;
	}

}
