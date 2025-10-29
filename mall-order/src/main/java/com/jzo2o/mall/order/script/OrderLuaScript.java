package com.jzo2o.mall.order.script;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;

/**
 * redis 脚本
 */
@Configuration
public class OrderLuaScript {

    /**
     * 库存扣减脚本
     */
    @Bean
    public DefaultRedisScript<Boolean> seckillScript() {
        DefaultRedisScript<Boolean> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("script/seckill.lua")));
        redisScript.setResultType(Boolean.class);
        return redisScript;
    }


}
