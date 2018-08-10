package com.troila.cloud.mail.file.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.troila.cloud.mail.file.interceptor.RequestLoggerInterceptor;
@Configuration
public class WebMvcConfig implements WebMvcConfigurer{
	
	@Autowired
	private RequestLoggerInterceptor requestLoggerInterceptor;
	
	@Override
	public void addCorsMappings(CorsRegistry registry) {
//		WebMvcConfigurer.super.addCorsMappings(registry);
		registry.addMapping("/**")
		.allowCredentials(true)
		.allowedOrigins("/**")
		.allowedMethods("GET", "HEAD", "POST", "PUT", "PATCH", "DELETE", "OPTIONS", "TRACE");
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(requestLoggerInterceptor);
		WebMvcConfigurer.super.addInterceptors(registry);
	}
	
}
