package com.orange.oss.osbreverseproxy;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "osbreverseproxy")
@Validated
public class OsbReverseProxyProperties {

	@NotNull
	private String backendBrokerUri;
	@NotNull
	private String httpProxyHost;
	@Positive //See https://www.baeldung.com/javax-validation
	private int httpProxyPort;


	public String getBackendBrokerUri() {
		return backendBrokerUri;
	}

	public void setBackendBrokerUri(String backendBrokerUri) {
		this.backendBrokerUri = backendBrokerUri;
	}

	public String getHttpProxyHost() {
		return httpProxyHost;
	}

	public void setHttpProxyHost(String httpProxyHost) {
		this.httpProxyHost = httpProxyHost;
	}

	public int getHttpProxyPort() {
		return httpProxyPort;
	}

	public void setHttpProxyPort(int httpProxyPort) {
		this.httpProxyPort = httpProxyPort;
	}

}
