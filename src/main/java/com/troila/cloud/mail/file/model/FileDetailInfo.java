package com.troila.cloud.mail.file.model;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.troila.cloud.mail.file.model.fenum.FileStatus;
import com.troila.cloud.mail.file.model.fenum.FileType;

@Entity
@Table(name = "v_file_detail_info")
public class FileDetailInfo {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	/*
	 * 文件名称
	 */
	private String fileName;

	/*
	 * 文件md5值
	 */
	private String md5;

	/*
	 * 文件大小
	 */
	private long size;

	/*
	 * 文件状态，是否上传成功
	 */
	@Enumerated(EnumType.STRING)
	private FileStatus status;

	private String originalFileName;

	private int baseFid;

	private String suffix;

	@Enumerated(EnumType.STRING)
	private FileType fileType;

	private Date gmtCreate;

	private Date gmtModify;

	private Date gmtDelete;
	@Transient
	private int totalPart;

	@Transient
	private long startTime;

	@Transient
	private boolean bingo;
	
	@Transient
	private String uploadId;

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getMd5() {
		return md5;
	}

	public void setMd5(String md5) {
		this.md5 = md5;
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

	public FileStatus getStatus() {
		return status;
	}

	public void setStatus(FileStatus status) {
		this.status = status;
	}

	public String getOriginalFileName() {
		return originalFileName;
	}

	public void setOriginalFileName(String originalFileName) {
		this.originalFileName = originalFileName;
	}

	public int getBaseFid() {
		return baseFid;
	}

	public void setBaseFid(int baseFid) {
		this.baseFid = baseFid;
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

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getUploadId() {
		return uploadId;
	}

	public void setUploadId(String uploadId) {
		this.uploadId = uploadId;
	}

}
