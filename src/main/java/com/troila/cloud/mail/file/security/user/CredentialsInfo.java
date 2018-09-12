package com.troila.cloud.mail.file.security.user;

import java.io.Serializable;

public class CredentialsInfo implements Serializable{

	private static final long serialVersionUID = 3232719402043941322L;
	
	private String password;
	
	private String remoteAddr;

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getRemoteAddr() {
		return remoteAddr;
	}

	public void setRemoteAddr(String remoteAddr) {
		this.remoteAddr = remoteAddr;
	}
	
}
