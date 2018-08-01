package com.troila.cloud.mail.file.config.settings;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix="security")
public class SecuritySettings {

	private String tokenUrl;

	public String getTokenUrl() {
		return tokenUrl;
	}

	public void setTokenUrl(String tokenUrl) {
		this.tokenUrl = tokenUrl;
	}
	
}
