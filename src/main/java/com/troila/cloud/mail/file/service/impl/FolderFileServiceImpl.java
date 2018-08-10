package com.troila.cloud.mail.file.service.impl;

import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.troila.cloud.mail.file.model.FileDetailInfo;
import com.troila.cloud.mail.file.model.FileInfoExt;
import com.troila.cloud.mail.file.model.Folder;
import com.troila.cloud.mail.file.model.FolderFile;
import com.troila.cloud.mail.file.model.ProgressInfo;
import com.troila.cloud.mail.file.model.UserFile;
import com.troila.cloud.mail.file.model.fenum.FolderType;
import com.troila.cloud.mail.file.repository.FileInfoExtRepository;
import com.troila.cloud.mail.file.repository.FolderFileRepository;
import com.troila.cloud.mail.file.repository.FolderRepository;
import com.troila.cloud.mail.file.repository.UserFileRespository;
import com.troila.cloud.mail.file.service.FolderFileService;

@Service
@Transactional
public class FolderFileServiceImpl implements FolderFileService{

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private FolderRepository folderRepository;
	
	@Autowired
	private FolderFileRepository folderFileRepository;
	
	@Autowired
	private FileInfoExtRepository fileInfoExtRepository;
	
	@Autowired
	private UserFileRespository userFileRespository;
	
	@Override
	public FolderFile complateUpload(FileDetailInfo fileDetailInfo) {
		logger.info("正在保存文件的用户信息...");
		//存fileinfoext
		FileInfoExt fileInfoExt = new FileInfoExt();
		fileInfoExt.setBaseFid(fileDetailInfo.getBaseFid());
		fileInfoExt.setOriginalFileName(fileDetailInfo.getOriginalFileName());
		fileInfoExt.setSuffix(fileDetailInfo.getSuffix());
		fileInfoExt.setFileType(fileDetailInfo.getFileType());
		fileInfoExt.setAcl(fileDetailInfo.getAcl());
		fileInfoExt.setGmtCreate(new Date());
		fileInfoExt.setGmtExpired(fileDetailInfo.getGmtExpired());
		fileInfoExt = fileInfoExtRepository.save(fileInfoExt);
		fileDetailInfo.setId(fileInfoExt.getId());
		ProgressInfo p = fileDetailInfo.getProgressInfo();
		if(p == null) {
			p = new ProgressInfo();
			p.setFid(fileDetailInfo.getId());
			p.setLeftTime(0);
			p.setMd5(fileDetailInfo.getMd5());
			p.setPercent(1);
			p.setSpeed(0);
			p.setTotalSize(fileDetailInfo.getSize());
			p.setUploadSize(0);
			p.setUsedTime(0);
			fileDetailInfo.setProgressInfo(p);
		}else {			
			fileDetailInfo.getProgressInfo().setFid(fileDetailInfo.getId());
		}
		//先判断用户是否输出fid
		Folder targetFolder = null;
		if(fileDetailInfo.getFolderId() == 0) {//没有提供folderId，存入默认文件夹
			logger.info("用户【{}】上传的文件【{}】没有提供文件夹信息，此文件将存入默认文件夹！",fileDetailInfo.getUid(),fileDetailInfo.getOriginalFileName());
			List<Folder> folders = folderRepository.findByType(FolderType.DEFAULT);
			if(folders.isEmpty()) {//没有默认文件夹，创建一个默认文件夹
				String defaultName = "default_"+System.currentTimeMillis();
				logger.info("用户【{}】没有提供默认文件夹，创建默认文件夹，名称：{}",fileDetailInfo.getUid(),defaultName);
				Folder folder = new Folder();
				folder.setGmtCreate(new Date());
				folder.setName(defaultName);
				folder.setUid(fileDetailInfo.getUid());
				folder.setPid(0);
				folder.setType(FolderType.DEFAULT);
				targetFolder = folderRepository.save(folder);
			}else {
				targetFolder = folders.get(0);
			}
		}
		FolderFile folderFile = new FolderFile();
		folderFile.setFileId(fileDetailInfo.getId());
		folderFile.setFolderId(targetFolder.getId());
		folderFile.setGmtCreate(new Date());
		FolderFile result = folderFileRepository.save(folderFile);
		logger.info("正在保存文件的用户信息...完毕！用户ID:【{}】,文件夹ID:【{}】,文件ID:【{}】",fileDetailInfo.getUid(),result.getFolderId(),result.getFileId());
		return result;
	}

	/**
	 * 删除文件夹中的文件,此方法不对外公开
	 */
	@Override
	public int deleteFolderFile(int fileId) {
		int result = 0;
		try {
			folderFileRepository.deleteById(fileId);
			result = 1;
		} catch (Exception e) {
			logger.error("文件【{}】删除失败！",fileId,e);
		}
		return result;
	}

	@Override
	public FolderFile updateFolderFile(int uid,FolderFile folderFile) {
		try {
			UserFile userFile = userFileRespository.getOne(folderFile.getId());
			if(userFile.getUid() != uid) {
				throw new Exception("非法的用户操作");
			}
			folderFile.setGmtModify(new Date());
			return folderFileRepository.save(folderFile);
		} catch (Exception e) {
			logger.error("文件【{}】更新失败！",folderFile.getId(),e);
		}
		return null;
	}

	@Override
	public boolean deleteFolderFileLogic(int uid,int fileId) {
		boolean result = false;
		try {
			UserFile userFile = userFileRespository.getOne(fileId);
			if(userFile.getUid() != uid) {
				throw new Exception("非法的用户操作");
			}
			FolderFile folderFile = folderFileRepository.getOne(fileId);
			folderFile.setDeleted(true);
			folderFile.setGmtDelete(new Date());
			folderFileRepository.save(folderFile);
			result = true;
		} catch (Exception e) {
			logger.error("文件【{}】逻辑删除失败！",fileId,e);
		}
		return result;
	}

	@Override
	public FolderFile findOne(int id) {
		return folderFileRepository.getOne(id);
	}

}
