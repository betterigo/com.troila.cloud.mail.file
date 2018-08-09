package com.troila.cloud.mail.file.model;

import java.util.Date;

public class PreviewFile {
	private String tmpDir;
	
	private String tempFile;
	
	private Date expiredTime;

	public String getTmpDir() {
		return tmpDir;
	}

	public void setTmpDir(String tmpDir) {
		this.tmpDir = tmpDir;
	}

	public String getTempFile() {
		return tempFile;
	}

	public void setTempFile(String tempFile) {
		this.tempFile = tempFile;
	}

	public Date getExpiredTime() {
		return expiredTime;
	}

	public void setExpiredTime(Date expiredTime) {
		this.expiredTime = expiredTime;
	}
	public boolean isExpired() {
		return System.currentTimeMillis()>this.expiredTime.getTime();
	}
}
