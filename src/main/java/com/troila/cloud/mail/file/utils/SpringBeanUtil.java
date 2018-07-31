package com.troila.cloud.mail.file.utils;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class SpringBeanUtil implements ApplicationContextAware{

	private static ApplicationContext applicationContext;
	
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		if(SpringBeanUtil.applicationContext == null) {
			System.out.println("springBeanUtil init");
			SpringBeanUtil.applicationContext = applicationContext;
		}
	}
	
	public static Object getBean(String beanName) {
		return applicationContext.getBean(beanName);
	}
	
	public static <T> T getBean(Class<T> clazz) {
		return applicationContext.getBean(clazz);
	}

	public static <T> T getBean(String beanName, Class<T> clazz) {
		return applicationContext.getBean(beanName,clazz);
	}
}
