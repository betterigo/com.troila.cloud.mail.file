package com.troila.cloud.mail.file.model;

public class PartInfo {
	private int partIndex;
	
	private String range;
	
	private long start;
	
	private long end;
	
	private boolean isComplete = false;

	public PartInfo(int partIndex, String range) {
		super();
		this.partIndex = partIndex;
		this.range = range;
		String subRange = range.substring(6, range.length());
		String[] ranges = subRange.split("-");
		this.start = Long.parseLong(ranges[0]);
		this.end = Long.parseLong(ranges[1]);
	}

	public int getPartIndex() {
		return partIndex;
	}

	public void setPartIndex(int partIndex) {
		this.partIndex = partIndex;
	}

	public String getRange() {
		return range;
	}

	public void setRange(String range) {
		this.range = range;
	}

	public boolean isComplete() {
		return isComplete;
	}

	public void setComplete(boolean isComplete) {
		this.isComplete = isComplete;
	}

	public long getStart() {
		return start;
	}

	public long getEnd() {
		return end;
	}
	
}
