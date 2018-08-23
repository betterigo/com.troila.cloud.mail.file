package com.troila.cloud.mail.file.model;

import java.io.Serializable;

public class ValidateInfo implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -1518461189402086886L;

	private String secretUrl;
	
	private String key;
	
	private String fileName;
	
	private boolean preview;
	
	private int fid;

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

	public int getFid() {
		return fid;
	}

	public void setFid(int fid) {
		this.fid = fid;
	}
}
