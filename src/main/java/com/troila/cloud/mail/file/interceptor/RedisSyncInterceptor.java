package com.troila.cloud.mail.file.interceptor;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

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
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {
		try {
			if(request.getSession().getAttribute("sync-user")!=null) {
				String accessKey = (String) request.getSession().getAttribute("accessKey");
				UserInfo old = (UserInfo) request.getSession().getAttribute("user");
				if(old!=null) {
					Optional<UserInfo> newUserInfo = userInfoRepository.findById(old.getId());
					if(newUserInfo.isPresent()) {
						redisTemplate.opsForValue().set(accessKey, mapper.writeValueAsString(newUserInfo.get()),1,TimeUnit.HOURS);
					}
				}
			}
			
		} catch (Exception e) {
			return;
		}
	}
	
}
