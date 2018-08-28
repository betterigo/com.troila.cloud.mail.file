package com.troila.cloud.mail.file.service;

import javax.servlet.http.HttpServletResponse;

public interface PreviewService {
	public void office2Html(int fid, HttpServletResponse response);
	public void office2Pdf(int fid, HttpServletResponse response);
}
