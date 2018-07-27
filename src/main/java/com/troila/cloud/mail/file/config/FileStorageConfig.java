package com.troila.cloud.mail.file.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.troila.cloud.mail.file.service.FileService;
import com.troila.cloud.mail.file.service.impl.ceph.FileServiceCephImpl;
import com.troila.cloud.mail.file.service.impl.system.FileServiceSystemImpl;

@Configuration
public class FileStorageConfig {
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Value("${file.storage.place}")
	private String storage="ceph";
	
	private static final String CEPH = "ceph";
	
	private static final String SYSTEM = "system";
	
	@Bean
	public FileService instanceFileService() {
		if(storage != null) {
			if(storage.equals(CEPH)) {
				logger.info("文件存储位置==>ceph");
				return new FileServiceCephImpl();
			}
			if(storage.equals(SYSTEM)) {
				logger.info("文件存储位置==>system");
				return new FileServiceSystemImpl();
			}
		}
		return null;
	}
}
