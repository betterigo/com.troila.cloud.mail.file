package com.troila.cloud.mail.file.model;

import java.util.concurrent.TimeUnit;

public class FileDelay {
	
	private int fid;
	
	private int time;
	
	private TimeUnit unit;

	public int getFid() {
		return fid;
	}

	public void setFid(int fid) {
		this.fid = fid;
	}

	public int getTime() {
		return time;
	}

	public void setTime(int time) {
		this.time = time;
	}

	public TimeUnit getUnit() {
		return unit;
	}

	public void setUnit(TimeUnit unit) {
		this.unit = unit;
	}
	
	
}
