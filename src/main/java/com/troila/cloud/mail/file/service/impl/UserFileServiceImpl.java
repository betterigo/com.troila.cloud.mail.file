package com.troila.cloud.mail.file.service.impl;

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
import com.troila.cloud.mail.file.model.ExpireBeforeUserFile;
import com.troila.cloud.mail.file.model.UserFile;
import com.troila.cloud.mail.file.repository.UserFileRespository;
import com.troila.cloud.mail.file.service.UserFileService;

@Service
@Transactional
public class UserFileServiceImpl implements UserFileService{

//	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private UserFileRespository userFileRespository;
	
	@Override
	@DecodeContent
	public Page<UserFile> findAll(int uid,int page,int size) {
		Pageable pageable = PageRequest.of(page, size);
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

	@Override
	@DecodeContent
	public Page<UserFile> findByFolderId(int userid, int fid, int page, int size) {
		Pageable pageable = PageRequest.of(page, size);
		return userFileRespository.findByUidAndFolderIdOrderByGmtCreateDesc(userid, fid, pageable);
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
}	
