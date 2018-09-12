package com.troila.cloud.mail.file.security.filter;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.troila.cloud.mail.file.model.UserInfo;

public class TokenFilter extends OncePerRequestFilter {


	PathMatchingResourcePatternResolver resourceLoader = new PathMatchingResourcePatternResolver();
	
	private StringRedisTemplate redisTemplate;
	
	private ObjectMapper mapper = new ObjectMapper();
	
	public TokenFilter(StringRedisTemplate redisTemplate) {
		super();
		this.redisTemplate = redisTemplate;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		String accessKey = null;
		accessKey = request.getParameter("access_key");
		if(accessKey == null) {
			accessKey = request.getHeader("access_key");
		}
		if(accessKey!=null) {
			String userInfoStr = redisTemplate.opsForValue().get(accessKey);
			if(userInfoStr!=null) {
				UserInfo user = mapper.readValue(userInfoStr, UserInfo.class);
				if(user!=null) {
					String currentRemoteAddr = getRemoteAddr(request);
					if(currentRemoteAddr!=null && currentRemoteAddr.equals(user.getRemoteAddr())) {	//判断是否是来自同一个id的请求					
						redisTemplate.expire(accessKey, 1, TimeUnit.HOURS);
						request.getSession().setAttribute("user", user);
						request.getSession().setAttribute("accessKey", accessKey);
						UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(user, null,user.getAuthorities());
						SecurityContextHolder.getContext().setAuthentication(authentication);
					}
				}
			}
		}
		filterChain.doFilter(request,response);
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
