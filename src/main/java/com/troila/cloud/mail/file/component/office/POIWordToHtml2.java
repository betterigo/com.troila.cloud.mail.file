package com.troila.cloud.mail.file.component.office;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.UUID;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.codec.binary.Base64;
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
public class POIWordToHtml2 implements PreviewConverter{
	private static final String DEFAULT_ENCODING = "UTF-8";// UTF-8

	public String wordToHtml(InputStream source,String suffix,String charSet){
		String ext = suffix;
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
						String imageBase64String = Base64.encodeBase64String(content);
						return  "data:image/gif;base64,"+imageBase64String;
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
//				FileUtils.writeFile(new String(out.toByteArray()), targetPath);
				content = out.toString();
				System.out.println("*****doc转html 转换结束...*****");
			} else if (ext.equals("docx")) {
				// 1) 加载word文档生成 XWPFDocument对象
//				InputStream in = new FileInputStream(source);
				XWPFDocument document = new XWPFDocument(source);
				// 2) 解析 XHTML配置 (这里设置IURIResolver来设置图片存放的目录)
				String path = System.getProperty("java.io.tmpdir");
				UUID uuid = UUID.randomUUID();
				String tempImagePath = path + "/" + uuid.toString();//定义临时图片存储路径
				XHTMLOptions options = XHTMLOptions.create();
				options.setExtractor(new FileImageExtractor(new File(tempImagePath)));
				options.URIResolver(new BasicURIResolver(tempImagePath));
				options.setOmitHeaderFooterPages(false);
				// 3) 将 XWPFDocument转换成XHTML
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				XHTMLConverter.getInstance().convert(document, baos, options);
				baos.close();
				content = baos.toString();
				//处理临时文件
				String middleImageDirStr = "/word/media";
				String imageDirStr = tempImagePath + middleImageDirStr;
				File imageDir = new File(imageDirStr);
				String[] imageList = imageDir.list();
				if(imageList!=null) {
					for(int i = 0;i<imageList.length;i++) {
						String imagePath = imageDirStr+"/"+imageList[i];
						File image = new File(imagePath);
						FileInputStream in = new FileInputStream(image);
						FileChannel channel = in.getChannel();
						ByteBuffer byteBuffer = ByteBuffer.allocate((int) channel.size());
						while(channel.read(byteBuffer)>0) {
							
						};
						in.close();
						String imageBase64String = Base64.encodeBase64String(byteBuffer.array());
						content = content.replace(imagePath, "data:image/gif;base64,"+imageBase64String);
					}
				}
				//清理临时路径
				File tempDir = new File(tempImagePath);
				FileUtils.deleteDirectory(tempDir);
				System.out.println("*****docx转html 转换结束...*****");
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(source!=null) {
				try {
					source.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return content;
	}

	@Override
	public String toHtml(InputStream in,String suffix,String charSet) {
		return this.wordToHtml(in, suffix, charSet);
	}

	@Override
	public String toHtml(InputStream in,String suffix) {
		return this.wordToHtml(in, suffix, DEFAULT_ENCODING);
	}

}
