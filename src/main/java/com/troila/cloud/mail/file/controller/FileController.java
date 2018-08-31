package com.troila.cloud.mail.file.controller;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Base64;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.ws.rs.BadRequestException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.troila.cloud.mail.file.component.DownloadUrlSecureConverter;
import com.troila.cloud.mail.file.config.settings.UserDefaultSettings;
import com.troila.cloud.mail.file.model.FileDetailInfo;
import com.troila.cloud.mail.file.model.FileInfo;
import com.troila.cloud.mail.file.model.PrepareUploadResult;
import com.troila.cloud.mail.file.model.ProgressInfo;
import com.troila.cloud.mail.file.model.RangeSettings;
import com.troila.cloud.mail.file.model.UserFile;
import com.troila.cloud.mail.file.model.UserInfo;
import com.troila.cloud.mail.file.model.ValidateInfo;
import com.troila.cloud.mail.file.model.fenum.AccessList;
import com.troila.cloud.mail.file.model.fenum.FileStatus;
import com.troila.cloud.mail.file.service.FileService;
import com.troila.cloud.mail.file.service.FolderFileService;
import com.troila.cloud.mail.file.service.PreviewService;
import com.troila.cloud.mail.file.service.UserFileService;
import com.troila.cloud.mail.file.utils.DownloadSpeedLimiter;
import com.troila.cloud.mail.file.utils.FileTypeUtil;
import com.troila.cloud.mail.file.utils.FileUtil;
import com.troila.cloud.mail.file.utils.InformationStores;
import com.troila.cloud.mail.file.utils.IoUtil;
import com.troila.cloud.mail.file.utils.OfficeFileUtils;
import com.troila.cloud.mail.file.utils.TokenUtil;

/**
 * 文件上传和下载接口Controller类
 * 
 * @author haodonglei
 *
 */
@RestController
@RequestMapping("/file")
public class FileController {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private static Map<String, FileDetailInfo> fileInfos = InformationStores.getFileInfosStore();

	@Value("${upload.file.maxSize}")
	private long UPLOAD_fILE_MAX_SIZE;

	@Value("${download.speed.limit}")
	private long DOWN_SPEED_LIMIT;

	private final int REQUEST_INTERVAL = 500;

	@Autowired
	private StringRedisTemplate redisTemplate;

	@Autowired
	private UserDefaultSettings userDefaultSettings;

	@Autowired
	private FileService fileService;

	@Autowired
	private FolderFileService folderFileService;

	@Autowired
	private UserFileService userFileService;
	
	@Autowired
	private PreviewService previewService;

	@Autowired
	private DownloadUrlSecureConverter downloadUrlSecureConverter;

	private ObjectMapper mapper = new ObjectMapper();

	private static final int CACHE_BUFFER_SIZE = 2048;

	private static final long BYTE_MB = 1024 * 1024;

	// 最大上传的文件块大小为20MB
	private static final long MAX_UPLAOD_PART_SIZE = 20 * 1024 * 1024;

	// 最大上传的文件块大小为5MB
	private static final long MIN_UPLAOD_PART_SIZE = 5 * 1024 * 1024;

	private static final long DEFAULT_EXPIRED_TIME = 30 * 24 * 60 * 60 * 1000L;

	@GetMapping("/test")
	public String test() {
		return "file server is running";
	}

	@GetMapping("/info/{fid}")
	public ResponseEntity<FileDetailInfo> getDetailInfo(@PathVariable("fid") int fid, HttpSession session) {
		FileDetailInfo result = fileService.find(fid);
		return ResponseEntity.ok(result);
	}

