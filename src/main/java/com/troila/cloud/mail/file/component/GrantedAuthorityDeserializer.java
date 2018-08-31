package com.troila.cloud.mail.file.component;

import java.io.IOException;
import java.util.List;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class GrantedAuthorityDeserializer extends JsonDeserializer<List<SimpleGrantedAuthority>>{

	@Override
	public List<SimpleGrantedAuthority> deserialize(JsonParser p, DeserializationContext ctxt)
			throws IOException, JsonProcessingException {
		JsonNode node = p.getCodec().readTree(p);
		ObjectMapper mapper = (ObjectMapper) p.getCodec();
		JavaType type = mapper.getTypeFactory().constructParametricType(List.class, SimpleGrantedAuthority.class);
		System.out.println(node.asText());
		List<SimpleGrantedAuthority> result = mapper.readValue(node.toString(), type);
		return result;
	}

}
