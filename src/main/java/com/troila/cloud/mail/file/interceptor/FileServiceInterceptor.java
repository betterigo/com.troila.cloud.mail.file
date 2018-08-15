package com.troila.cloud.mail.file.interceptor;

import com.troila.cloud.mail.file.model.FileDetailInfo;

public interface FileServiceInterceptor {
	public void beforeUpload(FileDetailInfo fileDetailInfo);
	public void afterUpload(FileDetailInfo fileDetailInfo);
	public void afterDownload(FileDetailInfo fileDetailInfo);
}
