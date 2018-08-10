package com.troila.cloud.mail.file.security.error;

import java.util.Date;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
@RestController
public class JsonTypeErrorController implements ErrorController{

	private static final String PATH = "/error";
	
	@Autowired
	private ErrorAttributes errorAttributes;
	@Override
	public String getErrorPath() {
		return PATH;
	}

	@RequestMapping(path=PATH,produces=MediaType.APPLICATION_JSON)
	public ErrorBody sendError(HttpServletRequest req, HttpServletResponse res) {
//		RequestAttributes attrs = new ServletRequestAttributes(req);
		WebRequest webRequest = new ServletWebRequest(req);
		Map<String,Object> errorAttrs = errorAttributes.getErrorAttributes(webRequest, true);
		ErrorBody errorBody = new ErrorBody();
		errorBody.setMessage((String) errorAttrs.get("message"));
		errorBody.setStatus((Integer) errorAttrs.get("status"));
		errorBody.setPath((String) errorAttrs.get("path"));
		errorBody.setTimestamp((Date)errorAttrs.get("timestamp"));
		return errorBody;
		
	}
}
