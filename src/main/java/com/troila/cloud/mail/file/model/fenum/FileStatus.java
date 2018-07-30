package com.troila.cloud.mail.file.model.fenum;

public enum FileStatus {
	UPLOADING(1),
	SUCESS(2),
	FAIL(3),
	DELETE(4),
	PAUSE(5);

	private int value;
	
	FileStatus(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}	
}
