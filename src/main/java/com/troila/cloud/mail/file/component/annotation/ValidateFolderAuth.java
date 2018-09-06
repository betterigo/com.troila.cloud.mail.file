package com.troila.cloud.mail.file.component.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.troila.cloud.mail.file.model.fenum.FolderAuth;

/**
 * 检验用户当前操作文件夹的权限
 * @author haodonglei
 *
 */
@Target(value= {ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ValidateFolderAuth {
	FolderAuth value() default FolderAuth.READ;
}
