package com.jzo2o.mall.security.context;


import com.jzo2o.mall.common.model.AuthUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@Slf4j
public class UserContextIntercepterConfig implements WebMvcConfigurer {

    @Autowired
    UserContextIntercepter userContextInteceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // token拦截器
        registry.addInterceptor(userContextInteceptor)
                .addPathPatterns("/**");
    }


}
