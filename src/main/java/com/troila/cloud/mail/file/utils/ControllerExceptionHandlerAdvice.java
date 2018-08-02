package com.troila.cloud.mail.file.utils;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ControllerExceptionHandlerAdvice {

	private static final Logger LOGGER = LoggerFactory.getLogger(ControllerExceptionHandlerAdvice.class);

	@ExceptionHandler(value = Exception.class)
	public ResponseEntity<String> exceptionHandler(Exception e) {

		LOGGER.error("", e);
		return new ResponseEntity<String>("服务器内部错误，请联系管理员", HttpStatus.INTERNAL_SERVER_ERROR);
	}
//
//	@ExceptionHandler(value = HttpException.class)
//	public ResponseEntity<String> httpExceptionHandler(HttpException e) {
//
//		LOGGER.warn("{}", e.getMsg());
//		return new ResponseEntity<String>(e.getMsg(), e.getHttpStatus());
//	}

	/**
	 * 输入参数验证异常
	 * @param e
	 * @return
     */
	@ExceptionHandler(value = MethodArgumentNotValidException.class)
	public ResponseEntity<String> MethodArgumentNotValidException(MethodArgumentNotValidException e) {
		StringBuilder message = new StringBuilder();
		BindingResult result = e.getBindingResult();
		if (result.hasErrors()){
			List<ObjectError> errorList = result.getAllErrors();
			for(ObjectError error : errorList){
				message.append(error.getDefaultMessage());
				message.append(";");
			}
		}
		LOGGER.warn("{}", message.toString());
		return new ResponseEntity<String>(message.toString(), HttpStatus.BAD_REQUEST);
	}
}