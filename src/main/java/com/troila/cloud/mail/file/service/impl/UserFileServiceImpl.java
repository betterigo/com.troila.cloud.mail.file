package com.troila.cloud.mail.file.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.ExampleMatcher.GenericPropertyMatchers;
import org.springframework.data.domain.ExampleMatcher.StringMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.troila.cloud.mail.file.component.annotation.DecodeContent;
import com.troila.cloud.mail.file.component.annotation.ValidateFolderAuth;
import com.troila.cloud.mail.file.model.ExpireBeforeUserFile;
import com.troila.cloud.mail.file.model.Folder;
import com.troila.cloud.mail.file.model.UserFile;
import com.troila.cloud.mail.file.model.UserFolder;
import com.troila.cloud.mail.file.model.fenum.FolderAuth;
import com.troila.cloud.mail.file.model.fenum.FolderType;
import com.troila.cloud.mail.file.repository.FolderRepository;
import com.troila.cloud.mail.file.repository.UserFileRespository;
import com.troila.cloud.mail.file.repository.UserFolderRepository;
import com.troila.cloud.mail.file.service.UserFileService;

@Service
@Transactional
public class UserFileServiceImpl implements UserFileService{

//	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private FolderRepository folderRepository;
	
	@Autowired
	private UserFileRespository userFileRespository;
	
	@Autowired
	private UserFolderRepository userFolderRepository;
	
	@Override
	@DecodeContent
	public Page<UserFile> findAll(int uid,int page,int size) {
		Pageable pageable = null;
		if(size>0) {
			pageable = PageRequest.of(page, size);
		}else {
			pageable = Pageable.unpaged();
		}
		
		return userFileRespository.findByUidOrderByGmtCreateDesc(uid, pageable);
	}

	@Override
	@DecodeContent
	public Page<UserFile> search(UserFile userFile, int page, int size) {
		userFile.setDeleted(false);
		ExampleMatcher matcher = ExampleMatcher.matching()
				.withStringMatcher(StringMatcher.CONTAINING)
				.withIgnoreCase(true)
				.withIgnoreNullValues()
				.withMatcher("originalFileName", GenericPropertyMatchers.contains())
//				.withIgnorePaths(ignoredPaths)
				.withMatcher("isDeleted", GenericPropertyMatchers.exact())
				.withMatcher("uid", GenericPropertyMatchers.exact())
				.withIgnorePaths("id","folderId","fileId","folderName","folderType","md5","size","fileName","suffix","fileType","gmtCreate"
						,"gmtModify","gmtDelete","status","acl","downloadTimes","shareTimes","score","gmtExpired");
		Example<UserFile> example = Example.of(userFile, matcher);
		Pageable pageable = PageRequest.of(page, size);
		return userFileRespository.findAll(example, pageable);
	}

	/**
	 * 查询文件夹中的文件
	 */
	@Override
	@DecodeContent
	public Page<UserFile> findByFolderId(int userid, int fid, int page, int size) {
		
		if(fid == 0) {//查询根目录
			List<Folder> root = folderRepository.findByTypeAndUid(FolderType.ROOT, userid);
			fid = root.get(0).getId();
		}
		Optional<UserFolder> userFolder = userFolderRepository.findByUidAndFolderId(userid, fid);
		if(userFolder.isPresent()) {
			return findByFolder(userFolder.get(),userid,page,size);
		}
		return null;
	}

	@ValidateFolderAuth(FolderAuth.READ)
	private  Page<UserFile> findByFolder(UserFolder folder,int userId,int page,int size){
		Pageable pageable = null;
		if(size>0) {
			pageable = PageRequest.of(page, size);
		}else {
			pageable = Pageable.unpaged();
		}
		return userFileRespository.findByUidAndFolderIdOrderByGmtCreateDesc(userId,folder.getFolderId(), pageable);
	}
	
	@Override
	@DecodeContent
	public UserFile findOne(int uid, int id) {
		Optional<UserFile> result = userFileRespository.findById(id);
		if(result.isPresent()) {			
			if(result.get().getUid()==uid) {			
				return result.get();
			}
		}
		return null;
	}
	
	@Override
	@DecodeContent
	public UserFile findOnePublic(int id) {
		Optional<UserFile> result = userFileRespository.findById(id);
		if(result.isPresent()) {
//			if(result.get().getAcl().equals(AccessList.PUBLIC)) {
				return result.get();
//			}
		}
		return null;
//		return result.get();
	}

    @Override
    @DecodeContent
	public ExpireBeforeUserFile findExpireBefores(int expireBeforeDays, int uid) {
    	ExpireBeforeUserFile expireBeforeUserFile = new ExpireBeforeUserFile();
    	expireBeforeUserFile.setExpireBefores(userFileRespository.findExpireBefores(expireBeforeDays, uid));
    	expireBeforeUserFile.setExpireBeforesBefore(userFileRespository.findExpireBeforesBefore(expireBeforeDays, uid));
    	return expireBeforeUserFile;	    
	}

	@Override
	public List<UserFile> findExpiredFiles(int days) {
		
		return userFileRespository.findExpiredFiles(days);
	}
}	
