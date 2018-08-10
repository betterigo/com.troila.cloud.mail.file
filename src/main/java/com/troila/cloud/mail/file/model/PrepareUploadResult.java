package com.troila.cloud.mail.file.model;

import java.util.ArrayList;
import java.util.List;

public class PrepareUploadResult {
	
	private String uploadId;
	
	private boolean bingo;
	
	private int fid = -1;
	
	private List<PartInfo> needUploadParts = new ArrayList<>();
	
	private int interval;
	
	public String getUploadId() {
		return uploadId;
	}

	public void setUploadId(String uploadId) {
		this.uploadId = uploadId;
	}

	public boolean isBingo() {
		return bingo;
	}

	public void setBingo(boolean bingo) {
		this.bingo = bingo;
	}

	public List<PartInfo> getNeedUploadParts() {
		return needUploadParts;
	}

	public void setNeedUploadParts(List<PartInfo> needUploadParts) {
		this.needUploadParts = needUploadParts;
	}
	public void setNeedPart(PartInfo partInfo) {
		this.needUploadParts.add(partInfo);
	}

	public int getInterval() {
		return interval;
	}

	public void setInterval(int interval) {
		this.interval = interval;
	}

	public int getFid() {
		return fid;
	}

	public void setFid(int fid) {
		this.fid = fid;
	}
}
