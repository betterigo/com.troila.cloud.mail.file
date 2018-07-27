package com.troila.cloud.mail.file.service.impl.ceph;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AbortMultipartUploadRequest;
import com.amazonaws.services.s3.model.CompleteMultipartUploadRequest;
import com.amazonaws.services.s3.model.CompleteMultipartUploadResult;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.InitiateMultipartUploadRequest;
import com.amazonaws.services.s3.model.InitiateMultipartUploadResult;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PartETag;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.s3.model.UploadPartRequest;
import com.amazonaws.services.s3.model.UploadPartResult;
import com.troila.cloud.mail.file.model.FileDetailInfo;
import com.troila.cloud.mail.file.model.FileInfo;
import com.troila.cloud.mail.file.model.FileInfoExt;
import com.troila.cloud.mail.file.model.ProgressInfo;
import com.troila.cloud.mail.file.model.fenum.FileStatus;
import com.troila.cloud.mail.file.repository.FileDetailInfoRepositoty;
import com.troila.cloud.mail.file.repository.FileInfoExtRepository;
import com.troila.cloud.mail.file.repository.FileInfoRepository;
import com.troila.cloud.mail.file.service.FileService;
import com.troila.cloud.mail.file.utils.FileTypeUtil;

/**
 * CEPH文件服务实现类，提供文件服务
 * @author haodonglei
 *
 */
public class FileServiceCephImpl implements FileService{

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private static Map<String, InitiateMultipartUploadResult> cephStore = new ConcurrentHashMap<>();
	
	private static Map<String, Integer> partStore = new ConcurrentHashMap<>();
	
	private static Map<String, List<PartETag>> eTagtStore = new ConcurrentHashMap<>();
	
	private static Map<String, ProgressInfo> progressStore = new ConcurrentHashMap<>();
	
	@Autowired
	private AmazonS3 s3;
	
	@Autowired
	private FileInfoRepository fileInfoRepository;
	
	@Autowired
	private FileDetailInfoRepositoty fileDetailInfoRepositoty;
	
	@Autowired
	private FileInfoExtRepository fileInfoExtRepository;
	
	@Override
	public FileInfo upload(File file) {
		logger.info("bucket:mailcloud.test\t【添加文件】\t{}",file.getName());
		s3.putObject("mailcloud.test", file.getName(), file);
		return null;
	}

