package com.troila.cloud.mail.file.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpSession;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.PathParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.troila.cloud.mail.file.model.ExpireBeforeUserFile;
import com.troila.cloud.mail.file.model.FileDelay;
import com.troila.cloud.mail.file.model.FileInfoExt;
import com.troila.cloud.mail.file.model.Folder;
import com.troila.cloud.mail.file.model.FolderFile;
import com.troila.cloud.mail.file.model.UserFile;
import com.troila.cloud.mail.file.model.UserInfo;
import com.troila.cloud.mail.file.service.FileService;
import com.troila.cloud.mail.file.service.FolderFileService;
import com.troila.cloud.mail.file.service.FolderService;
import com.troila.cloud.mail.file.service.UserFileService;
import com.troila.cloud.mail.file.utils.RedisValueManager;

/**
 * 用户文件api
 * 主要用来获取用户文件夹中的文件，文件搜索等功能
 * @author haodonglei
 */
@RestController
@RequestMapping("/userfile")
public class UserFileController {

	@Autowired
	private UserFileService userFileService;
	
	@Autowired
	private FolderFileService folderFileService;
	
	@Autowired
	private FileService fileService;
	
	@Autowired
	private FolderService folderService;
	
	/**
	 * 分页查询用户的所有文件
	 * @return
	 */
	@GetMapping("/all")
	public ResponseEntity<Page<UserFile>> getAllFiles(@RequestParam(name = "page",defaultValue = "0",required=false)int page,
			@RequestParam(name = "size",defaultValue="10",required=false)int size,HttpSession session){
		UserInfo user = (UserInfo) session.getAttribute("user");
		Page<UserFile> result = userFileService.findAll(user.getId(), page, size);
		result.stream().forEach(file->{
			setFileNameNull(file);
		});
		return ResponseEntity.ok(result);
		
	}
	
	/**
	 * 查询文件信息
	 * @param session
	 * @return
	 */
	@GetMapping("/detail/{fid}")
	public ResponseEntity<UserFile> getFileInfo(@PathVariable("fid")int fid,HttpSession session){
		UserInfo user = (UserInfo) session.getAttribute("user");
		UserFile result = userFileService.findOne(user.getId(), fid);
		setFileNameNull(result);
		return ResponseEntity.ok(result);
		
	}
	/**
	 * 查询接口
	 * @return
	 */
	@PostMapping("/search")
	public ResponseEntity<Page<UserFile>> search(@RequestBody UserFile userFile,HttpSession session,
			@RequestParam(name = "page",defaultValue = "0")int page,
			@RequestParam(name = "size",defaultValue="10")int size){
		UserInfo user = (UserInfo) session.getAttribute("user");
		userFile.setUid(user.getId());//防止查询别人的文件
		//防止写错filename属性
		userFile.setOriginalFileName(userFile.getFileName());
		Page<UserFile> result = userFileService.search(userFile, page, size);
		result.stream().forEach(file->{
			setFileNameNull(file);
		});
		return ResponseEntity.ok(result);
		
	}
	public ResponseEntity<UserFile> deleteUserFile(){
		return null;
	}
	@PostMapping("/search/name")
	public ResponseEntity<Page<UserFile>> searchByName(@RequestBody UserFile userFile,HttpSession session,
			@RequestParam(name = "page",defaultValue = "0")int page,
			@RequestParam(name = "size",defaultValue="10")int size){
		UserInfo user = (UserInfo) session.getAttribute("user");
		userFile.setUid(user.getId());//防止查询别人的文件
		//防止写错filename属性
		userFile.setOriginalFileName(userFile.getFileName());
		Page<UserFile> result = searchSecretName(userFile, page, size);
		result.stream().forEach(file->{
			setFileNameNull(file);
		});
		return ResponseEntity.ok(result);
		
	}
	
