package com.troila.cloud.mail.file.security.filter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.troila.cloud.mail.file.model.UserInfo;

public class TokenFilter extends OncePerRequestFilter {


	PathMatchingResourcePatternResolver resourceLoader = new PathMatchingResourcePatternResolver();
	//不进行过滤的url pattern
	private List<String> igoreUrls;
	
	private StringRedisTemplate redisTemplate;
	
	private ObjectMapper mapper = new ObjectMapper();
	
	public TokenFilter(List<String> igoreUrls,StringRedisTemplate redisTemplate) {
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
				accessKey = request.getHeader("access_key");
			}
			if(accessKey!=null) {			
				String userInfoStr = redisTemplate.opsForValue().get(accessKey);
				if(userInfoStr == null) {
					response.sendError(402,"无效的access_key");
					return;
				}
				UserInfo user = mapper.readValue(userInfoStr, UserInfo.class);
				if(user == null) {
					response.sendError(402,"无效的access_key");
					return;
				}
				redisTemplate.expire(accessKey, 1, TimeUnit.HOURS);
//				if(request.getSession().getAttribute("user")!=null) {					
					request.getSession().setAttribute("user", user);
					request.getSession().setAttribute("accessKey", accessKey);
//				}
			}else {
				response.sendError(402,"无效的access_key");
				return;
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
