package com.troila.cloud.mail.file.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import com.troila.cloud.mail.file.security.filter.UserLoginFilter;
import com.troila.cloud.mail.file.security.user.UserLoginProvider;

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
	
	@Override
	protected void configure(HttpSecurity http) throws Exception {

		http.csrf().disable()
			.authorizeRequests()
			.antMatchers("/file/download").permitAll()
			.anyRequest().authenticated()
			.and()
			.httpBasic()
			.and()
			.exceptionHandling().authenticationEntryPoint(createAuthenticationEntryPoint())
			.and()
			.logout()
			.logoutRequestMatcher(new AntPathRequestMatcher("/logout", "POST"))
			.and()
			.addFilterBefore(new UserLoginFilter(new AntPathRequestMatcher("/login", "GET"),
						authenticationManager()), UsernamePasswordAuthenticationFilter.class);
	}

	
	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {

		auth.authenticationProvider(userLoginProvider);
//		auth.authenticationProvider(troilaCloudLoginProvider);
//		auth.authenticationProvider(deskAppLoginProvider);
	}

	@Override
	public void configure(WebSecurity web) throws Exception {

		web.ignoring().antMatchers("/js/**", "/css/**", "/favicon.ico", "/images/**");
	}

	private AuthenticationEntryPoint createAuthenticationEntryPoint() {

		BasicAuthenticationEntryPoint point = new BasicAuthenticationEntryPoint();
		point.setRealmName("FILE-API");
		return point;
	}

}