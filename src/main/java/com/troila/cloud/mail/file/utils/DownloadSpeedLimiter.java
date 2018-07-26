package com.troila.cloud.mail.file.utils;

public class DownloadSpeedLimiter {
	
	/**
	 * B
	 */
	private long limiteSpeed;
	
	private long bufferSize;
	
	private long pieces;
	
	private int index = 0;
	
	private long startTime = 0;
	
	private static final long SECOND_PER = 1000;
	
	private long SLEEP_TIME = 200;
	//粒度调节
	private int grade = 5;
	
	
	
	public DownloadSpeedLimiter(long limiteSpeed, long bufferSize) {
		super();
		this.limiteSpeed = limiteSpeed;
		this.bufferSize = bufferSize;
		this.pieces = limiteSpeed / bufferSize / grade;
		this.SLEEP_TIME = SECOND_PER / this.grade;
	}


	public long getLimiteSpeed() {
		return limiteSpeed;
	}


	public void setLimiteSpeed(long limiteSpeed) {
		this.limiteSpeed = limiteSpeed;
	}


	public long getBufferSize() {
		return bufferSize;
	}


	public void setBufferSize(long bufferSize) {
		this.bufferSize = bufferSize;
	}

	public int getGrade() {
		return grade;
	}


	public void setGrade(int grade) {
		this.grade = grade;
	}


	public void limit() {
		if(this.index == 0) {
			this.startTime = System.currentTimeMillis();
		}
		this.index ++;
		if(this.index % this.pieces == 0) {
			this.index = 0;
			long time = System.currentTimeMillis() - this.startTime;
			try {
				long sleepTime = SLEEP_TIME - time;
				if(sleepTime>0) {
					Thread.sleep(sleepTime);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
