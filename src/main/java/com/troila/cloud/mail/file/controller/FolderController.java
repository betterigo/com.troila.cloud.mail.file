package com.troila.cloud.mail.file.controller;

import java.util.List;

import javax.servlet.http.HttpSession;
import javax.ws.rs.BadRequestException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.troila.cloud.mail.file.exception.FolderException;
import com.troila.cloud.mail.file.model.Folder;
import com.troila.cloud.mail.file.model.UserFolderDetail;
import com.troila.cloud.mail.file.model.UserInfo;
import com.troila.cloud.mail.file.service.FolderService;
import com.troila.cloud.mail.file.service.UserFolderService;

/**
 * 文件夹相关接口api
 * @author haodonglei
 *
 */
@RestController
@RequestMapping("/folder")
public class FolderController {
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private FolderService folderService;
	
	@Autowired
	private UserFolderService userFolderService;
	
	/**
	 * 当前用户建立一个文件夹
	 * @param session
	 * @param folderName
	 * @return
	 */
	@PostMapping("/create")
	public ResponseEntity<Folder> createUserFolder(HttpSession session,@RequestParam("folderName")String folderName,@RequestParam(name = "parentFid",defaultValue="0")int parentFid){
		UserInfo user = (UserInfo) session.getAttribute("user");
		Folder result;
		try {
			result = folderService.create(user, folderName, parentFid,null,null);
		} catch (FolderException e) {
			logger.error("创建文件夹失败!",e);
			throw new BadRequestException("创建文件夹失败!"+e.getMessage());
		}
		logger.info("用户【{}】创建了文件夹【{}】",user.getName(),result.getName());
		return ResponseEntity.ok(result);
	}
	/**
	 * 根据id获取一个文件夹信息
	 * @param session
	 * @param folderId
	 * @return
	 */
	@GetMapping
	public ResponseEntity<Folder> getFolderInfo(HttpSession session,@RequestParam("folderId")int folderId){
		UserInfo user = (UserInfo) session.getAttribute("user");
		Folder result =  folderService.getFolder(user, folderId);
		return ResponseEntity.ok(result);
		
	}
	
	/**
	 * 获取用户的文件夹列表
	 * @param session
	 * @return
	 */
	@GetMapping("/list")
	public ResponseEntity<List<UserFolderDetail>> getUserFolders(HttpSession session,@RequestParam(name="pid",defaultValue="0")int pid){
		UserInfo user = (UserInfo) session.getAttribute("user");
		List<UserFolderDetail> result = userFolderService.listMyFolders(user.getId(), pid);
		return ResponseEntity.ok(result);
		
	}
	
	/**
	 * 根据folderId删除用户的文件夹
	 * @param session
	 * @param folderId
	 * @return
	 */
	@DeleteMapping
	public ResponseEntity<String> deleteFolderById(HttpSession session,@RequestParam("folderId")int folderId){
		UserInfo user = (UserInfo) session.getAttribute("user");
		folderService.deleteFolderLogic(user, folderId);//逻辑删除
		return ResponseEntity.ok("success");
		
	}
	
	/**
	 * 修改文件夹
	 * @param session
	 * @param folder
	 * @return
	 */
	@PutMapping
	public ResponseEntity<Folder> update(HttpSession session,@RequestBody Folder folder){
		UserInfo user = (UserInfo) session.getAttribute("user");
		folder.setUid(user.getId());
		Folder result =  folderService.update(folder);
		return ResponseEntity.ok(result);
		
	}
}
