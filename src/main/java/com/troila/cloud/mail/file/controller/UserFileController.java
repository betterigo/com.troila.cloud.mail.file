package com.troila.cloud.mail.file.controller;

import java.util.List;

import javax.servlet.http.HttpSession;
import javax.ws.rs.PathParam;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.troila.cloud.mail.file.model.User;
import com.troila.cloud.mail.file.model.UserFile;
import com.troila.cloud.mail.file.service.UserFileService;

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
	/**
	 * 分页查询用户的所有文件
	 * @return
	 */
	@GetMapping("/all")
	public ResponseEntity<Page<UserFile>> getAllFiles(@RequestParam(name = "page",defaultValue = "0")int page,
			@RequestParam(name = "size",defaultValue="0")int size,HttpSession session){
		User user = (User) session.getAttribute("user");
		Page<UserFile> result = userFileService.findAll(user.getId(), page, size);
		return ResponseEntity.ok(result);
		
	}
	
	/**
	 * 查询接口
	 * @return
	 */
	@GetMapping("/search")
	public ResponseEntity<Page<UserFile>> search(){
		return null;
		
	}
	
	/**
	 * 获取文件夹中的文件
	 * @param folderId
	 * @return
	 */
	@GetMapping("/{folder}")
	public ResponseEntity<Page<UserFile>> getFolderFile(@PathParam("folder") int folderId){
		return null;
	}
}
