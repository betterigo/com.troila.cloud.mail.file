package com.troila.cloud.mail.file.model.fenum;

public enum AccessList {
	PRIVATE(1),
	PUBLIC(2),
	PROTECT(3);
	
	private int value;
	
	AccessList(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}	
}
