package com.troila.cloud.mail.file.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 * USER用户表实体类 
 * @author haodonglei
 */
@Entity
public class User {
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private int id;
	
	/*
	 * 用户名
	 */
	@Column(nullable=false)
	private String name;
	
	/*
	 * 用户昵称
	 */
	private String nick;
	
	/*
	 * 用户所属
	 */
	private String bucket;
	
	/*
	 * 用户是否被禁用
	 */
	private boolean diable;
	
	/*
	 * 创建时间
	 */
	private Date gmtCreate;
	
	/*
	 * 修改时间
	 */
	private Date gmtModify;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getNick() {
		return nick;
	}

	public void setNick(String nick) {
		this.nick = nick;
	}

	public String getBucket() {
		return bucket;
	}

	public void setBucket(String bucket) {
		this.bucket = bucket;
	}

	public boolean isDiable() {
		return diable;
	}

	public void setDiable(boolean diable) {
		this.diable = diable;
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

	@Override
	public String toString() {
		return "User [id=" + id + ", name=" + name + ", nick=" + nick + ", bucket=" + bucket + ", diable=" + diable
				+ ", gmtCreate=" + gmtCreate + ", gmtModify=" + gmtModify + "]";
	}
	
}
