package com.troila.cloud.mail.file.model;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.troila.cloud.mail.file.model.fenum.GroupUserRole;

@Entity
@Table(name="v_share_group_user_detail")
public class ShareGroupUserDetail {

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private int id;
	
	private int uid;
	
	private int gid;
	
	@Enumerated(EnumType.STRING)
	private GroupUserRole userRole;
	
	private String name;
	
	private String nick;
	
	private String telephone;
	
	private String email;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getUid() {
		return uid;
	}

	public void setUid(int uid) {
		this.uid = uid;
	}

	public int getGid() {
		return gid;
	}

	public void setGid(int gid) {
		this.gid = gid;
	}

	public GroupUserRole getUserRole() {
		return userRole;
	}

	public void setUserRole(GroupUserRole userRole) {
		this.userRole = userRole;
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
