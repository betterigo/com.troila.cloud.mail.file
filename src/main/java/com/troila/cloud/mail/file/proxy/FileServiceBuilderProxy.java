package com.troila.cloud.mail.file.proxy;

import org.springframework.cglib.proxy.Enhancer;

import com.troila.cloud.mail.file.config.settings.StorageSettings;
import com.troila.cloud.mail.file.interceptor.FileServiceInterceptor;
import com.troila.cloud.mail.file.service.FileService;

public class FileServiceBuilderProxy {
	public static FileService createFileService(Class<?> clazz,FileServiceInterceptor fileServiceInterceptor) {
		Enhancer enhancer = new Enhancer();
		enhancer.setSuperclass(clazz);
		enhancer.setCallback(new EnhancerInterceptor(fileServiceInterceptor));
		return (FileService) enhancer.create();
	}
	
	public static FileService createFileService(Class<?> clazz,FileServiceInterceptor fileServiceInterceptor,StorageSettings storageSettings) {
		Enhancer enhancer = new Enhancer();
		enhancer.setSuperclass(clazz);
		enhancer.setCallback(new EnhancerInterceptor(fileServiceInterceptor));
		return (FileService) enhancer.create(new Class[] {StorageSettings.class},new Object[] {storageSettings});
	}
}
