package com.troila.cloud.mail.file.model;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Transient;

import com.troila.cloud.mail.file.model.fenum.AccessList;
import com.troila.cloud.mail.file.model.fenum.FileType;

@Entity
public class FileInfoExt {
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private int id;
	
	private String originalFileName;
	
	private int baseFid;
	
	private String suffix;
	
	@Enumerated(EnumType.STRING)
	private FileType fileType;
	
	@Enumerated(EnumType.STRING)
	private AccessList acl;
	
	private Date gmtExpired;
	
	private Date gmtCreate;
	
	private Date gmtModify;
	
	private Date gmtDelete;
	@Transient
	private int totalPart;
	
	@Transient
	private long startTime;
	
	@Transient
	private boolean bingo;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getOriginalFileName() {
		return originalFileName;
	}

	public void setOriginalFileName(String originalFileName) {
		this.originalFileName = originalFileName;
	}

	public String getSuffix() {
		return suffix;
	}

	public void setSuffix(String suffix) {
		this.suffix = suffix;
	}

	public FileType getFileType() {
		return fileType;
	}

	public void setFileType(FileType fileType) {
		this.fileType = fileType;
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

	public Date getGmtDelete() {
		return gmtDelete;
	}

	public void setGmtDelete(Date gmtDelete) {
		this.gmtDelete = gmtDelete;
	}

	public int getBaseFid() {
		return baseFid;
	}

	public void setBaseFid(int baseFid) {
		this.baseFid = baseFid;
	}

	public int getTotalPart() {
		return totalPart;
	}

	public void setTotalPart(int totalPart) {
		this.totalPart = totalPart;
	}

	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public boolean isBingo() {
		return bingo;
	}

	public void setBingo(boolean bingo) {
		this.bingo = bingo;
	}

	public AccessList getAcl() {
		return acl;
	}

	public void setAcl(AccessList acl) {
		this.acl = acl;
	}

	public Date getGmtExpired() {
		return gmtExpired;
	}

	public void setGmtExpired(Date gmtExpired) {
		this.gmtExpired = gmtExpired;
	}
	
}
