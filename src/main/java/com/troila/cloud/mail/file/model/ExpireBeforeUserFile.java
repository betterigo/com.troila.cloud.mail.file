package com.troila.cloud.mail.file.model;

import java.io.Serializable;
import java.util.List;

public class ExpireBeforeUserFile implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 7723874906650112471L;

	List<UserFile> expireBefores;
	
	List<UserFile> expireBeforesBefore;

	public List<UserFile> getExpireBefores() {
		return expireBefores;
	}

	public void setExpireBefores(List<UserFile> expireBefores) {
		this.expireBefores = expireBefores;
	}

	public List<UserFile> getExpireBeforesBefore() {
		return expireBeforesBefore;
	}

	public void setExpireBeforesBefore(List<UserFile> expireBeforesBefore) {
		this.expireBeforesBefore = expireBeforesBefore;
	}
}
