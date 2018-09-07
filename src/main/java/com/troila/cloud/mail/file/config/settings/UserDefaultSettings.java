package com.troila.cloud.mail.file.config.settings;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix="user.default")
public class UserDefaultSettings {

	/*
	 * 单位：MB
	 */
	private int uploadSpeed;

	/*
	 * 单位：MB
	 */
	private int downloadSpeed;

	/*
	 * 单位：GB
	 */
	private int volume;
	

	/*
	 * 单位：GB
	 */
	private int maxFileSize;
	
	/*
	 * 单位：GB
	 */
	private int shareGroupVolume;
	
	private boolean enableAcl;

	public boolean isEnableAcl() {
		return enableAcl;
	}

	public void setEnableAcl(boolean enableAcl) {
		this.enableAcl = enableAcl;
	}

	public int getUploadSpeed() {
		return uploadSpeed;
	}

	public void setUploadSpeed(int uploadSpeed) {
		this.uploadSpeed = uploadSpeed;
	}

	public int getDownloadSpeed() {
		return downloadSpeed;
	}

	public void setDownloadSpeed(int downloadSpeed) {
		this.downloadSpeed = downloadSpeed;
	}

	public int getVolume() {
		return volume;
	}

	public void setVolume(int volume) {
		this.volume = volume;
	}

	public int getMaxFileSize() {
		return maxFileSize;
	}

	public void setMaxFileSize(int maxFileSize) {
		this.maxFileSize = maxFileSize;
	}
	
	public long getUploadSpeedLong() {
		return this.uploadSpeed * 1024 * 1024L;
	}
	public long getDownloadSpeedLong() {
		return this.downloadSpeed * 1024 * 1024L;
	}
	public long getVolumeLong() {
		return this.volume * 1024 *1024 *1024L;
	}
	public long getMaxFileSizeLong() {
		return this.maxFileSize * 1024 *1024 *1024L;
	}

	public long getShareGroupVolumeLong() {
		return shareGroupVolume* 1024 *1024 *1024L;
	}
	public int getShareGroupVolume() {
		return shareGroupVolume;
	}

	public void setShareGroupVolume(int shareGroupVolume) {
		this.shareGroupVolume = shareGroupVolume;
	}
	
}
