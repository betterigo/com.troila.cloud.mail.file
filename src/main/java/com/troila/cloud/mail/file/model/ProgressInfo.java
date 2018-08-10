package com.troila.cloud.mail.file.model;

/**
 * 文件上传进度实体类
 * @author haodonglei
 *
 */
public class ProgressInfo {

	private int fid;
	//文件指纹
	private String md5;
	
	private double percent;
	
	private long totalSize;
	
	private long uploadSize;
	
	private double speed;
	
	//单位（毫秒）
	private long usedTime;
	
	//单位（毫秒）
	private long leftTime;

	public String getMd5() {
		return md5;
	}

	public void setMd5(String md5) {
		this.md5 = md5;
	}

	public double getPercent() {
		return percent;
	}

	public void setPercent(double percent) {
		this.percent = percent;
	}

	public long getTotalSize() {
		return totalSize;
	}

	public void setTotalSize(long totalSize) {
		this.totalSize = totalSize;
	}

	public long getUploadSize() {
		return uploadSize;
	}

	public void setUploadSize(long uploadSize) {
		this.uploadSize = uploadSize;
	}

	public double getSpeed() {
		return speed;
	}

	public void setSpeed(double speed) {
		this.speed = speed;
	}

	public long getUsedTime() {
		return usedTime;
	}

	public void setUsedTime(long usedTime) {
		this.usedTime = usedTime;
	}

	public long getLeftTime() {
		return leftTime;
	}

	public void setLeftTime(long leftTime) {
		this.leftTime = leftTime;
	}

	public int getFid() {
		return fid;
	}

	public void setFid(int fid) {
		this.fid = fid;
	}
}
