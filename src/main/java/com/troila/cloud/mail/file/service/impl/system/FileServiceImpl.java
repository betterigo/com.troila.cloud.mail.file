package com.troila.cloud.mail.file.service.impl.system;

import java.io.File;
import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.amazonaws.services.s3.model.InitiateMultipartUploadResult;
import com.amazonaws.services.s3.model.PartETag;
import com.troila.cloud.mail.file.model.FileDetailInfo;
import com.troila.cloud.mail.file.model.FileInfo;
import com.troila.cloud.mail.file.model.FileInfoExt;
import com.troila.cloud.mail.file.model.ProgressInfo;
import com.troila.cloud.mail.file.repository.FileDetailInfoRepositoty;
import com.troila.cloud.mail.file.repository.FileInfoExtRepository;
import com.troila.cloud.mail.file.repository.FileInfoRepository;
import com.troila.cloud.mail.file.service.FileService;
import com.troila.cloud.mail.file.utils.FileTypeUtil;

public class FileServiceImpl implements FileService {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private static Map<String, InitiateMultipartUploadResult> cephStore = new ConcurrentHashMap<>();
	
	private static Map<String, Integer> partStore = new ConcurrentHashMap<>();
	
	private static Map<String, List<PartETag>> eTagtStore = new ConcurrentHashMap<>();
	
	private static Map<String, ProgressInfo> progressStore = new ConcurrentHashMap<>();
	
	@Autowired
	private FileInfoRepository fileInfoRepository;
	
	@Autowired
	private FileDetailInfoRepositoty fileDetailInfoRepositoty;
	
	@Autowired
	private FileInfoExtRepository fileInfoExtRepository;
	
	@Override
	public FileInfo upload(File file) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public FileInfo upload(InputStream in, FileInfo fileInfo) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public FileInfo updateFileInfo(FileInfo fileInfo) {
		// TODO Auto-generated method stub
		return null;
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
	public FileDetailInfo find(int fid) {
		return fileDetailInfoRepositoty.getOne(fid);
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
	public boolean deleteFile(int fid) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public ProgressInfo uploadPart(InputStream in, int index, FileDetailInfo fileInfo, long size) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public InputStream download(int fid) {
		// TODO Auto-generated method stub
		return null;
	}

}
