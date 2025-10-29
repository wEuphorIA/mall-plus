package com.jzo2o.mall.product.script;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;

/**
 * redis 脚本
 */
@Configuration
public class LuaScript {

    /**
     * 库存扣减脚本
     */
    @Bean
    public DefaultRedisScript<Boolean> quantityScript() {
        DefaultRedisScript<Boolean> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("script/quantity.lua")));
        redisScript.setResultType(Boolean.class);
        return redisScript;
    }


}