	/*
	 * 上传文件接口方法，调用此方法前必须先调用"/prepare"接口 分块大小范围为5MB~20MB TODO:当有多个人同时上传同一个文件时
	 * 需要处理，先上传成功会保存在服务器上，后面的不会保存，并不会提高上传的速度 断点续传
	 */
	@PostMapping
	public ResponseEntity<ProgressInfo> upload(@RequestParam("uploadId") String uploadId,
			@RequestParam("file") MultipartFile file, @RequestParam("index") int index, HttpServletResponse resp,
			HttpServletRequest req) throws IOException {
		FileDetailInfo fileInfo = fileInfos.get(uploadId);
		if (fileInfo == null) {
			throw new BadRequestException("server does not have information of this uploading file!");
		}
		if (file.getSize() > MAX_UPLAOD_PART_SIZE) {
			throw new BadRequestException("file part is too large!");
		}
		if (fileInfo.getTotalPart() > 1 && fileInfo.getTotalPart() != index + 1
				&& file.getSize() < MIN_UPLAOD_PART_SIZE) {
			throw new BadRequestException("file part is too small!");
		}
		// 锁对象，这样对于不同的文件就没有影响了
		synchronized (fileInfo) {
			if (fileInfo.getStatus() == FileStatus.PAUSE) {
				return ResponseEntity.ok(fileInfo.getProgressInfo());
			}
			if (fileInfo.getStatus() == FileStatus.UPLOADING) {
				ProgressInfo progressInfo = fileService
						.uploadPart(file.getInputStream(), index, fileInfo, file.getSize()).getProgressInfo();
				return ResponseEntity.ok(progressInfo);
			} else {
				return ResponseEntity.ok(fileInfo.getProgressInfo());
			}
		}

	}

	/**
	 * 暂停上传
	 * 
	 * @return
	 */
	@PostMapping("/pause")
	public ResponseEntity<ProgressInfo> pauseUpload(@RequestParam("uploadId") String uploadId) {
		FileDetailInfo fileInfo = fileInfos.get(uploadId);
		if (fileInfo == null) {
			throw new BadRequestException("没有uploadId为:" + uploadId + "的上传任务！");
		}
		fileInfo.setStatus(FileStatus.PAUSE);
		logger.info("文件【{}】上传已经暂停", fileInfo.getOriginalFileName());
		return ResponseEntity.ok(fileInfo.getProgressInfo());
	}

