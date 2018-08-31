package com.troila.cloud.mail.file.schedule;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Optional;

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
import com.troila.cloud.mail.file.model.FolderFile;
import com.troila.cloud.mail.file.model.UserFile;
import com.troila.cloud.mail.file.model.fenum.FileStatus;
import com.troila.cloud.mail.file.repository.FolderFileRepository;
import com.troila.cloud.mail.file.service.UserFileService;
import com.troila.cloud.mail.file.utils.InformationStores;

@Component
public class FileInfoCleanSchedule {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private StorageSettings storageSettings;
	
	@Autowired
	private AmazonS3 s3;
	
	@Autowired
	private UserFileService userFileService;
	
	@Autowired
	private FolderFileRepository folderFileRespository;
	
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
						if(entity.getValue().getStatus()!=FileStatus.SUCCESS) {
							try {
								s3.abortMultipartUpload(new AbortMultipartUploadRequest(entity.getValue().getBucket(), entity.getValue().getFileName(), cephUploadRequest.getUploadId()));
								logger.info("清理未完成的已经过期的文件上传记录：文件名：{},uploadID:{}",entity.getValue().getOriginalFileName(),entity.getValue().getUploadId());
							} catch (Exception e) {
								logger.error("清理清理未完成的已经过期的文件上传记录失败！",e);
							}
						}
					}
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
	 * 定时清理过期的预览文件
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
		//获取根目录
		File path = null;
		try {
			path = new File(ResourceUtils.getURL("classpath:").getPath());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return path;
	}
	
	/**
	 * 定时清理过期的文件(fileInfoExt对象)
	 */
	@Scheduled(cron="0 0/5 * * * ?")
	public void deleteExpiredFiles() {
		List<UserFile> list = userFileService.findExpiredFiles(2);
		Counter counter = new Counter();
		counter.setCount(0);
		list.stream().forEach(userFile->{
			Optional<FolderFile> tmp = folderFileRespository.findById(userFile.getId());
			if(tmp.isPresent()) {
				tmp.get().setDeleted(true);
				tmp.get().setGmtDelete(new Date());
				folderFileRespository.save(tmp.get());
				counter.setCount(counter.getCount()+1);
			}
		});
		if(counter.getCount()>0) {
			logger.info("清理{}个已经过期的文件",counter.getCount());
		}
	}
	
	private static class Counter{
		private int count;

		public int getCount() {
			return count;
		}

		public void setCount(int count) {
			this.count = count;
		}
		
	}
}
