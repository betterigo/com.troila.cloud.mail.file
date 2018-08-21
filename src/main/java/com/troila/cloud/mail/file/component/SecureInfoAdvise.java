package com.troila.cloud.mail.file.component;

import java.io.UnsupportedEncodingException;
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
			}
			if (param instanceof FileDetailInfo) {
				((FileDetailInfo) param).setOriginalFileName(secret(((FileDetailInfo) param).getOriginalFileName()));
				((FileDetailInfo) param).setSuffix(secret(((FileDetailInfo) param).getSuffix()));
			}
			if (param instanceof UserFile) {
				((UserFile) param).setOriginalFileName(secret(((UserFile) param).getOriginalFileName()));
				((UserFile) param).setSuffix(secret(((UserFile) param).getSuffix()));
			}
		}
	}

	@AfterReturning(returning = "rvt", pointcut = "pointCutTwo()")
	public void afterReturn(JoinPoint joinPoint, Object rvt) {
		if (rvt instanceof FileInfoExt) {
			((FileInfoExt) rvt).setOriginalFileName(unsecret(((FileInfoExt) rvt).getOriginalFileName()));
			((FileInfoExt) rvt).setSuffix(unsecret(((FileInfoExt) rvt).getSuffix()));
			if(entityManager.contains(rvt)) {
				entityManager.clear();
			}
		}
		if (rvt instanceof FileDetailInfo) {
			((FileDetailInfo) rvt).setOriginalFileName(unsecret(((FileDetailInfo) rvt).getOriginalFileName()));
			((FileDetailInfo) rvt).setSuffix(unsecret(((FileDetailInfo) rvt).getSuffix()));
			if(entityManager.contains(rvt)) {
				entityManager.clear();
			}
		}
		if (rvt instanceof UserFile) {
			((UserFile) rvt).setOriginalFileName(unsecret(((UserFile) rvt).getOriginalFileName()));
			((UserFile) rvt).setSuffix(unsecret(((UserFile) rvt).getSuffix()));
			if(entityManager.contains(rvt)) {
				entityManager.clear();
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
					}
				}
			}
		}
	}

	private String secret(String content) {
		try {
			byte[] secret = secureConverter.encode(content.getBytes(CHAR_SET));
			return Base64.getEncoder().encodeToString(secret);
		} catch (IllegalBlockSizeException | BadPaddingException | UnsupportedEncodingException e) {
			logger.error("加密内容发生错误！", e);
		}
		return content;
	}
	
	private String unsecret(String content) {
		try {
			byte[] secret = Base64.getDecoder().decode(content);
			byte[] result = secureConverter.decode(secret);
			return new String(result, CHAR_SET);
		} catch (IllegalBlockSizeException | BadPaddingException | UnsupportedEncodingException e) {
			logger.error("解密内容发生错误！", e);
		}
		return content;
	}
}
