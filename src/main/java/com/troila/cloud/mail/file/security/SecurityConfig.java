package com.troila.cloud.mail.file.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpMethod;
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
	private StringRedisTemplate redisTemplate;
	
	@Autowired
	private UserLogoutSuccessHandler userLogoutSuccessHandler;
	
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		
		http.csrf().disable()
//			.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)//不需要session验证
//			.and()
			.authorizeRequests()
			.antMatchers(HttpMethod.OPTIONS).permitAll()//跨域请求需要放行options请求
			.antMatchers("/login","/file/download/**","/userfile/uptoexpire","/page/**","/preview/**").permitAll()
			.antMatchers("/file/prepare","/file").hasRole("UPLOAD")
			.antMatchers("/user/**").hasRole("USER_MANAGE")
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
			.addFilterBefore(new TokenFilter(redisTemplate), UsernamePasswordAuthenticationFilter.class)
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