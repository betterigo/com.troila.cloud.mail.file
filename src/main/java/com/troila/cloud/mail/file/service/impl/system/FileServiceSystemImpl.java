package com.troila.cloud.mail.file.service.impl.system;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.troila.cloud.mail.file.config.constant.StorageBuckets;
import com.troila.cloud.mail.file.config.settings.StorageSettings;
import com.troila.cloud.mail.file.config.settings.SystemFileWriteMode;
import com.troila.cloud.mail.file.model.FileDetailInfo;
import com.troila.cloud.mail.file.model.FileHandler;
import com.troila.cloud.mail.file.model.FileInfo;
import com.troila.cloud.mail.file.model.FileInfoExt;
import com.troila.cloud.mail.file.model.ProgressInfo;
import com.troila.cloud.mail.file.repository.FileDetailInfoRepositoty;
import com.troila.cloud.mail.file.repository.FileInfoExtRepository;
import com.troila.cloud.mail.file.repository.FileInfoRepository;
import com.troila.cloud.mail.file.service.FileService;
import com.troila.cloud.mail.file.utils.FileTypeUtil;

public class FileServiceSystemImpl implements FileService {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	
	private static Map<String, FileHandler> fileStore = new ConcurrentHashMap<>();
	
	private static Map<String, ProgressInfo> progressStore = new ConcurrentHashMap<>();
	
	private static final int BYTE_ARRAY_LENGTH = 1024;
	
	@Autowired
	private FileInfoRepository fileInfoRepository;
	
	@Autowired
	private FileDetailInfoRepositoty fileDetailInfoRepositoty;
	
	@Autowired
	private FileInfoExtRepository fileInfoExtRepository;
	
	@Autowired
	private SystemFileWriteMode wm;

	private File rootPath;
	
	private File cachePath;
	
	private Map<String, File> bucketMap = new HashMap<>();
//	
//	public FileServiceSystemImpl(SystemFileWriteMode wm) {
//		super();
//		this.wm = wm;
//	}

	public FileServiceSystemImpl(StorageSettings storageSettings) {
		super();
		File file = new File(storageSettings.getRootpath());
		File cache = new File(storageSettings.getRootpath()+"/cache");
		if(!file.exists()) {
			file.mkdirs();
		}
		if(file.exists() && !file.isDirectory()) {
			file.mkdirs();
		}
		if(!cache.exists()) {
			cache.mkdirs();
		}
		if(cache.exists() && !cache.isDirectory()) {
			cache.mkdirs();
		}
		rootPath = file;
		cachePath = cache;
		for(String bucket : StorageBuckets.buckets) {
			File f = new File(rootPath, bucket);
			if(!f.exists() && !f.isDirectory()) {
				f.mkdirs(); 
			}
			bucketMap.put(bucket, f);
			
		}
	}

	@Override
	public FileInfo updateFileInfo(FileInfo fileInfo) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public FileInfo find(String md5) {
		List<FileInfo> fileInfos = fileInfoRepository.findByMd5(md5);
		if(fileInfos==null || fileInfos.isEmpty()) {
			return null;
		}
		return fileInfos.get(0);
	}

	@Override
	public FileDetailInfo find(int fid) {
		return fileDetailInfoRepositoty.getOne(fid);
	}

	@Override
	public FileInfoExt saveInfoExt(FileInfoExt fileInfoExt) {
		FileInfoExt temp = new FileInfoExt();
		temp.setBaseFid(fileInfoExt.getBaseFid());
		temp.setSuffix(fileInfoExt.getSuffix());
		temp.setOriginalFileName(fileInfoExt.getOriginalFileName());
		temp.setFileType(FileTypeUtil.distinguishFileType(fileInfoExt.getSuffix()));
		temp.setGmtCreate(new Date());
		return fileInfoExtRepository.save(temp);
	}

	@Override
	public boolean deleteFile(int fid) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public FileDetailInfo uploadPart(InputStream in, int index, FileDetailInfo fileInfo, long size) {
		if(wm.getMode()!=null && wm.getMode().equals("temp")) {			
			return withTempFileMode(in, index, fileInfo, size);
		}else {
			return withRandomAccessFileMode(in, index, fileInfo, size);
		}
	}
	