	/*
	 * 准备上传接口
	 */
	@PostMapping("/prepare")
	public ResponseEntity<PrepareUploadResult> prepareUpload(@RequestBody FileDetailInfo fileInfo,
			HttpServletResponse resp, HttpServletRequest req) {
		// 查询此文件是否已经有人上传
		UserInfo user = (UserInfo) req.getSession().getAttribute("user");
		if (user == null) {
			throw new BadRequestException("无效的用户信息!");
		}
		// 验证用户是否被禁用
		if (user.isDisable()) {
			throw new BadRequestException("用户已经被禁用!");
		}
		// 验证用户容量是否够用
		if (user.getUsed() + fileInfo.getSize() - user.getVolume() > 0) {
			throw new BadRequestException("用户可用容量不足!");
		}
		// 验证用户上传文件大小
		if (fileInfo.getSize() > user.getMaxFileSize()) {
			throw new BadRequestException("超过用户可上传的最大文件大小！");
		}
		fileInfo.setUid(user.getId());
		FileInfo info = fileService.find(fileInfo.getMd5());
		// 判断传入的info中是否含有uploadId，如果有，则为续传
		PrepareUploadResult prepareUploadResult = new PrepareUploadResult();
		if (fileInfo.getUploadId() != null) {
			fileInfo = fileInfos.get(fileInfo.getUploadId());
		}
		String originalFileName = fileInfo.getOriginalFileName();
		int pos = originalFileName.lastIndexOf(".");// TODO 没有文件类型的文件没做处理。
		String suffix = "";
		if (pos == -1) {
			suffix = "";
		} else {
			suffix = originalFileName.substring(pos + 1, originalFileName.length());
		}
		if (info != null) {
			if (fileInfo.getAcl() == null) {
				if (userDefaultSettings.isEnableAcl()) {
					fileInfo.setAcl(AccessList.PRIVATE);
				} else {
					fileInfo.setAcl(AccessList.PUBLIC);
				}
			}
			fileInfo.setBaseFid(info.getId());
			fileInfo.setSuffix(suffix);
			fileInfo.setGmtExpired(new Date(System.currentTimeMillis() + DEFAULT_EXPIRED_TIME));
			fileInfo.setFileType(FileTypeUtil.distinguishFileType(fileInfo.getSuffix()));
			fileInfo.setSecretKey(TokenUtil.getShortKey());
			folderFileService.complateUpload(fileInfo);
			prepareUploadResult.setBingo(true);
			prepareUploadResult.setFid(fileInfo.getProgressInfo().getFid());
			logger.info("文件【{}】秒传！", fileInfo.getOriginalFileName());
			return ResponseEntity.ok(prepareUploadResult);
		}
		if (fileInfo.getUploadId() != null) {
			fileInfo.setStatus(FileStatus.UPLOADING);
			fileInfo.getPartMap().values().stream().forEach(partInfo -> {
				if (!partInfo.isComplete()) {// 客户端需要根据此list来上传相应的part
					prepareUploadResult.setNeedPart(partInfo);
				}
			});
			fileInfo.setStartTime(System.currentTimeMillis() - fileInfo.getProgressInfo().getUsedTime());
		} else {
			UUID uuid = UUID.randomUUID();
			fileInfo.setFileName(uuid.toString().toUpperCase());
			if (fileInfo.getSize() > UPLOAD_fILE_MAX_SIZE * BYTE_MB) {
				throw new BadRequestException("file is too large!");
			}
			UUID uploadUUID = UUID.randomUUID();
			String uploadId = uploadUUID.toString();
			if (fileInfo.getAcl() == null) {
				if (userDefaultSettings.isEnableAcl()) {
					fileInfo.setAcl(AccessList.PRIVATE);
				} else {
					fileInfo.setAcl(AccessList.PUBLIC);
				}
			}
			if (fileInfo.getGmtExpired() == null) {
				fileInfo.setGmtExpired(new Date(System.currentTimeMillis() + DEFAULT_EXPIRED_TIME));
			}
			fileInfo.setSuffix(suffix.trim().toLowerCase());
			fileInfo.setUploadId(uploadId);
			fileInfo.setFileType(FileTypeUtil.distinguishFileType(fileInfo.getSuffix()));
			fileInfo.setBucket(fileInfo.getFileType().getValue());
			fileInfo.setSecretKey(TokenUtil.getShortKey());
			fileInfo.setStatus(FileStatus.UPLOADING);
			fileInfo.setExpiredTime(1 * 60 * 1000);// 设置1分钟超时
			if (fileInfo.getPartSize() != 0) {
				fileInfo.initPartMap();
			}
			fileInfos.put(uploadId, fileInfo);
		}
		prepareUploadResult.setInterval(REQUEST_INTERVAL);
		prepareUploadResult.setUploadId(fileInfo.getUploadId());
		logger.info("文件【{}】开始准备上传,size:{},totalPart:{},md5:{}", fileInfo.getOriginalFileName(), fileInfo.getSize(),
				fileInfo.getTotalPart(), fileInfo.getMd5());
		return ResponseEntity.ok(prepareUploadResult);
	}

