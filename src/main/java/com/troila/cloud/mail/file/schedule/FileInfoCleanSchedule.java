package com.troila.cloud.mail.file.schedule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.troila.cloud.mail.file.utils.InformationStores;

@Component
public class FileInfoCleanSchedule {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	/**
	 * 定时清理fileInfo，每5分钟一次
	 */
	@Scheduled(cron="0 0/5 * * * ?")
	public void cleanFileInfo() {
		int before = InformationStores.getFileInfosStore().size();
		InformationStores.getFileInfosStore().entrySet().stream().forEach(entity->{
			if(entity.getValue().isExpired()) {
				String uploadId = entity.getKey();
				InformationStores.getFileInfosStore().remove(entity.getKey());
				//清理各个store中的过期内容，因为可能存在上传中断的情况，所以各个store中可能存在垃圾数据，因此需要清理
				if(InformationStores.getCephStore().get(uploadId) == null) {
					InformationStores.getCephStore().remove(uploadId);
				}
				if(InformationStores.geteTagtStore().get(uploadId) == null) {
					InformationStores.geteTagtStore().remove(uploadId);
				}
				if(InformationStores.getFileStore().get(uploadId) == null) {
					InformationStores.getFileStore().remove(uploadId);
				}
				if(InformationStores.getProgressStore().get(uploadId) == null) {
					InformationStores.getProgressStore().remove(uploadId);
				}
			}
		});
		if(before-InformationStores.getFileInfosStore().size()>0) {			
			logger.info("清理{}条已经过期的文件上传记录",before-InformationStores.getFileInfosStore().size());
		}
	}
	
	/**
	 * 定时清理过期的文件（不是实体文件，是指向文件的对象:FileInfoExt）
	 */
	public void cleanExpiredFile() {
		
	}
}
