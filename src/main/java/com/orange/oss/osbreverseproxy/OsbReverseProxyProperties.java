package com.orange.oss.osbreverseproxy;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "osbreverseproxy")
public class OsbReverseProxyProperties {

	private List<String> backendBrokerUris = new ArrayList<>();

	public List<String> getBackendBrokerUris() {
		return backendBrokerUris;
	}

	public void setBackendBrokerUris(List<String> backendBrokerUris) {
		this.backendBrokerUris = backendBrokerUris;
	}

}
