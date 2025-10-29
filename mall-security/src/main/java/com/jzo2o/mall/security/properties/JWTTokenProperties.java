package com.jzo2o.mall.security.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * token过期配置
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "mall.jwt-setting")
public class JWTTokenProperties {


    /**
     * token默认过期时间
     */
    private long tokenExpireTime = 60;
}
