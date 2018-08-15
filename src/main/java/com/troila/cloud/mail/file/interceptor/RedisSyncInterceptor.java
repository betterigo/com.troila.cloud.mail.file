package com.troila.cloud.mail.file.interceptor;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.troila.cloud.mail.file.model.UserInfo;
import com.troila.cloud.mail.file.repository.UserInfoRepository;

@Component
public class RedisSyncInterceptor implements HandlerInterceptor{


	@Autowired
	private StringRedisTemplate redisTemplate;
	
	@Autowired
	private UserInfoRepository userInfoRepository;
	
	private ObjectMapper mapper = new ObjectMapper();
	
	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
			throws Exception {
		if(response.isCommitted()) {
			return;
		}
		if(request.getSession().getAttribute("sync-user")!=null) {
			String accessKey = (String) request.getSession().getAttribute("accessKey");
			UserInfo old = (UserInfo) request.getSession().getAttribute("user");
			if(old!=null) {
				Optional<UserInfo> newUserInfo = userInfoRepository.findById(old.getId());
				if(newUserInfo.isPresent()) {
					redisTemplate.opsForValue().set(accessKey, mapper.writeValueAsString(newUserInfo),1,TimeUnit.HOURS);
//					System.out.println("已经同步redis用户信息");
				}
			}
		}
	}
	
}
