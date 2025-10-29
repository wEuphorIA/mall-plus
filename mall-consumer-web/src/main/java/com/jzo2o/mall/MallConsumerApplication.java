package com.jzo2o.mall;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author Mr.M
 * @version 1.0
 * @description mall工程启动类
 * @date 2024/4/15 9:06
 */
@Slf4j
@SpringBootApplication
@EnableScheduling
@MapperScan("com.jzo2o.mall.common.*.mapper,com.jzo2o.mall.*.mapper")
public class MallConsumerApplication {
    public static void main(String[] args) {
        new SpringApplicationBuilder(MallConsumerApplication.class)
                .build(args)
                .run(args);
        log.info("商城项目消费端接口服务启动");
    }
}