	/**
	 * 删除用户文件
	 * @param fid
	 * @param session
	 * @return
	 */
	@DeleteMapping
	public ResponseEntity<Map<Integer,Boolean>> delete(@RequestParam("fids")List<Integer> fids,HttpSession session){
		UserInfo user = (UserInfo) session.getAttribute("user");
		Map<Integer, Boolean> result = new HashMap<>();
		if(fids==null) {
			throw new BadRequestException("fids is null");
		}
		for(int i=0;i<fids.size();i++) {			
			boolean res = folderFileService.deleteFolderFileLogic(user.getId(), fids.get(i));
			result.put(fids.get(i), res);
		}
		RedisValueManager.updateUserInfo(session);
		return ResponseEntity.ok(result);
		
	}
	/**
	 * 获取文件夹中的文件
	 * @param folderId
	 * @return
	 */
	@GetMapping("/{folder}")
	public ResponseEntity<Page<UserFile>> getFolderFile(@PathParam("folder") int folderId,HttpSession session,
			@RequestParam(name = "page",defaultValue = "0")int page,
			@RequestParam(name = "size",defaultValue="10")int size){
		UserInfo user = (UserInfo) session.getAttribute("user");
		Page<UserFile> result = userFileService.findByFolderId(user.getId(), folderId, page, size);
		result.stream().forEach(file->{
			setFileNameNull(file);
		});
		return ResponseEntity.ok(result);
	}
	
	/**
	 * 重命名文件
	 * @param name
	 * @param id
	 * @param session
	 * @return
	 */
	@PutMapping("/rename")
	public ResponseEntity<UserFile> renameFile(@RequestParam("name")String name,@RequestParam("id") int id,HttpSession session){
		UserInfo user = (UserInfo) session.getAttribute("user");
		UserFile userFile = userFileService.findOne(user.getId(), id);
		if(userFile == null) {
			throw new BadRequestException("非法的文件id！");
		}
		FileInfoExt fileInfoExt = fileService.findOneFileInfoExt(userFile.getFileId());
		fileInfoExt.setOriginalFileName(name);
		fileService.updateFileInfoExt(fileInfoExt);
		userFile = userFileService.findOne(user.getId(), id);
		setFileNameNull(userFile);
		return ResponseEntity.ok(userFile);
	}
	/**
	 * 修改用户文件的可访问范围
	 * @param session
	 * @param userFile *fileId,*acl 为必填项
	 * @return
	 */
	@PutMapping("/acl")
	public ResponseEntity<UserFile> setAcl(HttpSession session,@RequestBody UserFile userFile){
		UserInfo user = (UserInfo) session.getAttribute("user");
		if(userFile.getFileId()==0 || userFile.getAcl() == null) {
			throw new BadRequestException("请求参数不完整！fileId,acl不能为空！");
		}
		UserFile userFileTmp = userFileService.findOne(user.getId(), userFile.getId());
		if(userFileTmp == null) {
			throw new BadRequestException("非法的文件id！");
		}
		FileInfoExt fileInfoExt = fileService.findOneFileInfoExt(userFileTmp.getFileId());
		fileInfoExt.setAcl(userFile.getAcl());
		fileService.updateFileInfoExt(fileInfoExt);
		userFile = userFileService.findOne(user.getId(), userFileTmp.getId());
		setFileNameNull(userFile);
		return ResponseEntity.ok(userFile);
	}
	/**
	 * 移动文件到目标文件夹
	 * @param folderId
	 * @param id
	 * @param session
	 * @return
	 */
	@PutMapping("/move")
	public ResponseEntity<UserFile> moveFolder(@RequestParam("folderId") int folderId,@RequestParam("id") int id, HttpSession session){
		UserInfo user = (UserInfo) session.getAttribute("user");
		UserFile userFile = userFileService.findOne(user.getId(), id);
		if(userFile == null) {
			throw new BadRequestException("非法的文件id！");
		}
		Folder folder = folderService.getFolder(user, folderId);
		if(folder == null) {
			throw new BadRequestException("目标文件夹不存在！");
		}
		FolderFile folderFile = folderFileService.findOne(id);
		folderFile.setFolderId(folderId);
		folderFileService.updateFolderFile(user.getId(), folderFile);
		userFile = userFileService.findOne(user.getId(), id);
		setFileNameNull(userFile);
		return ResponseEntity.ok(userFile);
	}
	
