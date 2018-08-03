package com.troila.cloud.mail.file.security.user;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.troila.cloud.mail.file.config.settings.SecuritySettings;
import com.troila.cloud.mail.file.model.User;
import com.troila.cloud.mail.file.repository.UserRepository;
import com.troila.cloud.mail.file.utils.TokenUtil;

@Component
@EnableConfigurationProperties(value=SecuritySettings.class)
public class UserLoginProvider implements AuthenticationProvider{

	private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private SecuritySettings securitySettings;
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private RedisTemplate<Object, Object> redisTemplate;
	
	private ObjectMapper mapper = new ObjectMapper();
	
	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		Object loginToken = authentication.getCredentials();//token
		User user = null;
		if(loginToken == null) {
			throw new UsernameNotFoundException("token为null");
		}		
			String result = httpIssue(loginToken.toString());
			if(result == null) {
				throw new UsernameNotFoundException("无效的token值!");
			}
			try {
				JsonNode jsonNode = mapper.readTree(result);
				JsonNode nameNode = jsonNode.get("name");
				JsonNode userCodeNode = jsonNode.get("userCode");
				List<User> users = userRepository.findByUserCode(userCodeNode.asText());
				if(users.isEmpty()) {
					User newUser = new User();
					newUser.setGmtCreate(new Date());
					newUser.setName(nameNode.asText());
					newUser.setUserCode(userCodeNode.asText());
					newUser.setPassword(DigestUtils.md5Hex("123456"));//设置默认密码123456
					user = userRepository.save(newUser);
				}else {
					user = users.get(0);
				}
				String userAccessToken = TokenUtil.getToken();
				redisTemplate.opsForValue().set(userAccessToken, user);
				return new UsernamePasswordAuthenticationToken(userAccessToken,loginToken,getUserAuthorities());
			} catch (IOException e) {
				LOGGER.error("登录发生异常",e);
			}
		return null;
	}

	@Override
	public boolean supports(Class<?> authentication) {
		return authentication.equals(TokenTypeAuthenticationToken.class);
	}

	private String httpIssue(String token) {
		
		CloseableHttpClient httpClient = null;
		CloseableHttpResponse httpResponse = null;
		try {
			httpClient = HttpClients.createDefault();
			HttpPost httpPost = new HttpPost(securitySettings.getTokenUrl()+"?token="+token+"&type=deskapp");
			httpPost.addHeader("sendType", "sendTypeJson");
			httpPost.addHeader("receiveType", "receiveTypeJson");
			if(token != null && !"".equals(token)) {
				httpPost.addHeader("token", token);
			}
			//加入request body数据
			httpResponse = httpClient.execute(httpPost);
			int statusCode = httpResponse.getStatusLine().getStatusCode();
			if (statusCode == HttpStatus.SC_OK) {
				HttpEntity responseEntity = httpResponse.getEntity();
				if (responseEntity != null) {
					String result = EntityUtils.toString(responseEntity);
					return result;
				}else {
					LOGGER.error("responseEntity为空");
					return null;
				}
				
			}else {
				LOGGER.error("http返回码为：:{}",statusCode);
				return null;
			}
		} catch (Exception e) {
			LOGGER.error("http请求异常:{}",e);
		}finally { 
			try {
				httpClient.close();
			} catch (IOException e) {
				LOGGER.error("关闭httpClient异常:{}",e);
			}
			try {
				httpResponse.close();
			} catch (IOException e) {
				LOGGER.error("关闭httpResponse异常:{}",e);
			}
		}
		return null;
	}
	private List<GrantedAuthority> getUserAuthorities(){
		List<GrantedAuthority> authorities = new ArrayList<>();
		return authorities;
	}
}