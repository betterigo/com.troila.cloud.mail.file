package com.troila.cloud.mail.file.component;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Optional;

import javax.servlet.http.HttpSession;
import javax.ws.rs.ForbiddenException;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.troila.cloud.mail.file.component.annotation.ValidateFolderAuth;
import com.troila.cloud.mail.file.model.UserFile;
import com.troila.cloud.mail.file.model.UserFolder;
import com.troila.cloud.mail.file.model.UserInfo;
import com.troila.cloud.mail.file.model.fenum.FolderAuth;
import com.troila.cloud.mail.file.repository.UserFileRespository;
import com.troila.cloud.mail.file.service.UserFolderService;

@Component
@Aspect
public class UserFolderAuthAdvise {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private UserFolderService userFolderService;
	
	@Autowired
	private UserFileRespository userFileRespository;

	@Pointcut(value = "@annotation(com.troila.cloud.mail.file.component.annotation.ValidateFolderAuth)")
	public void pointCut() {
	}

	@Before("pointCut()")
	public void doBefore(JoinPoint joinPoint) {
		logger.info("用户文件夹操作权限认证...start");
		Signature signature = joinPoint.getSignature();
		MethodSignature methodSignature = (MethodSignature) signature;
		Method targetMethod = methodSignature.getMethod();
		ValidateFolderAuth targetAnnotation = targetMethod.getAnnotation(ValidateFolderAuth.class);
		FolderAuth auth = targetAnnotation.value();
		int folderId = 0;
		int fileId = 0;
		boolean hasFolderId = false;
		boolean hasFileId = false;
		for (Object param : joinPoint.getArgs()) {
			Field field = null;
			try {
				field = param.getClass().getDeclaredField("folderId");
				if (field != null) {
					Method getMethod = param.getClass().getMethod("get" + first2UpCase(field.getName()),
							new Class<?>[] {});
					Object folderIdObj = getMethod.invoke(param, new Object[] {});
					if (field.getType().equals(int.class)) {
						folderId = (int) folderIdObj;
					}
					hasFolderId = true;
				}
			} catch (NoSuchFieldException | SecurityException | NoSuchMethodException | IllegalAccessException
					| IllegalArgumentException | InvocationTargetException e) {
				// 不需要处理
			}
			try {
				Field fileIdField = param.getClass().getDeclaredField("fileId");
				if (fileIdField != null) {
					Method getMethod = param.getClass().getMethod("get" + first2UpCase(fileIdField.getName()),
							new Class<?>[] {});
					Object fileIdObj = getMethod.invoke(param, new Object[] {});
					if (fileIdField.getType().equals(int.class)) {
						fileId = (int) fileIdObj;
					}
					hasFileId = true;
				}
			} catch (Exception e) {
				//不需要处理
			}
			if(hasFileId && hasFolderId) {
				try {
					HttpSession session = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
							.getRequest().getSession();
					UserInfo user = (UserInfo) session.getAttribute("user");
					int id = 0;
					Field idField = param.getClass().getDeclaredField("id");
					if (idField != null) {
						Method getMethod = param.getClass().getMethod("get" + first2UpCase(idField.getName()),
								new Class<?>[] {});
						Object idObj = getMethod.invoke(param, new Object[] {});
						if (idField.getType().equals(int.class)) {
							id = (int) idObj;
							//获取用户文件..查看是否是自己上传的文件
							Optional<UserFile> userFileOpt = userFileRespository.findById(id);
							if(userFileOpt.isPresent()) {
								UserFile userFile = userFileOpt.get();
								if(userFile.getFileId() == fileId && user.getId() == userFile.getUid()) {
									return;
								}
							}
						}
					}
				} catch (Exception e) {
					//不需要处理
				}
			}
			if (field != null) {
				if (folderId != 0) {
					HttpSession session = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
							.getRequest().getSession();
					UserInfo user = (UserInfo) session.getAttribute("user");
					UserFolder userFolder = userFolderService.getUserFolder(user.getId(), (Integer) folderId);
					if (userFolder == null) {
						logger.error("用户{}权限认证...fail!,reason:{}",user.getName(),"没有此文件夹的用户权限信息!");
						throw new ForbiddenException("没有此文件夹的用户权限信息!");
					}
					String role = userFolder.getAuth().getValue();
					if (!role.toLowerCase().contains(auth.getValue())) {
						logger.error("用户{}权限认证...fail!,reason:{}",user.getName(),"此用户没有上传文件到文件夹:" + folderId + "的权限!");
						throw new ForbiddenException("此用户没有上传文件到文件夹:" + folderId + "的权限!");
					}
					logger.info("用户文件夹操作权限认证...ok,uid:{},authority:{}",user.getId(),auth.getValue());
				}else {					
					logger.info("用户文件夹操作权限认证...ok");
				}
				break;
			}
		}
	}

	private String first2UpCase(String str) {
		if (str != null && str.length() > 0) {
			char c = str.charAt(0);
			c = Character.toUpperCase(c);
			StringBuffer sb = new StringBuffer(str);
			sb = sb.replace(0, 1, String.valueOf(c));
			return sb.toString();
		}
		return str;
	}
}
