package com.troila.cloud.mail.file.component;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.UUID;

import org.jodconverter.DocumentConverter;
import org.jodconverter.document.DefaultDocumentFormatRegistry;
import org.jodconverter.document.DocumentFormat;
import org.jodconverter.office.OfficeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.troila.cloud.mail.file.model.PreviewFile;
import com.troila.cloud.mail.file.utils.InformationStores;

@Component
public class Jodconverter2HtmlConverter implements PreviewConverter{

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	@Autowired
	private DocumentConverter converter;
	
	@Override
	public String toHtml(InputStream in, String suffix) {
		PreviewFile previewFile = new PreviewFile();
		//创建临时文件夹
		String tmpDirName = UUID.randomUUID().toString();
		previewFile.setTmpDir("static/preview/"+tmpDirName);
		String targetFileName = tmpDirName + System.currentTimeMillis()+".html";
		previewFile.setTempFile(targetFileName);
		previewFile.setExpiredTime(new Date(System.currentTimeMillis()+1000*60*2));
		File tmpDir = new File(getClassPath(),previewFile.getTmpDir());
		File targetFile = new File(tmpDir,targetFileName);
		DocumentFormat documentFormat = getDocumentFormat(suffix);
		if(documentFormat == null) {
			return null;
		}
		try {
			converter.convert(in, true).as(documentFormat).to(targetFile).execute();
		} catch (OfficeException e) {
			logger.error("转换office文件失败！",e);
			e.printStackTrace();
			return null;
		}
		InformationStores.getPreviewFileStore().put(tmpDirName, previewFile);
		return tmpDirName+"/"+targetFileName;
	}
	@Override
	public void toPdf(InputStream in,OutputStream out, String suffix) {
		DocumentFormat documentFormat = getDocumentFormat(suffix);
		try {
			converter.convert(in, true).as(documentFormat).to(out).as(DefaultDocumentFormatRegistry.PDF).execute();
		} catch (OfficeException e) {
			logger.error("转换office文件失败！",e);
			e.printStackTrace();
		}
	}
	private DocumentFormat getDocumentFormat(String suffix) {
		DocumentFormat documentFormat = null;
		switch (suffix) {
		case "doc":
			documentFormat = DefaultDocumentFormatRegistry.DOC;
			break;
		case "docx":
			documentFormat = DefaultDocumentFormatRegistry.DOCX;
			break;
		case "odt":
			documentFormat = DefaultDocumentFormatRegistry.ODT;
			break;
		case "ott":
			documentFormat = DefaultDocumentFormatRegistry.OTT;
			break;
		case "rtf":
			documentFormat = DefaultDocumentFormatRegistry.RTF;
			break;
		case "txt":
			documentFormat = DefaultDocumentFormatRegistry.TXT;
			break;
		case "csv":
			documentFormat = DefaultDocumentFormatRegistry.CSV;
			break;
		case "ods":
			documentFormat = DefaultDocumentFormatRegistry.ODS;
			break;
		case "ots":
			documentFormat = DefaultDocumentFormatRegistry.OTS;
			break;
		case "tsv":
			documentFormat = DefaultDocumentFormatRegistry.TSV;
			break;
		case "xls":
			documentFormat = DefaultDocumentFormatRegistry.XLS;
			break;
		case "xlsx":
			documentFormat = DefaultDocumentFormatRegistry.XLSX;
			break;
		case "odp":
			documentFormat = DefaultDocumentFormatRegistry.ODP;
			break;
		case "otp":
			documentFormat = DefaultDocumentFormatRegistry.OTP;
			break;
		case "ppt":
			documentFormat = DefaultDocumentFormatRegistry.PPT;
			break;
		case "pptx":
			documentFormat = DefaultDocumentFormatRegistry.PPTX;
			break;
		default:
			break;
		}
		return documentFormat;
	}

}
