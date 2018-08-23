package com.troila.cloud.mail.file.component;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Base64;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import com.troila.cloud.mail.file.config.settings.UserDefaultSettings;
import com.troila.cloud.mail.file.model.ExpireBeforeUserFile;
import com.troila.cloud.mail.file.model.FileDetailInfo;
import com.troila.cloud.mail.file.model.FileInfoExt;
import com.troila.cloud.mail.file.model.UserFile;

@Component
@Aspect
public class SecureInfoAdvise {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private static final String CHAR_SET = "UTF-8";

	@Autowired
	private SecureConverter secureConverter;
	
	@Autowired
	private DownloadUrlSecureConverter downloadUrlSecureConverter;
	
	@Autowired
	private UserDefaultSettings userDefaultSettings;

	@PersistenceContext
	private EntityManager entityManager;
	
	@Pointcut(value = "@annotation(com.troila.cloud.mail.file.component.annotation.SecureContent)")
	public void pointCut() {
	}

	@Pointcut(value = "@annotation(com.troila.cloud.mail.file.component.annotation.DecodeContent)")
	public void pointCutTwo() {
	}

	@Before("pointCut()")
	public void doBefore(JoinPoint point) {
		// 查看参数类型
		for (Object param : point.getArgs()) {
			if (param instanceof FileInfoExt) {
				((FileInfoExt) param).setOriginalFileName(secret(((FileInfoExt) param).getOriginalFileName()));
				((FileInfoExt) param).setSuffix(secret(((FileInfoExt) param).getSuffix()));
				((FileInfoExt) param).setSecretKey(secret(((FileInfoExt) param).getSecretKey()));
			}
			if (param instanceof FileDetailInfo) {
				((FileDetailInfo) param).setOriginalFileName(secret(((FileDetailInfo) param).getOriginalFileName()));
				((FileDetailInfo) param).setSuffix(secret(((FileDetailInfo) param).getSuffix()));
				((FileDetailInfo) param).setSecretKey(secret(((FileDetailInfo) param).getSecretKey()));
			}
			if (param instanceof UserFile) {
				((UserFile) param).setOriginalFileName(secret(((UserFile) param).getOriginalFileName()));
				((UserFile) param).setSuffix(secret(((UserFile) param).getSuffix()));
				((UserFile) param).setSecretKey(secret(((UserFile) param).getSecretKey()));
			}
		}
	}

	@AfterReturning(returning = "rvt", pointcut = "pointCutTwo()")
	public void afterReturn(JoinPoint joinPoint, Object rvt) {
		if (rvt instanceof FileInfoExt) {
			((FileInfoExt) rvt).setOriginalFileName(unsecret(((FileInfoExt) rvt).getOriginalFileName()));
			((FileInfoExt) rvt).setSuffix(unsecret(((FileInfoExt) rvt).getSuffix()));
			((FileInfoExt) rvt).setSecretKey(unsecret(((FileInfoExt) rvt).getSecretKey()));
			if(entityManager.contains(rvt)) {
				entityManager.clear();
			}
		}
		if (rvt instanceof FileDetailInfo) {
			((FileDetailInfo) rvt).setOriginalFileName(unsecret(((FileDetailInfo) rvt).getOriginalFileName()));
			((FileDetailInfo) rvt).setSuffix(unsecret(((FileDetailInfo) rvt).getSuffix()));
			((FileDetailInfo) rvt).setSecretKey(unsecret(((FileDetailInfo) rvt).getSecretKey()));
			if(entityManager.contains(rvt)) {
				entityManager.clear();
			}
		}
		if (rvt instanceof UserFile) {
			((UserFile) rvt).setOriginalFileName(unsecret(((UserFile) rvt).getOriginalFileName()));
			((UserFile) rvt).setSuffix(unsecret(((UserFile) rvt).getSuffix()));
			((UserFile) rvt).setSecretKey(unsecret(((UserFile) rvt).getSecretKey()));
			createDownloadUrl((UserFile) rvt);
			if(entityManager.contains(rvt)) {
				entityManager.clear();
			}
		}
		if (rvt instanceof ExpireBeforeUserFile) {
			entityManager.clear();
			ExpireBeforeUserFile ebuf = (ExpireBeforeUserFile)rvt;
			for (UserFile uf : ebuf.getExpireBefores()) {
				uf.setOriginalFileName(unsecret(uf.getOriginalFileName()));
				uf.setSuffix(unsecret(uf.getSuffix()));
				uf.setSecretKey(unsecret(uf.getSecretKey()));
			}
			for (UserFile uf : ebuf.getExpireBeforesBefore()) {
				uf.setOriginalFileName(unsecret(uf.getOriginalFileName()));
				uf.setSuffix(unsecret(uf.getSuffix()));
				uf.setSecretKey(unsecret(uf.getSecretKey()));
			}
		}
		if(rvt instanceof Page<?>) {
			List<?> list = ((Page<?>) rvt).getContent();
			if(!list.isEmpty()) {
				Object testObj = list.get(0);
				if(testObj instanceof UserFile) {
					entityManager.clear();
					for(Object item:list) {
						((UserFile) item).setOriginalFileName(unsecret(((UserFile) item).getOriginalFileName()));
						((UserFile) item).setSuffix(unsecret(((UserFile) item).getSuffix()));
						((UserFile) item).setSecretKey(unsecret(((UserFile) item).getSecretKey()));
						createDownloadUrl((UserFile) item);
					}
				}
			}
		}
	}

	private String secret(String content) {
		if(content==null) {
			return null;
		}
		try {
			byte[] secret = secureConverter.encode(content.getBytes(CHAR_SET));
			return Base64.getEncoder().encodeToString(secret);
		} catch (IllegalBlockSizeException | BadPaddingException | UnsupportedEncodingException e) {
			logger.error("加密内容发生错误！", e);
		}
		return content;
	}
	
	private String unsecret(String content) {
		if(content==null) {
			return null;
		}
		try {
			byte[] secret = Base64.getDecoder().decode(content);
			byte[] result = secureConverter.decode(secret);
			return new String(result, CHAR_SET);
		} catch (IllegalBlockSizeException | BadPaddingException | UnsupportedEncodingException e) {
			logger.error("解密内容发生错误！", e);
		}
		return content;
	}
	
	private String secretUrl(String content) {
		if(content==null) {
			return null;
		}
		try {
			byte[] secret = downloadUrlSecureConverter.encode(content.getBytes(CHAR_SET));
			return Base64.getEncoder().encodeToString(secret);
		} catch (IllegalBlockSizeException | BadPaddingException | UnsupportedEncodingException e) {
			logger.error("加密url发生错误！", e);
		}
		return content;
	}
	
	private void createDownloadUrl(UserFile userFile) {
		if(userDefaultSettings.isEnableAcl()) {
			switch (userFile.getAcl()) {
			case PUBLIC:
				userFile.setDownloadUrl(secretUrl(String.valueOf(userFile.getId())));
				break;
			case PROTECT:
			case PRIVATE:
				String urlStr = userFile.getId()+"&withkey="+userFile.getSecretKey();
				userFile.setDownloadUrl(secretUrl(urlStr));
				break;
			default:
				userFile.setDownloadUrl(secretUrl(String.valueOf(userFile.getId())));
				break;
			}
		}else {
			userFile.setDownloadUrl(secretUrl(String.valueOf(userFile.getId())));
		}
		try {
			userFile.setDownloadUrl(URLEncoder.encode(userFile.getDownloadUrl(), CHAR_SET));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
}