	@Override
	public FileInfo updateFileInfo(FileInfo fileInfo) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean deleteFile(int fid) {
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	public FileInfo upload(InputStream in, FileInfo fileInfo) {
		logger.info("bucket:mailcloud.test\t【添加文件】\t{}",fileInfo.getFileName());
		ObjectMetadata matadata = new ObjectMetadata();
		matadata.setContentLength(fileInfo.getSize());
		s3.putObject("mailcloud.test", fileInfo.getFileName(),in,matadata);
		return null;
	}

	@Override
	public ProgressInfo uploadPart(InputStream in, int index, FileDetailInfo fileInfo,long size) {
		InitiateMultipartUploadResult result = null;
		List<PartETag> partETagList = null;
		String md5 = fileInfo.getMd5();
		String uploadId = fileInfo.getUploadId();
		CompleteMultipartUploadRequest completeMultipartUploadRequest = null;
		// 初始化s3分片上传
		if (cephStore.get(uploadId) == null) {
			fileInfo.setStartTime(System.currentTimeMillis());
			logger.info("初始化文件{}分片上传...",fileInfo.getOriginalFileName());
			InitiateMultipartUploadRequest init = new InitiateMultipartUploadRequest("mailcloud.test", fileInfo.getFileName());
			result = s3.initiateMultipartUpload(init);
			cephStore.put(uploadId, result);
			logger.info("初始化文件{}分片上传...完毕！上传ID为:{}",fileInfo.getOriginalFileName(),result.getUploadId());
		}else {
			result = cephStore.get(uploadId);
		}
		
		if(partStore.get(uploadId)==null) {			
			partStore.put(uploadId, 1);//已经上传了一块文件
			partETagList = new ArrayList<>();
			eTagtStore.put(result.getUploadId(), partETagList);
		}else {
			partETagList = eTagtStore.get(result.getUploadId());
		}
		UploadPartRequest req = new UploadPartRequest();
		req.setBucketName("mailcloud.test");
		req.setKey(fileInfo.getFileName());
		req.setUploadId(result.getUploadId());
		req.setPartNumber(index + 1);
		req.setInputStream(in);
		req.setPartSize(size);
		int parts = partStore.get(uploadId);
		if(parts == fileInfo.getTotalPart()) {
			completeMultipartUploadRequest = new CompleteMultipartUploadRequest();
			req.setLastPart(true);
			partStore.remove(uploadId);
		}else {
			parts++;
			partStore.put(uploadId, parts);
		}
		try {			
			UploadPartResult uploadPartResult = s3.uploadPart(req);
			logger.info("上传ID【{}】:已经上传文件【{}】编号为【{}】的分块,大小:{}",fileInfo.getUploadId(),fileInfo.getOriginalFileName(),index,size);
			partETagList.add(uploadPartResult.getPartETag());
			if(completeMultipartUploadRequest != null) {			
				completeMultipartUploadRequest.setBucketName("mailcloud.test");
				completeMultipartUploadRequest.setKey(fileInfo.getFileName());
				completeMultipartUploadRequest.setUploadId(result.getUploadId());
				completeMultipartUploadRequest.setPartETags(partETagList);
				CompleteMultipartUploadResult completeResult = s3.completeMultipartUpload(completeMultipartUploadRequest);
				//查询是否已经存在此文件了
				List<FileInfo> existFiles = fileInfoRepository.findByMd5(md5);
				FileInfo existFile = null;
				if(existFiles.isEmpty()) {					
					existFile = new FileInfo();
					existFile.setFileName(fileInfo.getFileName());
					existFile.setMd5(fileInfo.getMd5());
					existFile.setSize(fileInfo.getSize());
					existFile = saveFileInfo(existFile);
				}else {
					//删除上传的文件
					existFile = existFiles.get(0);
					s3.deleteObject(completeResult.getBucketName(), completeResult.getKey());
					logger.info("md5值为:{}的文件在存储端已经存在,删除本次上传的文件{}",existFile.getMd5(),fileInfo.getFileName());
				}
				//还需要保存一份ext的
				FileInfoExt fileInfoExt = new FileInfoExt();
				fileInfoExt.setBaseFid(existFile.getId());
				fileInfoExt.setOriginalFileName(fileInfo.getOriginalFileName());
				fileInfoExt.setSuffix(fileInfo.getSuffix());
				fileInfoExt = saveInfoExt(fileInfoExt);
				logger.info("文件【{}】上传完毕！存储端编号为:{},状态:{},类型:{}",fileInfo.getOriginalFileName(),fileInfo.getFileName(),existFile.getStatus(),fileInfoExt.getFileType());
			}
		} catch (Exception e) {
			 s3.abortMultipartUpload(new AbortMultipartUploadRequest("mailcloud.test", fileInfo.getFileName(), result.getUploadId()));
		}
		ProgressInfo progressInfo = null;
		if(progressStore.get(uploadId)==null) {			
			 progressInfo = new ProgressInfo();
			 progressInfo.setMd5(md5);
			 progressInfo.setTotalSize(fileInfo.getSize());
			 progressInfo.setUploadSize(size);
			 progressStore.put(uploadId, progressInfo);
		}else {
			progressInfo = progressStore.get(uploadId);
			progressInfo.setUploadSize(progressInfo.getUploadSize() + size);
		}
		long usedTime = System.currentTimeMillis() - fileInfo.getStartTime();
		progressInfo.setUsedTime(usedTime);
		progressInfo.setSpeed((1000 * progressInfo.getUploadSize()/usedTime)/1024); //KB/S
		progressInfo.setLeftTime((long) ((progressInfo.getTotalSize() - progressInfo.getUploadSize()) / progressInfo.getSpeed()));
		progressInfo.setPercent((double)progressInfo.getUploadSize() / progressInfo.getTotalSize());
		return progressInfo;
	}

	/*
	 * 上传成功后保存文件信息
	 * @param fileInfo
	 * @return
	 */
	private FileInfo saveFileInfo(FileInfo fileInfo) {
		
		fileInfo.setGmtCreate(new Date());
		fileInfo.setStatus(FileStatus.SUCESS);
		return fileInfoRepository.save(fileInfo);
	}

	@Override
	public FileInfo find(String md5) {
		List<FileInfo> fileInfos = fileInfoRepository.findByMd5(md5);
		if(fileInfos==null || fileInfos.isEmpty()) {
			return null;
		}
		return fileInfos.get(0);
	}

	@Override
	public FileInfoExt saveInfoExt(FileInfoExt fileInfoExt) {
		FileInfoExt temp = new FileInfoExt();
		temp.setBaseFid(fileInfoExt.getBaseFid());
		temp.setSuffix(fileInfoExt.getSuffix());
		temp.setOriginalFileName(fileInfoExt.getOriginalFileName());
		temp.setFileType(FileTypeUtil.distinguishFileType(fileInfoExt.getSuffix()));
		temp.setGmtCreate(new Date());
		return fileInfoExtRepository.save(temp);
	}

	@Override
	public InputStream download(FileDetailInfo fileDetailInfo) {
		S3Object file = s3.getObject(new GetObjectRequest("mailcloud.test", fileDetailInfo.getFileName()));
		S3ObjectInputStream sin = file.getObjectContent();
		return sin;
	}

	@Override
	public FileDetailInfo find(int fid) {
		return fileDetailInfoRepositoty.getOne(fid);
	}
}