	/**
	 * 延长用户文件过期时间
	 * @param session
	 * @param fileDelay
	 * @return
	 */
	@PutMapping("/delay")
	public ResponseEntity<List<FileDelay>> delayFileExpiredTime(HttpSession session,@RequestBody List<FileDelay> filesDelay){
		UserInfo user = (UserInfo) session.getAttribute("user");
		for(FileDelay fileDelay:filesDelay) {			
			if(fileDelay==null || fileDelay.getFid() == 0) {
				throw new BadRequestException("请求信息不完整，无法完成操作！");
			}
			UserFile userFile = userFileService.findOne(user.getId(), fileDelay.getFid());
			Date expiredTime = userFile.getGmtExpired();
			switch (fileDelay.getUnit()) {
			case DAYS:
				expiredTime = new Date(expiredTime.getTime() + fileDelay.getTime() * 1000L * 60 * 60 * 24);
				break;
			case HOURS:
				expiredTime = new Date(expiredTime.getTime() + fileDelay.getTime() * 1000L * 60 * 60);
				break;
			case MINUTES:
				expiredTime = new Date(expiredTime.getTime() + fileDelay.getTime() * 1000L * 60);
				break;
			case SECONDS:
				expiredTime = new Date(expiredTime.getTime() + fileDelay.getTime() * 1000L);
				break;
			case MILLISECONDS:
				expiredTime = new Date(expiredTime.getTime() + fileDelay.getTime());
				break;
			default:
				expiredTime = new Date(expiredTime.getTime() + fileDelay.getTime() * 1000L * 60 * 60 * 24);
				break;
			}
			FileInfoExt fileInfoExt = fileService.findOneFileInfoExt(userFile.getFileId());
			if(fileInfoExt == null) {
				throw new BadRequestException("无法获取文件信息！");
			}
			fileInfoExt.setGmtExpired(expiredTime);
			fileInfoExt = fileService.updateFileInfoExt(fileInfoExt);
			userFile.setGmtExpired(fileInfoExt.getGmtExpired());
		}
		return ResponseEntity.ok(filesDelay);
		
	}
	
     /**
	 * 获取即将到期大附件列表
	 * @param expireBeforeDays
	 * @return
	 */
	@GetMapping("/uptoexpire")
	public ResponseEntity<ExpireBeforeUserFile> findExpireBefores(
			@RequestParam("expireBeforeDays") int expireBeforeDays, @RequestParam("uid") int uid) {
		return ResponseEntity.ok(userFileService.findExpireBefores(expireBeforeDays, uid));
	}
	
	private Page<UserFile> searchSecretName(UserFile example, int page, int size) {
		Pageable pageable = null;
		if(size>0) {
			pageable = PageRequest.of(page, size);
		}else {
			pageable = Pageable.unpaged();
		}
		List<UserFile> result = new ArrayList<>();
		int index = 0;
		int startIndex = 0;
		int cSize = 0;
		long total = 0;
		List<UserFile> temp = userFileService.findAll(example.getUid(), 0, 0).getContent();
		//skip 
		for(UserFile f : temp) {
			if(f.getOriginalFileName().contains(example.getOriginalFileName())) {
				if(index < page*size) {						
					index ++;
				}
				total++;
			}
			if(index < page*size) {
				startIndex ++;
			}
		}
		//添加数据
//		List<UserFile> tempf = userFileService.findAll(example.getUid(), searchPage++, size).getContent();
		int i = 0;
		for(UserFile f:temp) {
			if(i<startIndex) {
				i++;
				continue;
			}
			if(f.getOriginalFileName().contains(example.getOriginalFileName())) {	
				if(size == 0) {					
					result.add(f);
				}else if(cSize<size) {
					result.add(f);
				}
				if(size!=0 && ++cSize>=size) {
					break;
				}
			}
		}
		Page<UserFile> pageResult = null;
		if(pageable.isUnpaged()) {
			pageResult = new PageImpl<>(result);
		}else {
			pageResult = new PageImpl<>(result, pageable, total);
		}
		return pageResult;
	}
	
	/**
	 * 把fileName置空，保证安全性
	 * @param file
	 */
	private void setFileNameNull(UserFile file) {
		if(file!=null) {
			file.setFileName(null);
		}
	}
}
