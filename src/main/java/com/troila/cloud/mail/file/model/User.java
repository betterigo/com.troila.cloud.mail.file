package com.troila.cloud.mail.file.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * USER用户表实体类 
 * @author haodonglei
 */
@Entity
public class User implements Serializable{
	
	private static final long serialVersionUID = 4858547701231815961L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private int id;
	
	/*
	 * 用户名
	 */
	@Column(nullable=false)
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

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
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
