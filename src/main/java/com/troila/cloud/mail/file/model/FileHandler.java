package com.troila.cloud.mail.file.model;

import java.io.File;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Map;

public class FileHandler {
	private OutputStream out;
	
	private String md5;
	
	private int currentTotalPart;
	
	private int totalPart;
	
	private File cacheFolder;//临时文件夹
	
	private Map<Integer,File> cacheFiles = new HashMap<>();//临时文件夹中的缓存文件
	
	private int currentPart = -1;
	
	private long starTime = 0;
	
	private long uploadSize = 0;
	
	private double speed;
	
	private RandomAccessFile raf;

	public OutputStream getOut() {
		return out;
	}

	public void setOut(OutputStream out) {
		this.out = out;
	}

	public String getMd5() {
		return md5;
	}

	public void setMd5(String md5) {
		this.md5 = md5;
	}

	public int getCurrentTotalPart() {
		return currentTotalPart;
	}

	public void setCurrentTotalPart(int currentTotalPart) {
		this.currentTotalPart = currentTotalPart;
	}

	public int getTotalPart() {
		return totalPart;
	}

	public void setTotalPart(int totalPart) {
		this.totalPart = totalPart;
	}

	public File getCacheFolder() {
		return cacheFolder;
	}

	public void setCacheFolder(File cacheFolder) {
		this.cacheFolder = cacheFolder;
	}

	public Map<Integer, File> getCacheFiles() {
		return cacheFiles;
	}

	public void setCacheFiles(Map<Integer, File> cacheFiles) {
		this.cacheFiles = cacheFiles;
	}

	public int getCurrentPart() {
		return currentPart;
	}

	public void setCurrentPart(int currentPart) {
		this.currentPart = currentPart;
	}

	public long getStarTime() {
		return starTime;
	}

	public void setStarTime(long starTime) {
		this.starTime = starTime;
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

	public RandomAccessFile getRaf() {
		return raf;
	}

	public void setRaf(RandomAccessFile raf) {
		this.raf = raf;
	}
}
