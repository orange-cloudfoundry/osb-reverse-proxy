package com.orange.oss.osbreverseproxy;

import javax.validation.constraints.NotNull;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.validation.annotation.Validated;

@Profile("!offline-test")
@ConfigurationProperties(prefix = "osbreverseproxy")
@Validated
public class OsbReverseProxyProperties {

	/**
	 * An optional domain to restrict OSB request routing to (e.g. "internal-controlplane-cf.paas") or null to not
	 * set up domain restriction for OSB api traffic routed.
	 */
	private String whiteListedOsbDomain = null;

	/**
	 * Uri of the backend broker uri to which OSB requests are routed to
	 */
	@NotNull
	private String backendBrokerUri;

	/**
	 * Service provider user granted access to /actuator/httptrace endpoint to access httptraces of last requests for troubleshooting purposes
	 */
	private String serviceProviderUser;

	/**
	 * Service provider user granted access to /actuator/httptrace endpoint to access httptraces of last requests for troubleshooting purposes
	 */
	private String serviceProviderPassword;

	/**
	 * Max number of bytes to keep in http trace captured request and response bodies. Default to 10,000 bytes
	 */
	private int abbreviateHttpTraceLargerThanBytes = 10000;


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

	public String getWhiteListedOsbDomain() { return whiteListedOsbDomain; }
	public void setWhiteListedOsbDomain(String whiteListedOsbDomain) { this.whiteListedOsbDomain = whiteListedOsbDomain; }

	public int getAbbreviateHttpTraceLargerThanBytes() {
		return abbreviateHttpTraceLargerThanBytes;
	}

	public void setAbbreviateHttpTraceLargerThanBytes(int abbreviateHttpTraceLargerThanBytes) {
		this.abbreviateHttpTraceLargerThanBytes = abbreviateHttpTraceLargerThanBytes;
	}

}
