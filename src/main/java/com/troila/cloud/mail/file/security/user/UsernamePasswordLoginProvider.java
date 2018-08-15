package com.troila.cloud.mail.file.security.user;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.troila.cloud.mail.file.model.User;
import com.troila.cloud.mail.file.model.UserInfo;
import com.troila.cloud.mail.file.repository.UserInfoRepository;
import com.troila.cloud.mail.file.repository.UserRepository;
import com.troila.cloud.mail.file.utils.TokenUtil;

@Component
public class UsernamePasswordLoginProvider implements AuthenticationProvider{

	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private UserInfoRepository userInfoRepository;
	
	@Autowired
	private StringRedisTemplate redisTemplate;
	
	private ObjectMapper mapper = new ObjectMapper();
	
	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		String username = authentication.getPrincipal().toString();
		String password = authentication.getCredentials().toString();
		password = DigestUtils.md5Hex(password);
		User user = userRepository.findByNameAndPassword(username, password);
		if(user == null) {
			throw new UsernameNotFoundException("用户名或密码错误");
		}else {
			String accessKey = TokenUtil.getToken();
//			redisTemplate.opsForValue().set(accessKey, user);
			try {
				Optional<UserInfo> userInfo = userInfoRepository.findById(user.getId());
				if(!userInfo.isPresent()) {
					throw new UsernameNotFoundException("用户信息获取失败！");
				}
				redisTemplate.opsForValue().set(accessKey, mapper.writeValueAsString(userInfo.get()), 1, TimeUnit.HOURS);
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			}
			return new UsernamePasswordAuthenticationToken(accessKey, password,getUserAuthorities());
		}
	}

	@Override
	public boolean supports(Class<?> authentication) {
		return authentication.equals(UsernamePasswordAuthenticationToken.class);
	}
	private List<GrantedAuthority> getUserAuthorities(){
		List<GrantedAuthority> authorities = new ArrayList<>();
		return authorities;
	}

}
