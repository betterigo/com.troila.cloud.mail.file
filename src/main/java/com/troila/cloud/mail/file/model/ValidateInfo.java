package com.troila.cloud.mail.file.model;

public class ValidateInfo {
	private String secretUrl;
	
	private String key;
	
	private String fileName;
	
	private boolean preview;

	public String getSecretUrl() {
		return secretUrl;
	}

	public void setSecretUrl(String secretUrl) {
		this.secretUrl = secretUrl;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public boolean isPreview() {
		return preview;
	}

	public void setPreview(boolean preview) {
		this.preview = preview;
	}
	
}
