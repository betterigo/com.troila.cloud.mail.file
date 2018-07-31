package com.troila.cloud.mail.file.proxy;

import java.lang.reflect.Method;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;

import com.troila.cloud.mail.file.interceptor.FileServiceInterceptor;
import com.troila.cloud.mail.file.model.FileDetailInfo;


public class EnhancerInterceptor implements MethodInterceptor{

	@Autowired
	private FileServiceInterceptor interceptor;
	
	public EnhancerInterceptor(FileServiceInterceptor interceptor) {
		super();
		this.interceptor = interceptor;
	}

	public EnhancerInterceptor() {
		super();
	}

	@Override
	public Object intercept(Object obj, Method method, Object[] params, MethodProxy proxy) throws Throwable {
		Object result = null;
		if(interceptor != null && method.getName().equals("uploadPart")) {
			for(Object param : params) {
				if(param instanceof FileDetailInfo) {					
					interceptor.beforeUpload((FileDetailInfo)param);
					break;
				}
			}
			result = proxy.invokeSuper(obj, params);
			interceptor.afterUpload((FileDetailInfo)result);
		}else {
			result = proxy.invokeSuper(obj, params);
		}
		return result;
	}

}
