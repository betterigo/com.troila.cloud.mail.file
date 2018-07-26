package com.troila.cloud.mail.file.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AbortMultipartUploadRequest;
import com.amazonaws.services.s3.model.CompleteMultipartUploadRequest;
import com.amazonaws.services.s3.model.InitiateMultipartUploadRequest;
import com.amazonaws.services.s3.model.InitiateMultipartUploadResult;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PartETag;
import com.amazonaws.services.s3.model.UploadPartRequest;
import com.amazonaws.services.s3.model.UploadPartResult;
import com.troila.cloud.mail.file.model.FileHandler;
import com.troila.cloud.mail.file.model.FileInfo;
import com.troila.cloud.mail.file.model.ProgressInfo;
import com.troila.cloud.mail.file.repository.FileInfoRepository;
import com.troila.cloud.mail.file.service.FileService;

@RestController
@RequestMapping("/test")
public class FileTestController {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private static Map<String, FileHandler> fileStore = new ConcurrentHashMap<>();

	private static Map<String, InitiateMultipartUploadResult> cephStore = new ConcurrentHashMap<>();
	
	private static Map<String, Integer> partStore = new ConcurrentHashMap<>();
	
	private static Map<String, List<PartETag>> eTagtStore = new ConcurrentHashMap<>();

	private static final int BYTE_ARRAY_LENGTH = 1024;

	private static final String TEMP_DATE_FOLDER = "dataTemp";
	@Autowired
	FileService fileService;

	@Autowired
	FileInfoRepository fileInfoRepository;

	@Autowired
	private AmazonS3 s3;

	@GetMapping
	public String hello() {
		return "Hello World";
	}

	@GetMapping("/info")
	public FileInfo getFileInfo() {
		FileInfo fileinfo = fileInfoRepository.getOne(15);
		return fileinfo;
	}

	
	public FileTestController() {
		super();
		File tmpF = new File(TEMP_DATE_FOLDER);
		logger.info("创建临时文件夹");
		if (!tmpF.exists()) {
			tmpF.mkdir();
		}
	}

	@PostMapping("/file")
	public ResponseEntity<FileInfo> uploadFile(@RequestParam("file") MultipartFile file) throws IOException {
		FileInfo fileInfo = new FileInfo();
		fileInfo.setFileName(file.getOriginalFilename());
		fileInfo.setMd5(file.getName());
		fileInfo.setSize(file.getSize());
		fileInfo.setGmtCreate(new Date());

		fileInfo = fileInfoRepository.save(fileInfo);

		fileService.upload(file.getInputStream(), fileInfo);
		return ResponseEntity.ok(fileInfo);
	}

