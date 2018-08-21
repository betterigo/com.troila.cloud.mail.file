package com.troila.cloud.mail.file.schedule;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AbortMultipartUploadRequest;
import com.amazonaws.services.s3.model.InitiateMultipartUploadResult;
import com.troila.cloud.mail.file.config.settings.StorageSettings;
import com.troila.cloud.mail.file.utils.InformationStores;

@Component
public class FileInfoCleanSchedule {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private StorageSettings storageSettings;
	
	@Autowired
	private AmazonS3 s3;
	/**
	 * 定时清理fileInfo，每5分钟一次
	 */
	@Scheduled(cron="0 0/5 * * * ?")
	public void cleanFileInfo() {
		int before = InformationStores.getFileInfosStore().size();
		InformationStores.getFileInfosStore().entrySet().stream().forEach(entity->{
			if(entity.getValue().isExpired()) {
				String uploadId = entity.getKey();
				if(storageSettings.getPlace().equals("ceph")) {
					InitiateMultipartUploadResult cephUploadRequest = InformationStores.getCephStore().get(uploadId);
					if(cephUploadRequest!=null) {						
						s3.abortMultipartUpload(new AbortMultipartUploadRequest(entity.getValue().getBucket(), entity.getValue().getFileName(), cephUploadRequest.getUploadId()));
					}
					logger.info("清理未完成的已经过期的文件上传记录：文件名：{},uploadID:{}",entity.getValue().getOriginalFileName(),entity.getValue().getUploadId());
				}
				if(storageSettings.getPlace().equals("system")) {
					File file = new File(new File(storageSettings.getRootpath()+File.separatorChar+entity.getValue().getBucket()),entity.getValue().getFileName());
					file.delete();
					logger.info("清理未完成的已经过期的文件上传记录：文件名：{},uploadID:{}",entity.getValue().getOriginalFileName(),entity.getValue().getUploadId());
				}
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
	@Scheduled(cron="0 0/2 * * * ?")
	public void cleanExpiredFile() {
		File root = getClassPath();
		int before = InformationStores.getPreviewFileStore().size();
		InformationStores.getPreviewFileStore().entrySet().stream().forEach(entity->{
			if(entity.getValue().isExpired()) {
				File tmpDir = new File(root,entity.getValue().getTmpDir());
				if(tmpDir.exists()) {
					try {
						FileUtils.deleteDirectory(tmpDir);
					} catch (IOException e) {
						logger.error("删除预览缓存文件失败！",e);
					}
				}
				InformationStores.getPreviewFileStore().remove(entity.getKey());
			}
		});
		int record = 0;
		if((record = before - InformationStores.getPreviewFileStore().size()) > 0) {
			logger.info("清理{}条已经过期的文件预览记录",record);
		}
	}
	
	private File getClassPath() {
		//获取跟目录
		File path = null;
		try {
			path = new File(ResourceUtils.getURL("classpath:").getPath());
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return path;
	}
}
