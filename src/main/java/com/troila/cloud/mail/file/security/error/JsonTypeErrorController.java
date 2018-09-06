package com.troila.cloud.mail.file.security.error;

import java.util.Date;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
	private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());
	@Autowired
	private ErrorAttributes errorAttributes;
	@Override
	public String getErrorPath() {
		return PATH;
	}

	@RequestMapping(path=PATH,produces=MediaType.APPLICATION_JSON)
	public ErrorBody sendError(HttpServletRequest req, HttpServletResponse res) {
		WebRequest webRequest = new ServletWebRequest(req);
		Throwable e = errorAttributes.getError(webRequest);
		LOGGER.error("",e);
		Map<String,Object> errorAttrs = errorAttributes.getErrorAttributes(webRequest, true);
		ErrorBody errorBody = new ErrorBody();
		errorBody.setMessage((String) errorAttrs.get("message"));
		if(e instanceof WebApplicationException) {
			errorBody.setStatus(((WebApplicationException) e).getResponse().getStatus());
			res.setStatus(((WebApplicationException) e).getResponse().getStatus());
		}else {
			errorBody.setStatus((Integer) errorAttrs.get("status"));
		}
		errorBody.setPath((String) errorAttrs.get("path"));
		errorBody.setTimestamp((Date)errorAttrs.get("timestamp"));
		res.setHeader("Access-Control-Allow-Origin", "*"); //不再spring mvc中，需要单独加上跨域
		return errorBody;
		
	}
}
