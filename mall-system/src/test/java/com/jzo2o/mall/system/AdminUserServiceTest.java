package com.jzo2o.mall.system;

import com.jzo2o.mall.system.model.domain.AdminUser;
import com.jzo2o.mall.system.service.AdminUserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.Assert;

/**
 * @author Mr.M
 * @version 1.0
 * @description TODO
 * @date 2024/4/18 16:25
 */
@SpringBootTest
public class AdminUserServiceTest {

    @Autowired
    AdminUserService adminUserService;


    @Test
    public void test() {
        AdminUser adminUser = adminUserService.findByUsername("admin");
        Assert.notNull(adminUser, "admin is null");
    }
}
