package com.troila.cloud.mail.file.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
@Configuration
public class WebMvcConfig implements WebMvcConfigurer{
	
	@Override
	public void addCorsMappings(CorsRegistry registry) {
//		WebMvcConfigurer.super.addCorsMappings(registry);
		registry.addMapping("/**")
		.allowCredentials(true)
		.allowedOrigins("/**")
		.allowedMethods("GET", "HEAD", "POST", "PUT", "PATCH", "DELETE", "OPTIONS", "TRACE");
	}
	
}
