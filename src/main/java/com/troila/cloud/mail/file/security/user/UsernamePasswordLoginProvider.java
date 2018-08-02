package com.troila.cloud.mail.file.security.user;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import com.troila.cloud.mail.file.model.User;
import com.troila.cloud.mail.file.repository.UserRepository;

@Component
public class UsernamePasswordLoginProvider implements AuthenticationProvider{

	@Autowired
	private UserRepository userRepository;
	
	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		String username = authentication.getPrincipal().toString();
		String password = authentication.getCredentials().toString();
		password = DigestUtils.md5Hex(password);
		User user = userRepository.findByNameAndPassword(username, password);
		if(user == null) {
			throw new UsernameNotFoundException("用户名或密码错误");
		}else {
			return new UsernamePasswordAuthenticationToken(username, password,getUserAuthorities());
		}
	}

	@Override
	public boolean supports(Class<?> authentication) {
		return authentication.equals(UsernamePasswordAuthenticationToken.class);
	}
	private List<GrantedAuthority> getUserAuthorities(){
		List<GrantedAuthority> authorities = new ArrayList<>();
		return authorities;
	}

}
