package com.troila.cloud.mail.file.component;

import java.io.InputStream;

public interface PreviewConverter {
	/**
	 * 把文件转换为html格式预览
	 * @return
	 */
	public String toHtml(InputStream in,String suffix,String charSet);
	
	public String toHtml(InputStream in,String suffix);
}