	private FileDetailInfo withRandomAccessFileMode(InputStream in, int index, FileDetailInfo fileInfo, long size) {
		
		String md5 = fileInfo.getMd5();
		String uploadId = fileInfo.getUploadId();
		FileHandler fileHandler = null;
		RandomAccessFile raf = null;
		if(fileStore.get(uploadId)==null) {
			logger.info("初始化文件{}上传...",fileInfo.getOriginalFileName());
			fileHandler = new FileHandler();
			fileHandler.setTotalPart(fileInfo.getTotalPart());
			fileInfo.setStartTime(System.currentTimeMillis());
			try {
				File newFile = new File(bucketMap.get(fileInfo.getBucket()),fileInfo.getFileName());
				raf = new RandomAccessFile(newFile, "rw");
				fileHandler.setRaf(raf);
			} catch (FileNotFoundException e) {
				logger.error("初始化文件{}上传...失败！信息:{}",fileInfo.getOriginalFileName(),e);
				e.printStackTrace();
			}
			fileHandler.setMd5(md5);
			fileHandler.setCacheFolder(cachePath);
			fileStore.put(uploadId, fileHandler);
			logger.info("初始化文件{}上传...完毕！",fileInfo.getOriginalFileName());
		}else {
			fileHandler = fileStore.get(uploadId);
			raf = fileHandler.getRaf();
		}
		byte[] tempByte = new byte[BYTE_ARRAY_LENGTH];
		int len;
		try {
			raf.seek(fileInfo.getPartMap().get(index).getStart());//设定起始点
			while ((len = in.read(tempByte)) != -1) {
				raf.write(tempByte, 0, len);
			}
			fileInfo.partDone(index);
			logger.info("上传ID【{}】:已经上传文件【{}】编号为【{}】的分块,大小:{}",fileInfo.getUploadId(),fileInfo.getOriginalFileName(),index,size);
		} catch (IOException e) {
			logger.error("上传文件【{}】出现异常,信息:{}",fileInfo.getOriginalFileName(),e);
			e.printStackTrace();
		}
		if(fileInfo.isComplete()) {
			try {
				raf.close();
				fileStore.remove(uploadId);
			} catch (IOException e) {
				e.printStackTrace();
			}// 关闭文件流
		}
		ProgressInfo progressInfo = null;
		if(progressStore.get(uploadId)==null) {			
			progressInfo = new ProgressInfo();
			progressInfo.setMd5(md5);
			progressInfo.setTotalSize(fileInfo.getSize());
			progressInfo.setUploadSize(size);
			progressStore.put(uploadId, progressInfo);
		}else {
			progressInfo = progressStore.get(uploadId);
			progressInfo.setUploadSize(progressInfo.getUploadSize() + size);
		}
		long usedTime = System.currentTimeMillis() - fileInfo.getStartTime();
		progressInfo.setUsedTime(usedTime);
		progressInfo.setSpeed((1000 * progressInfo.getUploadSize()/usedTime)/1024); //KB/S
		progressInfo.setLeftTime((long) ((progressInfo.getTotalSize() - progressInfo.getUploadSize()) / progressInfo.getSpeed()));
		progressInfo.setPercent((double)progressInfo.getUploadSize() / progressInfo.getTotalSize());
		fileInfo.setProgressInfo(progressInfo);
		return fileInfo;
		
	}