	/**
	 * 分片上传文件方法，此方法涉及多次的读写操作，因此为同步方法
	 * 
	 * @param file
	 * @param index
	 * @param md5
	 * @param totalParts
	 * @return
	 * @throws IOException
	 */
	@PostMapping("/part")
	public synchronized ResponseEntity<ProgressInfo> uploadPartFile(@RequestParam("file") MultipartFile file,
			@RequestParam("index") int index, @RequestParam("md5") String md5,
			@RequestParam("totalParts") int totalParts) throws IOException {
		FileInfo fileInfo = new FileInfo();
		OutputStream out = null;
		fileInfo.setFileName(file.getOriginalFilename());
		fileInfo.setMd5(file.getName());
		fileInfo.setSize(file.getSize());
		fileInfo.setGmtCreate(new Date());
		FileHandler handler = new FileHandler();
		if (fileStore.get(md5) == null) {
			// 如果存储中没有这个文件对象，那么创建这个文件对象并开启一个写入流
			handler.setMd5(md5);
			handler.setStarTime(System.currentTimeMillis());
			handler.setTotalPart(totalParts);
			File tempFile = new File(file.getOriginalFilename());
			out = new FileOutputStream(tempFile);
			handler.setOut(out);
			// 获取临时文件夹
			File tmpF = new File(TEMP_DATE_FOLDER);
			handler.setCacheFolder(tmpF);
			if (index == 0) {// 第一部分，直接写入
				handler.getOut().write(file.getBytes());
				handler.setCurrentPart(index);
			} else {
				createNewTempFile(file, index, handler);
			}
			fileStore.put(md5, handler);
		} else {
			handler = fileStore.get(md5);
			if (index == 0) {
				handler.getOut().write(file.getBytes());
				handler.setCurrentPart(index);
			} else {
				if (handler.getCurrentPart() + 1 == index) {
					handler.getOut().write(file.getBytes());
					handler.setCurrentPart(handler.getCurrentPart() + 1);
				} else {
					createNewTempFile(file, index, handler);
				}
			}
			writeTmpFile(handler, handler.getCurrentPart() + 1);
		}
		if (handler != null && handler.getCurrentPart() + 1 == handler.getTotalPart()) {
			handler.getOut().close();// 关闭文件流
			for (File f : handler.getCacheFiles().values()) {
				logger.info("清理{}缓存文件", f.getName());
				f.delete();
			}
			logger.info("删除缓存文件夹{}", handler.getCacheFolder().getName());
			if (handler.getCacheFolder().listFiles() != null) {
				// handler.getCacheFolder().delete();
			}
			logger.info("上传文件{}完成！", file.getName());
			fileStore.remove(md5);
		}
		handler.setUploadSize(handler.getUploadSize() + file.getSize());
		long time = System.currentTimeMillis() - handler.getStarTime();
		System.out.println("~~~~~~~~~~" + time + "totalSize:" + handler.getUploadSize());
		double speed = (handler.getUploadSize() / time) * 1000;
		handler.setSpeed(speed);
		logger.info("文件上传速度为:" + speed / 1024 + "KB/S");
		ProgressInfo progressInfo = new ProgressInfo();
		progressInfo.setUploadSize(handler.getUploadSize());
		progressInfo.setMd5(md5);
		progressInfo.setUsedTime(time);
		progressInfo.setSpeed(handler.getSpeed());
		return ResponseEntity.ok(progressInfo);

	}

	private void createNewTempFile(MultipartFile file, int index, FileHandler handler)
			throws IOException, FileNotFoundException {
		UUID uuid = UUID.randomUUID();
		String fileName = uuid.toString().replaceAll("-", "");
		logger.info("创建上传文件【{}】的临时文件{}", file.getOriginalFilename(), index + "_" + fileName + ".tmp");
		File tmpFile = new File(handler.getCacheFolder(), index + "_" + fileName + ".tmp");
		System.out.println(tmpFile.getAbsolutePath());
		byte[] b = new byte[BYTE_ARRAY_LENGTH];
		int len;
		InputStream in = file.getInputStream();
		OutputStream out = new FileOutputStream(tmpFile);
		while ((len = in.read(b)) != -1) {
			out.write(b, 0, len);
		}
		out.close();
		handler.getCacheFiles().put(index, tmpFile);
	}

	private void writeTmpFile(FileHandler handler, int searchIndex) throws FileNotFoundException, IOException {
		if (handler.getCacheFiles() == null) {
			logger.info("空的临时文件列表");
			return;
		}
		File temp = handler.getCacheFiles().get(searchIndex);
		if (temp != null) {
			logger.info("读取临时文件{}...", temp.getName());
			InputStream in = new FileInputStream(temp);
			byte[] tempByte = new byte[BYTE_ARRAY_LENGTH];
			int len;
			while ((len = in.read(tempByte)) != -1) {
				handler.getOut().write(tempByte, 0, len);
			}
			handler.setCurrentPart(handler.getCurrentPart() + 1);
			in.close();
			logger.info("读取临时文件{}...完毕", temp.getName());
			writeTmpFile(handler, searchIndex + 1);
			return;
		} else {
			return;
		}
	}

