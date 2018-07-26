package com.troila.cloud.mail.file.model;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 * 文件夹中的文件实体类
 * 体现文件夹与文件一对多的关系
 * @author haodonglei
 */
@Entity
public class FolderFile {

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private int id;
	
	/*
	 * 文件夹id
	 */
	private int folderId;
	
	/*
	 * 文件id
	 */
	private int fileId;
	
	/*
	 * 此文件是否被删除
	 */
	private boolean isDeleted;
	
	private Date gmtCreate;
	
	private Date gmtModify;
	
	private Date gmtDelete;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getFolderId() {
		return folderId;
	}

	public void setFolderId(int folderId) {
		this.folderId = folderId;
	}

	public int getFileId() {
		return fileId;
	}

	public void setFileId(int fileId) {
		this.fileId = fileId;
	}

	public boolean isDeleted() {
		return isDeleted;
	}

	public void setDeleted(boolean isDeleted) {
		this.isDeleted = isDeleted;
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

	@Override
	public String toString() {
		return "FolderFile [id=" + id + ", folderId=" + folderId + ", fileId=" + fileId + ", isDeleted=" + isDeleted
				+ ", gmtCreate=" + gmtCreate + ", gmtModify=" + gmtModify + ", gmtDelete=" + gmtDelete + "]";
	}
	
}
