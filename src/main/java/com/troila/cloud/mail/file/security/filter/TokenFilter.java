package com.troila.cloud.mail.file.security.filter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.BadRequestException;

import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.filter.OncePerRequestFilter;

import com.troila.cloud.mail.file.model.User;

public class TokenFilter extends OncePerRequestFilter {


	PathMatchingResourcePatternResolver resourceLoader = new PathMatchingResourcePatternResolver();
	//不进行过滤的url pattern
	private List<String> igoreUrls;
	
	private RedisTemplate<Object, Object> redisTemplate;
	
	public TokenFilter(List<String> igoreUrls,RedisTemplate<Object, Object> redisTemplate) {
		super();
		this.igoreUrls = igoreUrls;
		this.redisTemplate = redisTemplate;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		HttpServletRequest httpReq = (HttpServletRequest) request;
		if(matchUri(httpReq.getRequestURI())) {
			filterChain.doFilter(request, response);
		}else {		
			if("OPTIONS".equals(request.getMethod())){//application/json 请求为2次，第一次情趣是options类型的请求
				filterChain.doFilter(request, response);
				return;
			}
			String accessKey = null;
			accessKey = request.getParameter("access_key");
			if(accessKey == null) {				
				Cookie[] cookies = request.getCookies();
				if(cookies!=null) {			
					for(Cookie c:cookies) {
						if(c.getName().equals("access_key")) {
							accessKey = c.getValue();
							break;
						}
					}
				}
			}
			if(accessKey!=null) {				
				User user = (User) redisTemplate.opsForValue().get(accessKey);
				if(user == null) {
					throw new BadRequestException("无效的access_key");
				}
				redisTemplate.expire(accessKey, 1, TimeUnit.HOURS);
				request.getSession().setAttribute("user", user);
			}else {
				throw new BadRequestException("无效的access_key");
			}
			filterChain.doFilter(request,response);
		}
	}

	private boolean matchUri(String url) {
		boolean result;
		
		for(String uri:igoreUrls) {
			result = resourceLoader.getPathMatcher().match(uri, url);
			if(result) {
				return result;
			}
		}
		return false;
	}
}