	/**
	 * 文件下载接口 没有实现断点续传功能，参考资料https://www.2cto.com/kf/201610/552417.html
	 * 方法已过期{@link /download}
	 * 
	 * @param resp
	 * @param req
	 * @param fid
	 * @return
	 * @throws IOException
	 */
//	@GetMapping
	@Deprecated
	public ResponseEntity<String> download(HttpServletResponse resp, HttpServletRequest req,
			@RequestParam("fid") int fid) throws IOException {
		FileDetailInfo fileDetailInfo = fileService.find(fid);
		resp.reset();
		resp.setContentLengthLong(fileDetailInfo.getSize());
		String originalFileName = fileDetailInfo.getOriginalFileName();
		resp.setHeader("Content-Disposition", "attachment;filename=" + IoUtil.toUtf8String(originalFileName));
		resp.setContentType("application/octet-stream; charset=UTF-8");
		InputStream in = fileService.download(fileDetailInfo);
		if (in == null) {
			throw new BadRequestException("file does not exist on server!");
		}
		BufferedInputStream is = null;
		OutputStream os = null;
		try {
			is = new BufferedInputStream(in);
			os = resp.getOutputStream();
			int len = 0;
			// 2KB大小。
			byte[] buffer = new byte[CACHE_BUFFER_SIZE];
			DownloadSpeedLimiter limiter = new DownloadSpeedLimiter(DOWN_SPEED_LIMIT * 1024 * 1024, CACHE_BUFFER_SIZE);
			while ((len = is.read(buffer)) > 0) {
				os.write(buffer, 0, len);
				limiter.limit();
			}
			os.flush();
		} catch (Exception e) {
			logger.error("文件下载【{}】异常：{}", fid, e.getMessage());
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
				}
			}
			if (os != null) {
				try {
					os.close();
				} catch (IOException e) {
				}
			}
		}
		return ResponseEntity.ok(originalFileName);
	}

	/**
	 * 此方法支持多线程断点续传
	 * 
	 * @param resp
	 * @param req
	 * @param secreturl
	 * @return
	 * @throws IOException
	 */
	@GetMapping("/download")
	public ResponseEntity<String> downloadByPart(HttpServletResponse resp, HttpServletRequest req,
			@RequestParam(name = "link", required = false) String secretUrl,
			@RequestParam(name = "secretcode", required = false) String secretCode,
			@RequestParam(name = "filetoken", required = false) String fileToken,
			@RequestParam(name = "preview", defaultValue = "false") boolean preview) throws IOException {

		UserFile userFile = null;
		int fid = 0;
		boolean validated = false;
		if (secretUrl == null && secretCode != null) {
			byte[] validateKeyByte = Base64.getDecoder().decode(secretCode);
			String validateKey = new String(validateKeyByte, "UTF-8");
			String validateInfoStr = redisTemplate.opsForValue().get(validateKey);
			if (validateInfoStr == null) {
				throw new BadRequestException("验证码验证异常！");
			}
			ValidateInfo validateInfo = mapper.readValue(validateInfoStr, ValidateInfo.class);
			if (validateInfo == null) {
				throw new BadRequestException("验证码验证异常！");
			}
			redisTemplate.expire(validateKey, 5, TimeUnit.MINUTES);
			fid = validateInfo.getFid();
			preview = validateInfo.isPreview();
			userFile = userFileService.findOnePublic(fid);
			if (userFile != null && userFile.getSecretKey() != null
					&& userFile.getSecretKey().equals(validateInfo.getKey())) {
				validated = true;
				logger.info("通过验证码下载文件，验证通过！文件名:{}", userFile.getOriginalFileName());
			} else {
				throw new BadRequestException("验证码验证异常！");
			}
		} else {
			try {
				if (secretUrl == null) {
					throw new BadRequestException("下载链接解析异常！");
				}
				byte[] fidByte = Base64.getDecoder().decode(secretUrl.getBytes("UTF-8"));
				byte[] byteUrl = downloadUrlSecureConverter.decode(fidByte);
				String fidStr = new String(byteUrl, "UTF-8");
				int pos = fidStr.indexOf("&withkey=");
				if (pos == -1) {
					fid = Integer.valueOf(fidStr);
				} else {
					// 判断key是否合法
					String fidPart = fidStr.substring(0, pos);
					fid = Integer.valueOf(fidPart);
				}
				ValidateInfo tokenInfo=null;
				if(fileToken!=null) {
					byte[] tokenByte = Base64.getDecoder().decode(fileToken);
					String token = new String(tokenByte,"UTF-8");
					String infoStr = redisTemplate.opsForValue().get(token);
					if(infoStr==null) {
						throw new BadRequestException("用户token解析异常！");
					}
					redisTemplate.expire(token, 5, TimeUnit.MINUTES);//重置过期时间
					tokenInfo = mapper.readValue(infoStr, ValidateInfo.class);
				}
				userFile = userFileService.findOnePublic(fid);
				if (userDefaultSettings.isEnableAcl() && !validated) {
					if(tokenInfo==null || tokenInfo!=null && !tokenInfo.getKey().equals(String.valueOf(userFile.getUid()))) {
						if (!userFile.getAcl().equals(AccessList.PUBLIC)) {
							req.setAttribute("secreturl", secretUrl);
							try {
								if (preview) {
									req.setAttribute("preview", true);
								}
								req.getRequestDispatcher("/page/validate").forward(req, resp);
								return null;
							} catch (ServletException e) {
								e.printStackTrace();
							}
						}
					}
					//判断filetoken中是否含有fid限制
					//与上面重复了，但是为了逻辑的可读性，放在了这里
					if(tokenInfo!=null && tokenInfo.getFid()!=0 && tokenInfo.getFid()!=userFile.getId()) {
						if (!userFile.getAcl().equals(AccessList.PUBLIC)) {
							req.setAttribute("secreturl", secretUrl);
							try {
								if (preview) {
									req.setAttribute("preview", true);
								}
								req.getRequestDispatcher("/page/validate").forward(req, resp);
								return null;
							} catch (ServletException e) {
								e.printStackTrace();
							}
						}
					}
				}
			} catch (IllegalBlockSizeException | BadPaddingException e1) {
				logger.error("文件下载【{}】异异常", secretUrl, e1);
				throw new BadRequestException("下载链接解析异常！");
			}
		}
		if (userFile == null) {
			throw new BadRequestException("文件资源未找到！");
		}
		if(userFile.isDeleted()) {
			throw new BadRequestException("该文件资源已被删除！");
		}
		FileDetailInfo fileDetailInfo = fileService.find(userFile.getFileId());
		UserInfo userInfo = (UserInfo) req.getSession().getAttribute("user");
		long downloadSpeed = DOWN_SPEED_LIMIT * 1024 * 1024L;
		if (userInfo != null && userInfo.getDownloadSpeedLimit() > downloadSpeed) {
			downloadSpeed = userInfo.getDownloadSpeedLimit();
		}
		if (fileDetailInfo == null) {
			throw new BadRequestException("文件资源未找到！");
		}
		if (System.currentTimeMillis() > fileDetailInfo.getGmtExpired().getTime()) {
			throw new BadRequestException("文件已经过期！");
		}
		InputStream in = fileService.download(fileDetailInfo);
		if (in == null) {
			throw new BadRequestException("文件资源未找到！");
		}
		if (preview && OfficeFileUtils.isOfficeFile(fileDetailInfo.getSuffix())) {
				// TODO 过大的文件应该禁止预览
			previewService.office2Pdf(fileDetailInfo.getId(), resp);
			return null;
		}
		String originalFileName = fileDetailInfo.getOriginalFileName();
		RangeSettings rangeSettings = FileUtil.headerSetting(fileDetailInfo, req, resp, preview);
		long pos = rangeSettings.getStart();
		long contentLength = rangeSettings.getContentLength();
		BufferedInputStream is = null;
		OutputStream os = null;
		try {
			IoUtil.skipFully(in, pos);
			is = new BufferedInputStream(in);
			os = resp.getOutputStream();
			int len = 0;
			long hasUpload = 0;
			// 2KB大小。
			byte[] buffer = new byte[CACHE_BUFFER_SIZE];
			DownloadSpeedLimiter limiter = new DownloadSpeedLimiter(downloadSpeed, CACHE_BUFFER_SIZE);
			while ((len = is.read(buffer)) > 0) {
				os.write(buffer, 0, len);
				os.flush();
				hasUpload += len;
				if (hasUpload == contentLength) {
					logger.info("已经上传字节：" + contentLength);
					break;
				}
				limiter.limit();
			}
		} catch (Exception e) {
			logger.error("文件下载【{}】异常：{}", fid, e.getMessage());
		} finally {
			if (in != null) {
				try {
					if (in instanceof S3ObjectInputStream) {
						((S3ObjectInputStream) in).abort();
						logger.info("中断ceph流=>" + in.hashCode());
					}
					in.close();
				} catch (IOException e) {
					logger.error("文件输入流关闭异常：{}", e.getMessage());
				}
			}
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					logger.error("输入流关闭异常：{}", e.getMessage());
				}
			}
			if (os != null) {
				try {
					os.close();
				} catch (IOException e) {
					logger.error("输出流关闭异常：{}", e.getMessage());
				}
			}
		}
		return ResponseEntity.ok(originalFileName);

	}

	/**
	 * 此方法只用于展示如何提供多线程下载，不推荐使用。
	 * 
	 * @param response
	 * @param request
	 * @param fid
	 * @return
	 * @throws IOException
	 */
