package com.troila.cloud.mail.file.model;

import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Transient;

import com.troila.cloud.mail.file.model.fenum.FolderType;

/**
 * 文件夹实体类
 */
@Entity
public class Folder {
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private int id;
	
	/*
	 * 此文件夹的父文件夹，顶级目录此值为0
	 */
	private int pid;
	
	/*
	 * 此文件夹的user id
	 */
	private int uid;
	
	/*
	 * 文件夹名称
	 */
	@Column(nullable=false)
	private String name;
	
	/*
	 * 文件夹归类（枚举类型）
	 */
	@Enumerated(EnumType.STRING)
	private FolderType type;
	
	/*
	 * 文件夹是否为空
	 */
	private boolean isEmpty;
	
	/*
	 * 文件夹是否已经被删除
	 */
	private boolean isDeleted;
	
	private Date gmtCreate;
	
	private Date gmtModify;
	
	private Date gmtDelete;
	
	@Transient
	private List<Folder> subFolders;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getPid() {
		return pid;
	}

	public void setPid(int pid) {
		this.pid = pid;
	}

	public int getUid() {
		return uid;
	}

	public void setUid(int uid) {
		this.uid = uid;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public FolderType getType() {
		return type;
	}

	public void setType(FolderType type) {
		this.type = type;
	}

	public boolean isEmpty() {
		return isEmpty;
	}

	public void setEmpty(boolean isEmpty) {
		this.isEmpty = isEmpty;
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

	public List<Folder> getSubFolders() {
		return subFolders;
	}

	public void setSubFolders(List<Folder> subFolders) {
		this.subFolders = subFolders;
	}
	public void addSubFolder(Folder subFolder) {
		this.subFolders.add(subFolder);
	}
}
