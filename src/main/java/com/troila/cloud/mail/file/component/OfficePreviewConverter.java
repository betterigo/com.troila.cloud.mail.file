package com.troila.cloud.mail.file.component;

import java.io.InputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.troila.cloud.mail.file.component.office.POIExcel2Html;
import com.troila.cloud.mail.file.component.office.POIWord2Html;

@Component
public class OfficePreviewConverter implements PreviewConverter {

	private static final String DEFAULT_ENCODING = "UTF-8";// UTF-8
	
	@Autowired
	private POIWord2Html word2Html;
	
	@Autowired
	private POIExcel2Html excel2Html;
	
	
	@Override
	public String toHtml(InputStream in, String suffix, String charSet) {
		String content = null;
		if(suffix == null) {
			return null;
		}
		switch (suffix) {
		case "doc":
		case ".doc":
		case "docx":
		case ".docx":	
			content = word2Html.wordToHtml(in, suffix, charSet);
			break;
		case "xls":
		case ".xls":
		case "xlsx":
		case ".xlsx":
			content = excel2Html.excelToHtml(in, true, charSet);
		case "ppt":
		case "pptx":
		case ".ppt":
		case ".pptx":
		default:
			break;
		}
		return content;
	}

	@Override
	public String toHtml(InputStream in, String suffix) {
		return toHtml(in, suffix, DEFAULT_ENCODING);
	}

}
