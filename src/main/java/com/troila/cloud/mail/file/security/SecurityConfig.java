package com.troila.cloud.mail.file.security;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import com.troila.cloud.mail.file.security.filter.TokenFilter;
import com.troila.cloud.mail.file.security.filter.UserLoginFilter;
import com.troila.cloud.mail.file.security.user.AjaxAuthentiacationEntryPoint;
import com.troila.cloud.mail.file.security.user.UserLoginProvider;
import com.troila.cloud.mail.file.security.user.UserLogoutSuccessHandler;
import com.troila.cloud.mail.file.security.user.UsernamePasswordLoginProvider;

/**
 * <b>类说明：</b>
 * <p>
 * class of security configuration
 * </p>
 * <b>创建时间：</b>
 * <p>
 * 2018-01-12
 * </p>
 * 
 * @author hao
 */
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

	@Autowired
	private UserLoginProvider userLoginProvider;
	
	@Autowired
	private UsernamePasswordLoginProvider usernamePasswordLoginProvider;
	
	@Autowired
	private RedisTemplate<Object, Object> redisTemplate;
	
	@Autowired
	private UserLogoutSuccessHandler userLogoutSuccessHandler;
	
	private List<String> igoreUrls = new ArrayList<>();
	
	
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		igoreUrls.add("/login");
		igoreUrls.add("/file/download");
		
		http.csrf().disable()
			.authorizeRequests()
			.antMatchers("/**").permitAll()
			.anyRequest().authenticated()
			.and()
			.httpBasic()
			.and()
			.exceptionHandling().authenticationEntryPoint(createAuthenticationEntryPoint())
			.and()
			.logout()
			.logoutRequestMatcher(new AntPathRequestMatcher("/logout", "POST"))
			.logoutSuccessHandler(userLogoutSuccessHandler)
			.and()
			.addFilterBefore(new TokenFilter(igoreUrls, redisTemplate), UsernamePasswordAuthenticationFilter.class)
//			.and()
			.addFilterBefore(new UserLoginFilter(new AntPathRequestMatcher("/login", "POST"),
						authenticationManager()), UsernamePasswordAuthenticationFilter.class);
	}

	
	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {

		auth.authenticationProvider(userLoginProvider);
		auth.authenticationProvider(usernamePasswordLoginProvider);
	}

	@Override
	public void configure(WebSecurity web) throws Exception {

		web.ignoring().antMatchers("/js/**", "/css/**", "/favicon.ico", "/images/**");
	}

	private AuthenticationEntryPoint createAuthenticationEntryPoint() {

		AjaxAuthentiacationEntryPoint point = new AjaxAuthentiacationEntryPoint();
		point.setRealmName("FILE-API");
		return point;
	}

}