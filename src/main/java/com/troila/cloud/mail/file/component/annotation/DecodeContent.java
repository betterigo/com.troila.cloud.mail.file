package com.troila.cloud.mail.file.component.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 解密字符串
 * @author haodonglei
 *
 */
@Target(value= {ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DecodeContent {

}
