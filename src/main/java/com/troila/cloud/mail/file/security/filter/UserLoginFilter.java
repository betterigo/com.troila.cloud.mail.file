package com.troila.cloud.mail.file.security.filter;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.RequestMatcher;

import com.troila.cloud.mail.file.security.user.CredentialsInfo;
import com.troila.cloud.mail.file.security.user.LoginFailHandler;
import com.troila.cloud.mail.file.security.user.LoginSuccessHandler;
import com.troila.cloud.mail.file.security.user.TokenTypeAuthenticationToken;
import com.troila.cloud.mail.file.utils.SpringBeanUtil;

public class UserLoginFilter extends AbstractAuthenticationProcessingFilter{

	public UserLoginFilter(RequestMatcher requiresAuthenticationRequestMatcher,AuthenticationManager authenticationManager) {
		super(requiresAuthenticationRequestMatcher);
		setAuthenticationManager(authenticationManager);
		setAuthenticationSuccessHandler(SpringBeanUtil.getBean(LoginSuccessHandler.class));
		setAuthenticationFailureHandler(SpringBeanUtil.getBean(LoginFailHandler.class));
	}

	@Override
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
			throws AuthenticationException, IOException, ServletException {
		String token = request.getParameter("token");
		String remoteAddr = getRemoteAddr(request);
		CredentialsInfo credentailsInfo = new CredentialsInfo();
		credentailsInfo.setRemoteAddr(remoteAddr);
		if(token != null) {
			credentailsInfo.setPassword(token);
			return getAuthenticationManager().authenticate(new TokenTypeAuthenticationToken(null, credentailsInfo));
		}else {
			String username = request.getParameter("username");
			String password = request.getParameter("password");	
			credentailsInfo.setPassword(password);
			return getAuthenticationManager().authenticate(new UsernamePasswordAuthenticationToken(username, credentailsInfo));
		}
	}
	
	private String getRemoteAddr(HttpServletRequest request) {
		String remoteAddr = null;
		if((remoteAddr=request.getHeader("X-Real-IP"))!=null) {
			return remoteAddr;
		}
		remoteAddr = request.getRemoteAddr();
		return remoteAddr;
	}
}
