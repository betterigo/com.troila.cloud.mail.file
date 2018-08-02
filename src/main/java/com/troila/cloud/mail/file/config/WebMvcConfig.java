package com.troila.cloud.mail.file.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.troila.cloud.mail.file.config.settings.WebCorsSettings;

@Configuration
@EnableConfigurationProperties(value=WebCorsSettings.class)
public class WebMvcConfig implements WebMvcConfigurer{

	@Autowired
	private WebCorsSettings webCorsSettings;
	
	@Override
	public void addCorsMappings(CorsRegistry registry) {
//		WebMvcConfigurer.super.addCorsMappings(registry);
		registry.addMapping("/**")
		.allowCredentials(true)
		.allowedOrigins(webCorsSettings.getAllowedOriginsList())
		.allowedMethods("GET", "HEAD", "POST", "PUT", "PATCH", "DELETE", "OPTIONS", "TRACE");
	}
	
}
