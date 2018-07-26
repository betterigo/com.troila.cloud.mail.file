package com.troila.cloud.mail.file.controller;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.BadRequestException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.troila.cloud.mail.file.model.FileDetailInfo;
import com.troila.cloud.mail.file.model.FileInfo;
import com.troila.cloud.mail.file.model.FileInfoExt;
import com.troila.cloud.mail.file.model.PrepareUploadResult;
import com.troila.cloud.mail.file.model.ProgressInfo;
import com.troila.cloud.mail.file.service.FileService;

/**
 * 文件上传和下载接口Controller类
 * @author haodonglei
 *
 */
@RestController
@RequestMapping("/file")
public class FileController {
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private static Map<String, FileDetailInfo> fileInfos = new ConcurrentHashMap<>();
	
	@Value("${upload.file.maxSize}")
	private long UPLOAD_fILE_MAX_SIZE;
	
	@Autowired
	private FileService fileService;
	
	private static final long BYTE_MB = 1024 * 1024;
	
	//最大上传的文件块大小为20MB
	private static final long MAX_UPLAOD_PART_SIZE = 20* 1024 * 1024;
	
	//最大上传的文件块大小为5MB
	private static final long MIN_UPLAOD_PART_SIZE = 5* 1024 * 1024;
	/*
	 * 上传文件接口方法，调用此方法前必须先调用"/prepare"接口
	 * 分块大小范围为5MB~20MB
	 * TODO:当有多个人同时上传同一个文件时 需要处理，先上传成功会保存在服务器上，后面的不会保存，并不会提高上传的速度
	 */
	@PostMapping
	public ResponseEntity<ProgressInfo> upload(@RequestParam("uploadId") String uploadId,@RequestParam("file") MultipartFile file,
			@RequestParam("index") int index) throws IOException{
		FileDetailInfo fileInfo = fileInfos.get(uploadId);
		if(fileInfo == null) {
			throw new BadRequestException("server does not have this current file infomation!");
		}
		if(file.getSize() > MAX_UPLAOD_PART_SIZE) {
			throw new BadRequestException("file part is too large!");
		}
		if(fileInfo.getTotalPart()>1 && fileInfo.getTotalPart() != index+1 && file.getSize()<MIN_UPLAOD_PART_SIZE) {
			throw new BadRequestException("file part is too small!");
		}
		//锁对象，这样对于不同的文件就没有影响了
		synchronized (fileInfo) {			
			ProgressInfo progressInfo = fileService.uploadPart(file.getInputStream(), index, fileInfo, file.getSize());
			return ResponseEntity.ok(progressInfo);
		}
		
	}

	/*
	 * 准备上传接口
	 */
	@PostMapping("/prepare")
	public ResponseEntity<PrepareUploadResult> prepareUpload(@RequestBody FileDetailInfo fileInfo){
		//查询此文件是否已经有人上传
		FileInfo info = fileService.find(fileInfo.getMd5());
		PrepareUploadResult prepareUploadResult = new PrepareUploadResult();
		String originalFileName = fileInfo.getOriginalFileName();
		int pos = originalFileName.lastIndexOf(".");
		String suffix = originalFileName.substring(pos, originalFileName.length());
		if(info!=null) {
			FileInfoExt fileInfoExt = new FileInfoExt();
			fileInfoExt.setOriginalFileName(fileInfo.getOriginalFileName());
			fileInfoExt.setSuffix(suffix);
			fileInfoExt.setBaseFid(info.getId());
			fileService.saveInfoExt(fileInfoExt);
			prepareUploadResult.setBingo(true);
			logger.info("文件【{}】秒传！",fileInfo.getOriginalFileName());
			return ResponseEntity.ok(prepareUploadResult);
		}
		UUID uuid = UUID.randomUUID();
		fileInfo.setFileName(uuid.toString().toUpperCase());
		if(fileInfo.getSize() > UPLOAD_fILE_MAX_SIZE * BYTE_MB) {
			throw new BadRequestException("file is too large!");
		}
		UUID uploadUUID =  UUID.randomUUID();
		String uploadId = uploadUUID.toString();
		fileInfo.setSuffix(suffix.trim().toLowerCase());
		fileInfo.setUploadId(uploadId);
		fileInfos.put(uploadId, fileInfo);
		prepareUploadResult.setUploadId(uploadId);
		logger.info("文件【{}】开始准备上传,size:{},totalPart:{},md5:{}",fileInfo.getOriginalFileName(),fileInfo.getSize(),fileInfo.getTotalPart(),fileInfo.getMd5());
		return ResponseEntity.ok(prepareUploadResult);
	}
	
	@GetMapping
	public ResponseEntity<String> download(HttpServletResponse resp,HttpServletRequest req,
			@RequestParam("fid") int fid){
		FileDetailInfo fileDetailInfo = fileService.find(fid);
		resp.addHeader("Content-Disposition", "attachment;filename=" + fileDetailInfo.getOriginalFileName());
		InputStream in = fileService.download(fid);
		BufferedInputStream is = null;
		OutputStream os = null;
		try {
			is = new BufferedInputStream(in);
			os = resp.getOutputStream();
			int len = 0;
			byte[] buffer = new byte[4096];
			while((len = is.read(buffer)) > 0) {
				os.write(buffer,0,len);
				os.flush();
			}
		} catch(Exception e) {
			logger.error("文件下载【{}】异常：{}", fid, e.getMessage(), e);
		} finally {
			if(is != null) {
				try {
					is.close();
				} catch(IOException e) {
				}
			}
			if(os != null) {
				try {
					os.close();
				} catch(IOException e) {
				}
			}
		}
		return ResponseEntity.ok().build();
	} 
}