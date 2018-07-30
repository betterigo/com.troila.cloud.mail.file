package com.troila.cloud.mail.file.model;

import java.util.ArrayList;
import java.util.List;

public class PrepareUploadResult {
	
	private String uploadId;
	
	private boolean bingo;
	
	private List<PartInfo> needUploadParts = new ArrayList<>();
	
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
}