	/**
	 * ceph s3 api 分段上传文件 分段文件大小范围5MB~5GB,最后一个分段大小可以小于5MB
	 * @param file
	 * @param index
	 * @param md5
	 * @param totalParts
	 * @return
	 * @throws IOException
	 */
	@PostMapping("/upload/ceph")
	public synchronized ResponseEntity<ProgressInfo> uploadFileToCeph(@RequestParam("file") MultipartFile file,
			@RequestParam("index") int index, @RequestParam("md5") String md5,
			@RequestParam("totalParts") int totalParts) throws IOException {
		InitiateMultipartUploadResult result = null;
		List<PartETag> partETagList = null;
		CompleteMultipartUploadRequest completeMultipartUploadRequest = null;
		// 初始化s3分片上传
		if (cephStore.get(md5) == null) {
			logger.info("初始化文件{}分片上传...",file.getOriginalFilename());
			InitiateMultipartUploadRequest init = new InitiateMultipartUploadRequest("mailcloud.test", md5);
			ObjectMetadata meta = new ObjectMetadata();
//			meta.setContentLength(contentLength);
			init.setObjectMetadata(meta);
			result = s3.initiateMultipartUpload(init);
			cephStore.put(md5, result);
			logger.info("初始化文件{}分片上传...完毕！上传ID为:{}",file.getOriginalFilename(),result.getUploadId());
		}else {
			result = cephStore.get(md5);
		}
		UploadPartRequest req = new UploadPartRequest();
		req.setBucketName("mailcloud.test");
		req.setKey(md5);
		req.setUploadId(result.getUploadId());
		req.setPartNumber(index + 1);
		req.setInputStream(file.getInputStream());
		req.setPartSize(file.getSize());
		if(partStore.get(md5)==null) {			
			partStore.put(md5, 1);//已经上传了一块文件
			partETagList = new ArrayList<>();
			eTagtStore.put(result.getUploadId(), partETagList);
		}else {
			partETagList = eTagtStore.get(result.getUploadId());
		}
		int parts = partStore.get(md5);
		if(parts == totalParts) {
			completeMultipartUploadRequest = new CompleteMultipartUploadRequest();
			req.setLastPart(true);
			partStore.remove(md5);
		}else {
			parts++;
			partStore.put(md5, parts);
		}
		try {			
			UploadPartResult uploadPartResult = s3.uploadPart(req);
			partETagList.add(uploadPartResult.getPartETag());
			if(completeMultipartUploadRequest != null) {			
				completeMultipartUploadRequest.setBucketName("mailcloud.test");
				completeMultipartUploadRequest.setKey(md5);
				completeMultipartUploadRequest.setUploadId(result.getUploadId());
				completeMultipartUploadRequest.setPartETags(partETagList);
				s3.completeMultipartUpload(completeMultipartUploadRequest);
			}
		} catch (Exception e) {
			 s3.abortMultipartUpload(new AbortMultipartUploadRequest("mailcloud.test", md5, result.getUploadId()));
		}
		ProgressInfo progressInfo = new ProgressInfo();
		return ResponseEntity.ok(progressInfo);
	}

	/**
	 * 获取文件上传进度
	 * 
	 * @param md5
	 * @return
	 */
	@GetMapping("/progress")
	public ResponseEntity<ProgressInfo> getProgressInfo(@RequestParam("md5") String md5) {
		ProgressInfo progressInfo = new ProgressInfo();
		FileHandler handler = fileStore.get(md5);
		if (handler == null) {
			return ResponseEntity.ok(progressInfo);
		}
		progressInfo.setUploadSize(progressInfo.getUploadSize());
		progressInfo.setMd5(md5);
		long time = System.currentTimeMillis() - handler.getStarTime();
		progressInfo.setUsedTime((int) time / 1000);
		progressInfo.setSpeed(handler.getSpeed());
		return ResponseEntity.ok(progressInfo);
	}

}
