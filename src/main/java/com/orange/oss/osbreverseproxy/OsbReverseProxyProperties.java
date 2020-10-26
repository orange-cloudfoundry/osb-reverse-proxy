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

	private String serviceProviderUser;
	private String serviceProviderPassword;

	public String getBackendBrokerUri() {
		return backendBrokerUri;
	}
	public void setBackendBrokerUri(String backendBrokerUri) {
		this.backendBrokerUri = backendBrokerUri;
	}

	public String getServiceProviderUser() { return serviceProviderUser; }
	public void setServiceProviderUser(String serviceProviderUser) { this.serviceProviderUser = serviceProviderUser; }

	public String getServiceProviderPassword() { return serviceProviderPassword; }
	public void setServiceProviderPassword(String serviceProviderPassword) { this.serviceProviderPassword = serviceProviderPassword; }

}
