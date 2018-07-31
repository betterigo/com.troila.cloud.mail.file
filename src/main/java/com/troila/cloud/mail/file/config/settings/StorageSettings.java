package com.troila.cloud.mail.file.config.settings;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix="file.storage")
public class StorageSettings {
	private String place;
	
	private String rootpath;

	public String getPlace() {
		return place;
	}

	public void setPlace(String place) {
		this.place = place;
	}

	public String getRootpath() {
		return rootpath;
	}

	public void setRootpath(String rootpath) {
		this.rootpath = rootpath;
	}
	
}
