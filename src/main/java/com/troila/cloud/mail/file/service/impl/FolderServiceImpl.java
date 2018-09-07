package com.troila.cloud.mail.file.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.troila.cloud.mail.file.exception.FolderException;
import com.troila.cloud.mail.file.model.Folder;
import com.troila.cloud.mail.file.model.ShareGroupUserDetail;
import com.troila.cloud.mail.file.model.UserFolder;
import com.troila.cloud.mail.file.model.UserInfo;
import com.troila.cloud.mail.file.model.fenum.AccessList;
import com.troila.cloud.mail.file.model.fenum.FolderAuth;
import com.troila.cloud.mail.file.model.fenum.FolderType;
import com.troila.cloud.mail.file.model.fenum.GroupUserRole;
import com.troila.cloud.mail.file.repository.FolderRepository;
import com.troila.cloud.mail.file.repository.ShareGroupUserDetailRepository;
import com.troila.cloud.mail.file.repository.UserFolderRepository;
import com.troila.cloud.mail.file.service.FolderService;

@Service
@Transactional
public class FolderServiceImpl implements FolderService{

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private FolderRepository folderRepository;
	
	@Autowired
	private UserFolderRepository userFolderRepository;
	
	@Autowired
	private ShareGroupUserDetailRepository shareGroupUserDetailRepository;
	
	@Override
	public Folder create(UserInfo user, String folderName, int pid,AccessList acl,FolderType folderType) throws FolderException {
		List<Folder> userFolders = this.getUserFolders(user,0);
		if(userFolders != null && !userFolders.isEmpty() && pid==0) {
			throw new FolderException("用户只能创建一个根目录文件夹");
		}
		//判断重名
		List<Folder> brothers = this.getUserFolders(user, pid);
		for(Folder f : brothers) {
			if(f.getName().equals(folderName)) {
				throw new FolderException("已经存在文件名为{"+folderName+"}的同级文件夹！") ;
			}
		}
	
		Folder pFolder = folderRepository.findById(pid).orElseThrow(()->new FolderException("没有找到fid为："+pid+"的父目录"));
		Folder folder = new Folder();
		folder.setGmtCreate(new Date());
		folder.setName(folderName);
		folder.setUid(user.getId());
		folder.setPid(pid);
		if(acl == null) {
			folder.setAcl(AccessList.PRIVATE);
		}else {			
			folder.setAcl(acl);
		}
		if(folderType == null) {			
			folder.setType(FolderType.CUSTOM);
		}else {
			folder.setType(folderType);
		}
		//判断是否创建的是共享文件夹中的文件夹
		if(pFolder.getAcl() == AccessList.SHARE) {
			folder.setGid(pFolder.getGid());
			//强制设置为SHARE访问类型
			folder.setAcl(AccessList.SHARE);
			//为每个成员设置一个userFolder对象
			List<ShareGroupUserDetail> users = shareGroupUserDetailRepository.findByGid(pFolder.getGid());
			boolean status = users.stream().anyMatch(u->{
				if(u.getGid() == pFolder.getGid() && u.getUid()==user.getId()) {
					if(u.getUserRole() == GroupUserRole.ADMIN || u.getUserRole() == GroupUserRole.MANAGER) {
						return true;
					}
				}
				return false;
			});
			if(!status) {
				throw new FolderException("您没有权限创建文件夹！");
			}
			Folder result = folderRepository.save(folder);
			for(ShareGroupUserDetail sud : users) {
				UserFolder uf = new UserFolder();
				if(sud.getUserRole() == GroupUserRole.ADMIN || sud.getUserRole() == GroupUserRole.MANAGER) {
					uf.setAuth(FolderAuth.RWMD);
				}else {
					uf.setAuth(FolderAuth.RW);
				}
				uf.setFolderId(result.getId());
				uf.setUid(sud.getUid());
				uf.setGmtCreate(new Date());
				userFolderRepository.save(uf);
			}
			return result;
		}else {			
			Folder result = folderRepository.save(folder);
			//添加userFolder
			UserFolder newUserFolder = new UserFolder();
			newUserFolder.setAuth(FolderAuth.RWMD);
			newUserFolder.setFolderId(result.getId());
			newUserFolder.setUid(result.getUid());
			newUserFolder.setGmtCreate(new Date());
			userFolderRepository.save(newUserFolder);
			return result;
		}
	}

	@Override
	public Folder getFolder(UserInfo user, int fid) {
		Optional<Folder> result = folderRepository.findById(fid);
		if(result.isPresent()) {			
			if(user.getId() != result.get().getUid()) {
				return null;
			}else {
				return result.get();
			}
		}
		return null;
	}

	@Override
	public List<Folder> getUserFolders(UserInfo user,int pid) {
		List<Folder> folders = folderRepository.findByUid(user.getId());
		folders.stream().forEach(f->{
			f.setSubFolders(new ArrayList<>());//重置子文件夹
		});
		Map<Integer, Folder> folderMap = new HashMap<>();
		for(Folder f : folders) {
			folderMap.put(f.getId(), f);
		}
		for(Folder ff : folders) {
			int parentFid = ff.getPid();
			if(parentFid != 0) {//不是顶级目录
				Folder parentFolder = folderMap.get(parentFid);
				parentFolder.addSubFolder(ff);
			}
		}
		List<Folder> result = folders.stream().filter(folder->{
			return folder.getPid() == 0;
		}).collect(Collectors.toList());//只返回顶级目录
		List<Folder> searchResult = getSubFolders(result, pid);
		return searchResult;
	}

	private List<Folder> getSubFolders(List<Folder> folders,int pid){
		if(folders!=null && !folders.isEmpty()) {			
			for(Folder f : folders) {
				if(f.getPid() == pid) {
					return folders;
				}else {
					return getSubFolders(f.getSubFolders(), pid);
				}
			}
		}
		return null;
	}
	
	/**
	 * 删除文件夹。递归删除子文件夹
	 */
	@Override
	public boolean deleteFolder(UserInfo user, int fid) {
		List<Folder> sub = this.getUserFolders(user, fid);
		if(sub == null || sub.isEmpty()) {			
			int result = folderRepository.deleteByIdAndUid(fid, user.getId());
			if(result > 0) {
				logger.info("用户【{}】已经删除文件夹,ID={}",user.getId(),fid);
				return true;
			}
		}else {
			for(Folder f:sub) {
				deleteFolder(user,f.getId());
			}
		}
		return false;
	}

	@Override
	public Folder update(Folder folder) {
		//检查用户
		try {
			Folder old = folderRepository.getOne(folder.getId());
			if(old.getUid()!=folder.getUid()) {
				throw new Exception("非法的用户操作！");
			}
			folder.setGmtModify(new Date());
			Folder result = folderRepository.save(folder);
			logger.info("用户【{}】已经更新文件夹【{}】信息,ID={}",folder.getUid(),folder.getName(),folder.getId());
			return result;
		} catch (Exception e) {
			logger.error("修改文件夹【{}】失败，原因:{}",folder.getId(),e.getMessage(),e);
		}
		return null;
	}

	@Override
	public boolean deleteFolderLogic(UserInfo user, int fid) {
		try {			
			Folder folder = folderRepository.getOne(fid);
			if(folder.getUid() != user.getId()) {
				throw new Exception("非法的用户操作！");
			}
			folder.setDeleted(true);
			folder.setGmtDelete(new Date());
			folderRepository.save(folder);
			return true;
		} catch (Exception e) {
			logger.error("逻辑删除文件夹【{}】失败，原因:{}",fid,e.getMessage(),e);
		}
		return false;
	}

}
