package com.jzo2o.mall.system;

import com.jzo2o.mall.system.mapper.AdminUserMapper;
import com.jzo2o.mall.system.model.domain.AdminUser;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.Assert;

/**
 * @author Mr.M
 * @version 1.0
 * @description TODO
 * @date 2024/4/18 16:12
 */
@SpringBootTest
public class AdminUserMapperTest {

    @Autowired
    AdminUserMapper adminUserMapper;

    @Test
    public void test(){
        AdminUser admin = adminUserMapper.selectById(1337306110277476352L);
        Assert.notNull(admin,"admin is null");
    }
}
