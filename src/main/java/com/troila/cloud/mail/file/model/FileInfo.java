package com.troila.cloud.mail.file.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.troila.cloud.mail.file.model.fenum.FileStatus;

/**
 * 文件信息实体类
 * @author haodonglei
 *
 */
@Entity
@JsonIgnoreProperties(value = {"handler","hibernateLazyInitializer","fieldHandler"})
public class FileInfo {

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private int id;
	
	/*
	 * 文件名称
	 */
	@Column(nullable=false)
	private String fileName;
	
	/*
	 * 文件md5值
	 */
	@Column(nullable=false)
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
	
	
	private Date gmtCreate;
	
	private Date gmtModify;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

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
	
	public FileStatus getStatus() {
		return status;
	}

	public void setStatus(FileStatus status) {
		this.status = status;
	}
	
}
