package com.gym;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsUtils;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 关键：必须放在最前面，开启CORS，自动关联上面的跨域配置
                .cors().and()
                // 开发环境关闭CSRF防护，否则POST请求会被拦截
                .csrf().disable()
                // 放行所有OPTIONS预检请求，这是跨域的核心
                .authorizeRequests()
                .requestMatchers(CorsUtils::isPreFlightRequest).permitAll()
                // 放行注册、登录等不需要认证的接口
                .antMatchers("/api/users/**").permitAll()
                // 放行动作库相关接口
                .antMatchers("/api/exercises/**").permitAll()
                // 放行训练计划相关接口
                .antMatchers("/api/plans/**").permitAll()
                // 放行营养记录相关接口
                .antMatchers("/api/nutrition/**").permitAll()
                // 放行训练记录相关接口
                .antMatchers("/api/workouts/**").permitAll()
                // 放行AI相关接口
                .antMatchers("/api/ai/**").permitAll()
                // 其余接口需要认证
                .anyRequest().authenticated()
                .and()
                // 前后端分离项目使用无状态会话
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);

        return http.build();
    }
}