package com.troila.cloud.mail.file.security.error;

import java.util.Date;

public class ErrorBody {
	private int status;
	private Date timestamp;
	private String message;
	private String path;
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public Date getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	@Override
	public String toString() {
		return "ErrorBody [status=" + status + ", timestamp=" + timestamp + ", message=" + message + ", path=" + path
				+ "]";
	}
}
