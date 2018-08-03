package com.troila.cloud.mail.file.controller;

import java.util.List;

import javax.servlet.http.HttpSession;

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

import com.troila.cloud.mail.file.model.Folder;
import com.troila.cloud.mail.file.model.User;
import com.troila.cloud.mail.file.service.FolderService;

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
	
	/**
	 * 当前用户建立一个文件夹
	 * @param session
	 * @param folderName
	 * @return
	 */
	@PostMapping("/create")
	public ResponseEntity<Folder> createUserFolder(HttpSession session,@RequestParam("folderName")String folderName,@RequestParam(name = "parentFid",defaultValue="0")int parentFid){
		User user = (User) session.getAttribute("user");
		Folder result =  folderService.create(user, folderName, parentFid);
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
		User user = (User) session.getAttribute("user");
		Folder result =  folderService.getFolder(user, folderId);
		return ResponseEntity.ok(result);
		
	}
	
	/**
	 * 获取用户的文件夹列表
	 * @param session
	 * @return
	 */
	@GetMapping("/list")
	public ResponseEntity<List<Folder>> getUserFolders(HttpSession session){
		User user = (User) session.getAttribute("user");
		List<Folder> result = folderService.getUserFolders(user);
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
		User user = (User) session.getAttribute("user");
		folderService.deleteFolder(user, folderId);
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
		User user = (User) session.getAttribute("user");
		folder.setUid(user.getId());
		Folder result =  folderService.update(folder);
		return ResponseEntity.ok(result);
		
	}
}
