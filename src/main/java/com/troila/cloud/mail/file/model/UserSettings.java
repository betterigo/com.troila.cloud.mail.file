package com.troila.cloud.mail.file.model;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class UserSettings {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private int id;
	
	private int uid;
	
	private long volume;
	
	private long used;
	
	private long maxFileSize;
	
	private long downloadSpeedLimit;
	
	private long uploadSpeedLimit;
	
	private boolean vip;
	
	private Date gmtCreate;
	
	private Date gmtModify;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getUid() {
		return uid;
	}

	public void setUid(int uid) {
		this.uid = uid;
	}

	public long getVolume() {
		return volume;
	}

	public void setVolume(long volume) {
		this.volume = volume;
	}

	public long getUsed() {
		return used;
	}

	public void setUsed(long used) {
		this.used = used;
	}

	public long getMaxFileSize() {
		return maxFileSize;
	}

	public void setMaxFileSize(long maxFileSize) {
		this.maxFileSize = maxFileSize;
	}

	public long getDownloadSpeedLimit() {
		return downloadSpeedLimit;
	}

	public void setDownloadSpeedLimit(long downloadSpeedLimit) {
		this.downloadSpeedLimit = downloadSpeedLimit;
	}

	public long getUploadSpeedLimit() {
		return uploadSpeedLimit;
	}

	public void setUploadSpeedLimit(long uploadSpeedLimit) {
		this.uploadSpeedLimit = uploadSpeedLimit;
	}

	public boolean isVip() {
		return vip;
	}

	public void setVip(boolean vip) {
		this.vip = vip;
	}

	public Date getGmtCreate() {
		return gmtCreate;
	}

	public void setGmtCreate(Date gmtCreate) {
		this.gmtCreate = gmtCreate;
	}

	public Date getGmtModify() {
		return gmtModify;
	}

	public void setGmtModify(Date gmtModify) {
		this.gmtModify = gmtModify;
	}
	
}
