package com.troila.cloud.mail.file.security.user;

import org.springframework.security.core.GrantedAuthority;

public class UserGrantedAuthority implements GrantedAuthority{

	/**
	 * 
	 */
	private static final long serialVersionUID = 8213609196760527920L;
	private String authority;
	
	
	
	public UserGrantedAuthority() {
		super();
	}



	public UserGrantedAuthority(String authority) {
		super();
		this.authority = authority;
	}

	public void setAuthority(String authority) {
		this.authority = authority;
	}


	@Override
	public String getAuthority() {
		return this.authority;
	}

}
