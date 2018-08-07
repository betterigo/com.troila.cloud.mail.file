package com.troila.cloud.mail.file.component.office;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.apache.poi.hslf.usermodel.HSLFShape;
import org.apache.poi.hslf.usermodel.HSLFSlideShow;
import org.apache.poi.hslf.usermodel.HSLFSlideShowImpl;
import org.apache.poi.hslf.usermodel.HSLFTextParagraph;
import org.apache.poi.hslf.usermodel.HSLFTextRun;
import org.apache.poi.hslf.usermodel.HSLFTextShape;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFShape;
import org.apache.poi.xslf.usermodel.XSLFTextParagraph;
import org.apache.poi.xslf.usermodel.XSLFTextRun;
import org.apache.poi.xslf.usermodel.XSLFTextShape;

import com.troila.cloud.mail.file.utils.OfficeFileUtils;


public class POIPptToHtml {

	private final static String PPT = "ppt";
	private final static String PPTX = "pptx";

	public static void pptToHtml(String sourcePath, String targetDir) {
		File pptFile = new File(sourcePath);
		if (pptFile.exists()) {
			try {
				String type = OfficeFileUtils.GetFileExt(sourcePath);
				String pptFileName = pptFile.getName().substring(0, pptFile.getName().lastIndexOf("."));

				if (PPT.equals(type)) {
					String htmlStr = toImage2003(sourcePath, targetDir,pptFileName);
//					OfficeFileUtils.writeFile(htmlStr, targetDir + "/"+pptFileName+".html");
					FileUtils.writeByteArrayToFile(new File(targetDir), htmlStr.getBytes());
				} else if (PPTX.equals(type)) {
					String htmlStr = toImage2007(sourcePath, targetDir, pptFileName);
//					OfficeFileUtils.writeFile(htmlStr, targetDir + "/"+pptFileName+".html");
					FileUtils.writeByteArrayToFile(new File(targetDir), htmlStr.getBytes());
				} else {
					System.out.println("the file is not a ppt");
				}

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			System.out.println("file does not exist!");
		}

	}

	public static String toImage2007(String sourcePath, String targetDir, String pptFileName) throws Exception {
		String htmlStr = "";
		FileInputStream is = new FileInputStream(sourcePath);
		@SuppressWarnings("resource")
		XMLSlideShow ppt = new XMLSlideShow(is);
		is.close();
		OfficeFileUtils.createDir(targetDir);// create html dir
		Dimension pgsize = ppt.getPageSize();
		System.out.println(pgsize.width + "--" + pgsize.height);

		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < ppt.getSlides().size(); i++) {
			try {
				// 防止中文乱码
				for (XSLFShape shape : ppt.getSlides().get(i).getShapes()) {
					if (shape instanceof XSLFTextShape) {
						XSLFTextShape tsh = (XSLFTextShape) shape;
						for (XSLFTextParagraph p : tsh) {
							for (XSLFTextRun r : p) {
								r.setFontFamily("宋体");
							}
						}
					}
				}
				BufferedImage img = new BufferedImage(pgsize.width, pgsize.height, BufferedImage.TYPE_INT_RGB);
				Graphics2D graphics = img.createGraphics();
				// clear the drawing area
				graphics.setPaint(Color.white);
				graphics.fill(new Rectangle2D.Float(0, 0, pgsize.width, pgsize.height));
				// render
				ppt.getSlides().get(i).draw(graphics);
				// save the output
				String imageDir = targetDir + "/" + pptFileName + "/";
				OfficeFileUtils.createDir(imageDir);// create image dir
				String imagePath = imageDir + pptFileName + "-" + (i + 1) + ".png";
				sb.append("<br>");
				sb.append("<img src=" + "\"" + imagePath + "\"" + "/>");
				FileOutputStream out = new FileOutputStream(imagePath);
				javax.imageio.ImageIO.write(img, "png", out);
				out.close();
			} catch (Exception e) {
				System.out.println("第" + i + "张ppt转换出错");
			}
		}
		System.out.println("success");
		htmlStr = sb.toString();

		return htmlStr;
	}

	public static String toImage2003(String sourcePath, String targetDir, String pptFileName) {
		String htmlStr = "";
		try {
			@SuppressWarnings("resource")
			HSLFSlideShow ppt = new HSLFSlideShow(new HSLFSlideShowImpl(sourcePath));
			OfficeFileUtils.createDir(targetDir);// create html dir
			Dimension pgsize = ppt.getPageSize();
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < ppt.getSlides().size(); i++) {
				// 防止中文乱码
				for (HSLFShape shape : ppt.getSlides().get(i).getShapes()) {
					if (shape instanceof HSLFTextShape) {
						HSLFTextShape tsh = (HSLFTextShape) shape;
						for (HSLFTextParagraph p : tsh) {
							for (HSLFTextRun r : p) {
								r.setFontFamily("宋体");
							}
						}
					}
				}
				BufferedImage img = new BufferedImage(pgsize.width, pgsize.height, BufferedImage.TYPE_INT_RGB);
				Graphics2D graphics = img.createGraphics();
				// clear the drawing area
				graphics.setPaint(Color.white);
				graphics.fill(new Rectangle2D.Float(0, 0, pgsize.width, pgsize.height));
				// render
				ppt.getSlides().get(i).draw(graphics);
				String imageDir = targetDir + "/" + pptFileName + "/";
				OfficeFileUtils.createDir(imageDir);// create image dir
				String imagePath = imageDir + pptFileName + "-" + (i + 1) + ".png";
				sb.append("<br>");
				sb.append("<img src=" + "\"" + imagePath + "\"" + "/>");
				FileOutputStream out = new FileOutputStream(imagePath);
				javax.imageio.ImageIO.write(img, "png", out);
				out.close();

			}
			System.out.println("success");
			htmlStr = sb.toString();
		} catch (Exception e) {

		}
		return htmlStr;
	}

	/***
	 * 功能 :调整图片大小
	 * 
	 * @param srcImgPath
	 *            原图片路径
	 * @param distImgPath
	 *            转换大小后图片路径
	 * @param width
	 *            转换后图片宽度
	 * @param height
	 *            转换后图片高度
	 */
	public static void resizeImage(String srcImgPath, String distImgPath, int width, int height) throws IOException {

		File srcFile = new File(srcImgPath);
		Image srcImg = ImageIO.read(srcFile);
		BufferedImage buffImg = null;
		buffImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		buffImg.getGraphics().drawImage(srcImg.getScaledInstance(width, height, Image.SCALE_SMOOTH), 0, 0, null);

		ImageIO.write(buffImg, "JPEG", new File(distImgPath));

	}
}
