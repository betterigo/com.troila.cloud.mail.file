package com.troila.cloud.mail.file.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "v_user_info")
public class UserInfo implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -5129406915210078835L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;

	/*
	 * 用户名
	 */
	@Column(nullable = false)
	private String name;

	@JsonIgnore
	private String password;

	/*
	 * 用户昵称
	 */
	private String nick;
	/*
	 * 手机号
	 */
	private String telephone;
	
	/*
	 * 电子邮箱
	 */
	private String email;

	/*
	 * 用户所属
	 */
	private String userCode;

	/*
	 * 用户是否被禁用
	 */
	private boolean disable;

	/*
	 * 创建时间
	 */
	private Date gmtCreate;

	/*
	 * 修改时间
	 */
	private Date gmtModify;
	
	private long volume;

	private long used;

	private long maxFileSize;

	private long downloadSpeedLimit;

	private long uploadSpeedLimit;

	private boolean vip;

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

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getNick() {
		return nick;
	}

	public void setNick(String nick) {
		this.nick = nick;
	}

	public String getUserCode() {
		return userCode;
	}

	public void setUserCode(String userCode) {
		this.userCode = userCode;
	}

	public boolean isDisable() {
		return disable;
	}

	public void setDisable(boolean disable) {
		this.disable = disable;
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

	public long getVolume() {
		return volume;
	}

	public void setVolume(long volume) {
		this.volume = volume;
	}

	public long getUsed() {
		return used;
	}

	public void setUsed(long used) {
		this.used = used;
	}

	public long getMaxFileSize() {
		return maxFileSize;
	}

	public void setMaxFileSize(long maxFileSize) {
		this.maxFileSize = maxFileSize;
	}

	public long getDownloadSpeedLimit() {
		return downloadSpeedLimit;
	}

	public void setDownloadSpeedLimit(long downloadSpeedLimit) {
		this.downloadSpeedLimit = downloadSpeedLimit;
	}

	public long getUploadSpeedLimit() {
		return uploadSpeedLimit;
	}

	public void setUploadSpeedLimit(long uploadSpeedLimit) {
		this.uploadSpeedLimit = uploadSpeedLimit;
	}

	public boolean isVip() {
		return vip;
	}

	public void setVip(boolean vip) {
		this.vip = vip;
	}

	public String getTelephone() {
		return telephone;
	}

	public void setTelephone(String telephone) {
		this.telephone = telephone;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}
	
}
