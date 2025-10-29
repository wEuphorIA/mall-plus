package com.jzo2o.mall.config;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.jzo2o.mysql.interceptor.MyBatisAutoFillInterceptor;
import com.jzo2o.mysql.properties.MybatisPlusProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Configuration
@EnableConfigurationProperties(MybatisPlusProperties.class)
public class MallMybatisPlusConfiguration {

    @Autowired
    private MybatisPlusInterceptor mybatisPlusInterceptor;


    @PostConstruct
    public void init() {
        MyBatisAutoFillInterceptor interceptor = new MyBatisAutoFillInterceptor();
        mybatisPlusInterceptor.addInnerInterceptor(interceptor);
    }

}
