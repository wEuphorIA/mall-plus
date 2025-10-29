package com.jzo2o.mall.security.filter;

import com.jzo2o.mall.security.CustomAccessDeniedHandler;
import com.jzo2o.mall.security.handler.AuthenticationEntryPointImpl;
import com.jzo2o.mall.security.properties.IgnoredUrlsProperties;
import com.jzo2o.redis.helper.Cache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.web.cors.CorsConfigurationSource;

/**
 * spring Security 核心配置类 Manager安全配置中心
 */
@Slf4j
@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class ManagerSecurityConfig extends WebSecurityConfigurerAdapter {


    /**
     * 忽略验权配置
     */
    @Autowired
    private IgnoredUrlsProperties ignoredUrlsProperties;
    /**
     * spring security -》 权限不足处理
     */
    @Autowired
    private CustomAccessDeniedHandler accessDeniedHandler;
    @Autowired
    private Cache<String> cache;
    @Autowired
    private CorsConfigurationSource corsConfigurationSource;

    /**
     * 认证失败处理类
     */
    @Autowired
    private AuthenticationEntryPointImpl unauthorizedHandler;


    @Override
    protected void configure(HttpSecurity http) throws Exception {

        ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry registry = http
                .authorizeRequests();
        //配置的url 不需要授权
        for (String url : ignoredUrlsProperties.getUrls()) {
            registry.antMatchers(url).permitAll();
        }
        registry
                .and()
                //禁止网页iframe
                .headers().frameOptions().disable()
                .and()
                .authorizeRequests()
                //任何请求
                .anyRequest()
                //需要身份认证
                .authenticated()
                .and()
                .exceptionHandling().authenticationEntryPoint(unauthorizedHandler)
                .and()
                //允许跨域
                .cors().configurationSource(corsConfigurationSource).and()
                //关闭跨站请求防护
                .csrf().disable()
                //前后端分离采用JWT 不需要session
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                //自定义权限拒绝处理类
                .exceptionHandling().accessDeniedHandler(accessDeniedHandler)
                .and()
                //添加JWT认证过滤器
                .addFilter(new ManagerAuthenticationFilter(authenticationManager(), cache));
    }

}