	private FileDetailInfo withTempFileMode(InputStream in, int index, FileDetailInfo fileInfo, long size) {
		String md5 = fileInfo.getMd5();
		String uploadId = fileInfo.getUploadId();
		FileHandler fileHandler = null;
		if(fileStore.get(uploadId) == null) {
			logger.info("初始化文件{}上传...",fileInfo.getOriginalFileName());
			fileHandler = new FileHandler();
			fileHandler.setTotalPart(fileInfo.getTotalPart());
			fileInfo.setStartTime(System.currentTimeMillis());
			try {
				OutputStream out = new FileOutputStream(new File(bucketMap.get(fileInfo.getBucket()),fileInfo.getFileName()));
				fileHandler.setOut(out);
			} catch (FileNotFoundException e) {
				logger.error("初始化文件{}上传...失败！信息:{}",fileInfo.getOriginalFileName(),e);
				e.printStackTrace();
			}
			fileHandler.setMd5(md5);
			fileHandler.setCacheFolder(cachePath);
			fileStore.put(uploadId, fileHandler);
			logger.info("初始化文件{}上传...完毕！",fileInfo.getOriginalFileName());
		}else {
			fileHandler = fileStore.get(uploadId);
		}
		try {			
			if(fileHandler.getCurrentPart() + 1 == index) {
				writeToFile(fileHandler.getOut(), in);
				fileHandler.setCurrentPart(fileHandler.getCurrentPart() + 1);
			}else {					
				createNewTempFile(fileInfo, index, fileHandler,in);
			}
			//查看缓存文件，按是否有符合的文件
			writeTmpFile(fileHandler, fileHandler.getCurrentPart() + 1);
			fileHandler.setUploadSize(fileHandler.getUploadSize()+size);
		} catch (IOException e) {
			logger.error("上传文件【{}】出现异常,信息:{}",fileInfo.getOriginalFileName(),e);
		}
		//判断是否已经上传完毕
		if (fileHandler != null && fileHandler.getCurrentPart() + 1 == fileHandler.getTotalPart()) {
			try {
				fileHandler.getOut().close();
				for (File f : fileHandler.getCacheFiles().values()) {
					logger.info("清理{}缓存文件", f.getName());
					f.delete();
				}
				fileStore.remove(uploadId);
			} catch (IOException e) {
				e.printStackTrace();
			}// 关闭文件流
			
		}
		ProgressInfo progressInfo = null;
		if(progressStore.get(uploadId)==null) {			
			 progressInfo = new ProgressInfo();
			 progressInfo.setMd5(md5);
			 progressInfo.setTotalSize(fileInfo.getSize());
			 progressInfo.setUploadSize(size);
			 progressStore.put(uploadId, progressInfo);
		}else {
			progressInfo = progressStore.get(uploadId);
			progressInfo.setUploadSize(progressInfo.getUploadSize() + size);
		}
		long usedTime = System.currentTimeMillis() - fileInfo.getStartTime();
		progressInfo.setUsedTime(usedTime);
		progressInfo.setSpeed((1000 * progressInfo.getUploadSize()/usedTime)/1024); //KB/S
		progressInfo.setLeftTime((long) ((progressInfo.getTotalSize() - progressInfo.getUploadSize()) / progressInfo.getSpeed()));
		progressInfo.setPercent((double)progressInfo.getUploadSize() / progressInfo.getTotalSize());
		fileInfo.setProgressInfo(progressInfo);
		return fileInfo;
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
	
	private void createNewTempFile(FileDetailInfo fileInfo, int index, FileHandler handler,InputStream in)
			throws IOException, FileNotFoundException {
		UUID uuid = UUID.randomUUID();
		String fileName = uuid.toString().replaceAll("-", "");
		logger.info("创建上传文件【{}】的临时文件{}", fileInfo.getOriginalFileName(), index + "_" + fileName + ".tmp");
		File tmpFile = new File(handler.getCacheFolder(), index + "_" + fileName + ".tmp");
		System.out.println(tmpFile.getAbsolutePath());
		byte[] b = new byte[BYTE_ARRAY_LENGTH];
		int len;
		OutputStream out = new FileOutputStream(tmpFile);
		while ((len = in.read(b)) != -1) {
			out.write(b, 0, len);
		}
		out.close();
		handler.getCacheFiles().put(index, tmpFile);
	}
	
	private void writeToFile(OutputStream out,InputStream in) throws IOException {
		byte[] byteArray = new byte[BYTE_ARRAY_LENGTH];
		int len;
		while((len=in.read(byteArray))!=-1) {
			out.write(byteArray, 0, len);
		}
	}
	
	@Override
	public InputStream download(FileDetailInfo fileDetailInfo) {
		File file = new File(bucketMap.get(fileDetailInfo.getFileType().getValue()),fileDetailInfo.getFileName());
		try {
			return new FileInputStream(file);
		} catch (FileNotFoundException e) {
			logger.error("文件【{}】没有在文件存储中找到对应文件名为【{}】的文件，可能已经被删除！",fileDetailInfo.getOriginalFileName(),fileDetailInfo.getFileName(),e);
		}
		return null;
	}
	

}
