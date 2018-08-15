package com.troila.cloud.mail.file.config.settings;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix="server.allowed")
public class AllowedOriginsSettings {
	
	private String origins;

	public String getOrigins() {
		return origins;
	}

	public void setOrigins(String origins) {
		this.origins = origins;
	}
	public List<String> getAllowedOrigins(){
		String[] list = origins.split(";");
		List<String> result = new ArrayList<>();
		for(String o:list) {
			if(o!=null && !"".equals(o))
			result.add(o);
		}
		return result;
	}
	
}
