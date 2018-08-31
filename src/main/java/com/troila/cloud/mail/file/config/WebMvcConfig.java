package com.troila.cloud.mail.file.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.troila.cloud.mail.file.interceptor.RedisSyncInterceptor;
import com.troila.cloud.mail.file.interceptor.RequestLoggerInterceptor;
@Configuration
public class WebMvcConfig implements WebMvcConfigurer{
	
	@Autowired
	private RequestLoggerInterceptor requestLoggerInterceptor;
	
	@Autowired
	private RedisSyncInterceptor redisSyncInterceptor;

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(requestLoggerInterceptor);
		registry.addInterceptor(redisSyncInterceptor);
		WebMvcConfigurer.super.addInterceptors(registry);
	}
	
}
