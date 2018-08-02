package com.troila.cloud.mail.file.security.user;

import java.util.Collection;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

public class TokenTypeAuthenticationToken extends AbstractAuthenticationToken{

	
	/**
	 * 
	 */
	private static final long serialVersionUID = -8178145143012000024L;
	private Object principal;
	private Object credentials;
	
	public TokenTypeAuthenticationToken(Collection<? extends GrantedAuthority> authorities) {
		super(authorities);
	}

	
	public TokenTypeAuthenticationToken(Collection<? extends GrantedAuthority> authorities, Object principal,
			Object credentials) {
		super(authorities);
		this.principal = principal;
		this.credentials = credentials;
		setAuthenticated(true);
	}


	public TokenTypeAuthenticationToken( Object principal,Object credentials) {
		super(null);
		this.principal = principal;
		this.credentials = credentials;
		setAuthenticated(false);
	}


	@Override
	public Object getCredentials() {
		return credentials;
	}

	@Override
	public Object getPrincipal() {
		return principal;
	}

}
