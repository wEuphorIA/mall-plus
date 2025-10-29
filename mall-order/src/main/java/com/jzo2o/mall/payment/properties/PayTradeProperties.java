package com.jzo2o.mall.payment.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * token过期配置
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "trade")
public class PayTradeProperties {


    /**
     * trade 支付服务地址
     */
    private String url;
}
