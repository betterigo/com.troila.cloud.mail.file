package com.troila.cloud.mail.file.interceptor.impl;

import java.io.File;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.amazonaws.services.s3.AmazonS3;
import com.troila.cloud.mail.file.config.settings.StorageSettings;
import com.troila.cloud.mail.file.interceptor.FileServiceInterceptor;
import com.troila.cloud.mail.file.model.FileDetailInfo;
import com.troila.cloud.mail.file.model.FileInfo;
import com.troila.cloud.mail.file.model.FolderFile;
import com.troila.cloud.mail.file.model.fenum.FileStatus;
import com.troila.cloud.mail.file.repository.FileInfoRepository;
import com.troila.cloud.mail.file.service.FolderFileService;

@Component
public class FileServiceInterceptorImpl implements FileServiceInterceptor{

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private FileInfoRepository fileInfoRepository;
	
//	@Autowired
//	private FileInfoExtRepository fileInfoExtRepository;

	@Autowired
	private FolderFileService folderFileService;
	
	@Autowired
	private AmazonS3 s3;
	
	@Autowired
	private StorageSettings storageSettings;
	
	
	@Override
	public void beforeUpload(FileDetailInfo fileDetailInfo) {
		fileDetailInfo.refreshExpiredTime();//刷新1分钟超时
	}

	public FileServiceInterceptorImpl() {
		super();
	}

	@Override
	public void afterUpload(FileDetailInfo fileDetailInfo) {
		
		if(fileDetailInfo.isComplete()) {
			//查询是否已经存在此文件了
			List<FileInfo> existFiles = fileInfoRepository.findByMd5(fileDetailInfo.getMd5());
			FileInfo existFile = null;
			if(existFiles.isEmpty()) {					
				existFile = new FileInfo();
				existFile.setFileName(fileDetailInfo.getFileName());
				existFile.setMd5(fileDetailInfo.getMd5());
				existFile.setSize(fileDetailInfo.getSize());
				existFile = saveFileInfo(existFile);
			}else {
				//删除上传的文件
				existFile = existFiles.get(0);
				deleteFile(fileDetailInfo);
				logger.info("md5值为:{}的文件在存储端已经存在,删除本次上传的文件{}",existFile.getMd5(),fileDetailInfo.getFileName());
			}
			//还需要保存一份ext的
			fileDetailInfo.setBaseFid(existFile.getId());
			fileDetailInfo.setStatus(FileStatus.SUCCESS);
//			FileInfoExt fileInfoExt = new FileInfoExt();
//			fileInfoExt.setBaseFid(existFile.getId());
//			fileInfoExt.setOriginalFileName(fileDetailInfo.getOriginalFileName());
//			fileInfoExt.setSuffix(fileDetailInfo.getSuffix());
//			fileInfoExt.setFileType(fileDetailInfo.getFileType());
//			fileInfoExt.setAcl(fileDetailInfo.getAcl());
//			fileInfoExt.setGmtExpired(fileDetailInfo.getGmtExpired());
//			fileInfoExt = saveInfoExt(fileInfoExt);
//			fileDetailInfo.setId(fileInfoExt.getId());
			FolderFile newFolderFile = folderFileService.complateUpload(fileDetailInfo);
			logger.info("文件【{}】上传完毕！存储端编号为:{},文件夹:{},状态:{},类型:{}",fileDetailInfo.getOriginalFileName(),fileDetailInfo.getFileName(),newFolderFile.getFolderId(),existFile.getStatus(),fileDetailInfo.getFileType());
		}
		
	}
	/*
	 * 上传成功后保存文件信息
	 * @param fileInfo
	 * @return
	 */
	private FileInfo saveFileInfo(FileInfo fileInfo) {
		
		fileInfo.setGmtCreate(new Date());
		fileInfo.setStatus(FileStatus.SUCCESS);
		return fileInfoRepository.save(fileInfo);
	}
	
//	private FileInfoExt saveInfoExt(FileInfoExt fileInfoExt) {
//		FileInfoExt temp = new FileInfoExt();
//		temp.setBaseFid(fileInfoExt.getBaseFid());
//		temp.setSuffix(fileInfoExt.getSuffix());
//		temp.setOriginalFileName(fileInfoExt.getOriginalFileName());
//		temp.setFileType(fileInfoExt.getFileType());
//		temp.setAcl(fileInfoExt.getAcl());
//		temp.setGmtExpired(fileInfoExt.getGmtExpired());
//		temp.setGmtCreate(new Date());
//		return fileInfoExtRepository.save(temp);
//	}
//	
	private void deleteFile(FileDetailInfo fileDetailInfo) {
		
		if(storageSettings.getPlace() != null) {
			if(storageSettings.getPlace().equals("ceph")) {
				s3.deleteObject(fileDetailInfo.getBucket(), fileDetailInfo.getFileName());
			}
			if(storageSettings.getPlace().equals("system")) {
				File file = new File(new File(storageSettings.getRootpath()+File.separatorChar+fileDetailInfo.getBucket()),fileDetailInfo.getFileName());
				file.delete();
			}
		}
	}

}
