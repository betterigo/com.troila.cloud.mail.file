package com.troila.cloud.mail.file.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.PathParam;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
		return ResponseEntity.ok(result);
		
	}
	/**
	 * 查询接口
	 * @return
	 */
	@GetMapping("/search")
	public ResponseEntity<Page<UserFile>> search(@RequestBody UserFile userFile,HttpSession session,
			@RequestParam(name = "page",defaultValue = "0")int page,
			@RequestParam(name = "size",defaultValue="0")int size){
		UserInfo user = (UserInfo) session.getAttribute("user");
		userFile.setUid(user.getId());//防止查询别人的文件
		Page<UserFile> result = userFileService.search(userFile, page, size);
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
			@RequestParam(name = "size",defaultValue="0")int size){
		UserInfo user = (UserInfo) session.getAttribute("user");
		Page<UserFile> result = userFileService.findByFolderId(user.getId(), folderId, page, size);
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
		return ResponseEntity.ok(userFile);
	}
}
