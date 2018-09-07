package com.troila.cloud.mail.file.model;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.troila.cloud.mail.file.model.fenum.AccessList;
import com.troila.cloud.mail.file.model.fenum.FolderAuth;
import com.troila.cloud.mail.file.model.fenum.FolderType;

@Entity
@Table(name="v_user_folder_detail")
public class UserFolderDetail {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	
	private int folderId;
	
	private int uid;
	
	private int pid;
	
	@Enumerated(EnumType.STRING)
	private FolderAuth auth;
	
	private String name;
	
	@Enumerated(EnumType.STRING)
	private AccessList acl;
	
	private int owner;
	
	@Enumerated(EnumType.STRING)
	private FolderType type;
	
	private Date gmtCreate;

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

	public int getUid() {
		return uid;
	}

	public void setUid(int uid) {
		this.uid = uid;
	}

	public int getPid() {
		return pid;
	}

	public void setPid(int pid) {
		this.pid = pid;
	}

	public FolderAuth getAuth() {
		return auth;
	}

	public void setAuth(FolderAuth auth) {
		this.auth = auth;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public AccessList getAcl() {
		return acl;
	}

	public void setAcl(AccessList acl) {
		this.acl = acl;
	}

	public Date getGmtCreate() {
		return gmtCreate;
	}

	public void setGmtCreate(Date gmtCreate) {
		this.gmtCreate = gmtCreate;
	}

	public int getOwner() {
		return owner;
	}

	public void setOwner(int owner) {
		this.owner = owner;
	}

	public FolderType getType() {
		return type;
	}

	public void setType(FolderType type) {
		this.type = type;
	}
}
