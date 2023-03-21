package com.numble.mybox.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.httpBasic().disable() // 기본 설정 사용 X
            .csrf().disable() // rest api는 csrf 보안 필요 X
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)// jwt 인증에 세션 필요 X
            .and()
            .authorizeRequests() // 다음 리퀘스트에 대한 사용 권한 체크
            .antMatchers("/**").permitAll(); // 가입 및 인증 주소는 누구나 접근 가능

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

}
