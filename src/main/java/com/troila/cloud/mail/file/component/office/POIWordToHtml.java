package com.troila.cloud.mail.file.component.office;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.UUID;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.FileUtils;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.converter.PicturesManager;
import org.apache.poi.hwpf.converter.WordToHtmlConverter;
import org.apache.poi.hwpf.usermodel.PictureType;
import org.apache.poi.xwpf.converter.core.BasicURIResolver;
import org.apache.poi.xwpf.converter.core.FileImageExtractor;
import org.apache.poi.xwpf.converter.xhtml.XHTMLConverter;
import org.apache.poi.xwpf.converter.xhtml.XHTMLOptions;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;

import com.troila.cloud.mail.file.component.PreviewConverter;

@Component
public class POIWordToHtml implements PreviewConverter{
	private static final String DEFAULT_ENCODING = "UTF-8";// UTF-8

	public String wordToHtml(InputStream source, String picturesPath,String targetPath,String suffix,String uuid,String charSet){
		String ext = suffix;
		File root = getClassPath();
		File picturesDir = new File(root,picturesPath);
		if (!picturesDir.isDirectory()) {
			picturesDir.mkdirs();
		}
		File targetFile = new File(root,targetPath);
		if(!targetFile.exists()) {
			targetFile.mkdirs();
		}
		String content = null;
		try {
			if (ext.equals("doc")) {
				HWPFDocument wordDocument = new HWPFDocument(source);
				WordToHtmlConverter wordToHtmlConverter = new WordToHtmlConverter(
						DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument());
				wordToHtmlConverter.setPicturesManager(new PicturesManager() {
					@Override
					public String savePicture(byte[] content, PictureType pictureType, String suggestedName,
							float widthInches, float heightInches) {
						File file = new File(picturesDir, suggestedName);
						FileOutputStream fos = null;
						try {
							fos = new FileOutputStream(file);
							fos.write(content);
							fos.close();
						} catch (Exception e) {
							e.printStackTrace();
						}
						return  "preview/"+uuid+"/images/" + suggestedName;
					}
				});
				wordToHtmlConverter.processDocument(wordDocument);
				Document htmlDocument = wordToHtmlConverter.getDocument();
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				DOMSource domSource = new DOMSource(htmlDocument);
				StreamResult streamResult = new StreamResult(out);

				TransformerFactory tf = TransformerFactory.newInstance();
				Transformer serializer = tf.newTransformer();
				serializer.setOutputProperty(OutputKeys.ENCODING,charSet);
				serializer.setOutputProperty(OutputKeys.INDENT, "yes");
				serializer.setOutputProperty(OutputKeys.METHOD, "html");
				serializer.transform(domSource, streamResult);
				out.close();
				FileUtils.writeByteArrayToFile(new File(targetFile,"index.html"), out.toByteArray());
//				FileUtils.writeFile(new String(out.toByteArray()), targetPath);
				content = out.toString();
				System.out.println("*****doc转html 转换结束...*****");
			} else if (ext.equals("docx")) {
				// 1) 加载word文档生成 XWPFDocument对象
//				InputStream in = new FileInputStream(source);
				XWPFDocument document = new XWPFDocument(source);
				// 2) 解析 XHTML配置 (这里设置IURIResolver来设置图片存放的目录)
				XHTMLOptions options = XHTMLOptions.create();
				options.setExtractor(new FileImageExtractor(picturesDir));
				options.URIResolver(new BasicURIResolver(picturesPath));
				// 3) 将 XWPFDocument转换成XHTML
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				XHTMLConverter.getInstance().convert(document, baos, options);
				baos.close();
				content = baos.toString();
//				FileUtils.writeFile(content, targetPath);
				FileUtils.writeByteArrayToFile(new File(targetFile,"index.html"), content.getBytes());
				System.out.println("*****docx转html 转换结束...*****");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return content;
	}

	@Override
	public String toHtml(InputStream in,String suffix,String charSet) {
		UUID uuid = UUID.randomUUID();
		String folderName = uuid.toString();
		this.wordToHtml(in, "static/preview/"+folderName+"/images", "static/preview/"+folderName+"/index.html", suffix,folderName, charSet);
		return folderName;
	}

	@Override
	public String toHtml(InputStream in,String suffix) {
		UUID uuid = UUID.randomUUID();
		String folderName = uuid.toString();
		this.wordToHtml(in, "static/preview/"+folderName+"/images", "static/preview/"+folderName, suffix,folderName, DEFAULT_ENCODING);
		return folderName;
	}

//	public static void main(String[] args) {
//		wordToHtml("D://test.doc", "D://upload", "D://aaabbb.html");
//	}
}
