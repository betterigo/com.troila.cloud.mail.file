package com.troila.cloud.mail.file.utils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.amazonaws.services.s3.model.InitiateMultipartUploadResult;
import com.amazonaws.services.s3.model.PartETag;
import com.troila.cloud.mail.file.model.FileDetailInfo;
import com.troila.cloud.mail.file.model.FileHandler;
import com.troila.cloud.mail.file.model.PreviewFile;
import com.troila.cloud.mail.file.model.ProgressInfo;

public class InformationStores {
	
	private static Map<String, FileDetailInfo> fileInfosStore = new ConcurrentHashMap<>();
	
	private static Map<String, InitiateMultipartUploadResult> cephStore = new ConcurrentHashMap<>();
	
	private static Map<String, List<PartETag>> eTagtStore = new ConcurrentHashMap<>();
	
	private static Map<String, ProgressInfo> progressStore = new ConcurrentHashMap<>();
	
	private static Map<String, FileHandler> fileStore = new ConcurrentHashMap<>();

	private static Map<String, PreviewFile> previewFileStore = new ConcurrentHashMap<>();
	
	public static Map<String, FileDetailInfo> getFileInfosStore() {
		return fileInfosStore;
	}

	public static Map<String, InitiateMultipartUploadResult> getCephStore() {
		return cephStore;
	}

	public static Map<String, List<PartETag>> geteTagtStore() {
		return eTagtStore;
	}

	public static Map<String, ProgressInfo> getProgressStore() {
		return progressStore;
	}

	public static Map<String, FileHandler> getFileStore() {
		return fileStore;
	}

	public static Map<String, PreviewFile> getPreviewFileStore() {
		return previewFileStore;
	}
	
}