//	@GetMapping("/down1")
	@Deprecated
	public ResponseEntity<String> downloadByPart1(HttpServletResponse response, HttpServletRequest request,
			@RequestParam("fid") int fid) throws IOException {
		FileDetailInfo fileDetailInfo = fileService.find(fid);
		String originalFileName = fileDetailInfo.getOriginalFileName();
		// File downloadFile = new
		// File("D:/defonds/book/pattern/SteveJobsZH.pdf");//要下载的文件
		long fileLength = fileDetailInfo.getSize();// 记录文件大小
		long pastLength = 0;// 记录已下载文件大小
		int rangeSwitch = 0;// 0：从头开始的全文下载；1：从某字节开始的下载（bytes=27000-）；2：从某字节开始到某字节结束的下载（bytes=27000-39000）
		long toLength = 0;// 记录客户端需要下载的字节段的最后一个字节偏移量（比如bytes=27000-39000，则这个值是为39000）
		long contentLength = 0;// 客户端请求的字节总量
		String rangeBytes = "";// 记录客户端传来的形如“bytes=27000-”或者“bytes=27000-39000”的内容
		// RandomAccessFile raf = null;//负责读取数据
		OutputStream os = null;// 写出数据
		OutputStream out = null;// 缓冲
		byte b[] = new byte[1024];// 暂存容器

		if (request.getHeader("Range") != null) {// 客户端请求的下载的文件块的开始字节
			response.setStatus(javax.servlet.http.HttpServletResponse.SC_PARTIAL_CONTENT);
			logger.info("request.getHeader(\"Range\")=" + request.getHeader("Range"));
			rangeBytes = request.getHeader("Range").replaceAll("bytes=", "");
			if (rangeBytes.indexOf('-') == rangeBytes.length() - 1) {// bytes=969998336-
				rangeSwitch = 1;
				rangeBytes = rangeBytes.substring(0, rangeBytes.indexOf('-'));
				pastLength = Long.parseLong(rangeBytes.trim());
				contentLength = fileLength - pastLength + 1;// 客户端请求的是 969998336 之后的字节
			} else {// bytes=1275856879-1275877358
				rangeSwitch = 2;
				String temp0 = rangeBytes.substring(0, rangeBytes.indexOf('-'));
				String temp2 = rangeBytes.substring(rangeBytes.indexOf('-') + 1, rangeBytes.length());
				pastLength = Long.parseLong(temp0.trim());// bytes=1275856879-1275877358，从第 1275856879 个字节开始下载
				toLength = Long.parseLong(temp2);// bytes=1275856879-1275877358，到第 1275877358 个字节结束
				contentLength = toLength - pastLength + 1;// 客户端请求的是 1275856879-1275877358 之间的字节
			}
		} else {// 从开始进行下载
			contentLength = fileLength;// 客户端要求全文下载
		}

		/**
		 * 如果设设置了Content-Length，则客户端会自动进行多线程下载。如果不希望支持多线程，则不要设置这个参数。 响应的格式是:
		 * Content-Length: [文件的总大小] - [客户端请求的下载的文件块的开始字节]
		 * ServletActionContext.getResponse().setHeader("Content-Length", new
		 * Long(file.length() - p).toString());
		 */
		response.reset();// 告诉客户端允许断点续传多线程连接下载,响应的格式是:Accept-Ranges: bytes
		response.setHeader("Accept-Ranges", "bytes");// 如果是第一次下,还没有断点续传,状态是默认的 200,无需显式设置;响应的格式是:HTTP/1.1 200 OK
		if (pastLength != 0) {
			// 不是从最开始下载,
			// 响应的格式是:
			// Content-Range: bytes [文件块的开始字节]-[文件的总大小 - 1]/[文件的总大小]
			logger.info("----------------------------不是从开始进行下载！服务器即将开始断点续传...");
			switch (rangeSwitch) {
			case 1: {// 针对 bytes=27000- 的请求
				String contentRange = new StringBuffer("bytes ").append(new Long(pastLength).toString()).append("-")
						.append(new Long(fileLength - 1).toString()).append("/").append(new Long(fileLength).toString())
						.toString();
				logger.info("rangeSwitch={},contenRange:{}", rangeSwitch, contentRange);
				response.setHeader("Content-Range", contentRange);
				break;
			}
			case 2: {// 针对 bytes=27000-39000 的请求
				String contentRange = "bytes " + rangeBytes + "/" + new Long(fileLength).toString();
				logger.info("rangeSwitch={},contenRange:{}", rangeSwitch, contentRange);
				response.setHeader("Content-Range", contentRange);
				break;
			}
			default: {
				break;
			}
			}
		} else {
			// 是从开始下载
			logger.info("----------------------------是从开始进行下载！");
		}

		try {
			response.addHeader("Content-Disposition", "attachment; filename=\"" + originalFileName + "\"");
			response.setContentType(IoUtil.setContentType(originalFileName));// set the MIME type.
			response.addHeader("Content-Length", String.valueOf(contentLength));
			os = response.getOutputStream();
			InputStream in = fileService.download(fileDetailInfo);
			out = new BufferedOutputStream(os);
			// raf = new RandomAccessFile(in, "r");
			DownloadSpeedLimiter limiter = new DownloadSpeedLimiter(DOWN_SPEED_LIMIT * 1024 * 1024, 1024);
			try {
				switch (rangeSwitch) {
				case 0: {// 普通下载，或者从头开始的下载
					// 同1
				}
				case 1: {// 针对 bytes=27000- 的请求
					// raf.seek(pastLength);//形如 bytes=969998336- 的客户端请求，跳过 969998336 个字节
					int n = 0;
					IoUtil.skipFully(in, pastLength);
					while ((n = in.read(b, 0, 1024)) != -1) {
						out.write(b, 0, n);
						limiter.limit();
					}
					break;
				}
				case 2: {// 针对 bytes=27000-39000 的请求
					// raf.seek(pastLength - 1);//形如 bytes=1275856879-1275877358 的客户端请求，找到第
					// 1275856879 个字节
					int n = 0;
					IoUtil.skipFully(in, pastLength - 1);
					long readLength = 0;// 记录已读字节数
					while (readLength <= contentLength - 1024) {// 大部分字节在这里读取
						n = in.read(b, 0, 1024);
						readLength += 1024;
						out.write(b, 0, n);
						limiter.limit();
					}
					if (readLength <= contentLength) {// 余下的不足 1024 个字节在这里读取
						n = in.read(b, 0, (int) (contentLength - readLength));
						out.write(b, 0, n);
					}
					break;
				}
				default: {
					break;
				}
				}
				out.flush();
			} catch (IOException ie) {
				/**
				 * 在写数据的时候， 对于 ClientAbortException 之类的异常， 是因为客户端取消了下载，而服务器端继续向浏览器写入数据时，
				 * 抛出这个异常，这个是正常的。 尤其是对于迅雷这种吸血的客户端软件， 明明已经有一个线程在读取 bytes=1275856879-1275877358，
				 * 如果短时间内没有读取完毕，迅雷会再启第二个、第三个。。。线程来读取相同的字节段， 直到有一个线程读取完毕，迅雷会 KILL
				 * 掉其他正在下载同一字节段的线程， 强行中止字节读出，造成服务器抛 ClientAbortException。 所以，我们忽略这种异常
				 */
				// ignore
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					logger.error(e.getMessage());
				}
			}
		}

		return ResponseEntity.ok(originalFileName);

	}
}
