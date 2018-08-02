package com.troila.cloud.mail.file.config.settings;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix="web.cors")
public class WebCorsSettings {
	private String allowedOrigins;

	public String getAllowedOrigins() {
		if(allowedOrigins!=null) {
			return allowedOrigins;
		}else {
			return "*";
		}
	}

	public void setAllowedOrigins(String allowedOrigins) {
		this.allowedOrigins = allowedOrigins;
	}
	public String[] getAllowedOriginsList() {
		if(allowedOrigins!=null) {			
			return allowedOrigins.split(";");
		}else {
			return new String[] {"*"};
		}
	}
}
