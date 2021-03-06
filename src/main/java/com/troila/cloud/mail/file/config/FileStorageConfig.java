package com.troila.cloud.mail.file.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.troila.cloud.mail.file.config.settings.StorageSettings;
import com.troila.cloud.mail.file.config.settings.SystemFileWriteMode;
import com.troila.cloud.mail.file.interceptor.impl.FileServiceInterceptorImpl;
import com.troila.cloud.mail.file.proxy.FileServiceBuilderProxy;
import com.troila.cloud.mail.file.service.FileService;
import com.troila.cloud.mail.file.service.impl.ceph.FileServiceCephImpl;
import com.troila.cloud.mail.file.service.impl.system.FileServiceSystemImpl;

@Configuration
@EnableConfigurationProperties(value= {StorageSettings.class,SystemFileWriteMode.class})
public class FileStorageConfig {
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private StorageSettings storageSettings;
	@Autowired
	private FileServiceInterceptorImpl fileServiceInterceptorImpl;
	
	private static final String CEPH = "ceph";
	
	private static final String SYSTEM = "system";
	
	

	@Bean
	public FileService instanceFileService() {
		if(storageSettings.getPlace() != null) {
			if(storageSettings.getPlace().equals(CEPH)) {
				logger.info("文件存储位置==>ceph");
				return FileServiceBuilderProxy.createFileService(FileServiceCephImpl.class, fileServiceInterceptorImpl);
			}
			if(storageSettings.getPlace().equals(SYSTEM)) {
				logger.info("文件存储位置==>system");
				return FileServiceBuilderProxy.createFileService(FileServiceSystemImpl.class, fileServiceInterceptorImpl,storageSettings);
			}
		}
		logger.info("使用默认文件存储位置==>system");
		return FileServiceBuilderProxy.createFileService(FileServiceSystemImpl.class, fileServiceInterceptorImpl,storageSettings);
	}
}
