package com.jzo2o.mall.security.token.service;


import com.jzo2o.mall.common.enums.UserEnums;
import com.jzo2o.mall.common.model.Token;
import org.springframework.cache.annotation.CacheConfig;

import java.util.List;

/**
 * 用户业务层
 */
@CacheConfig(cacheNames = "{adminuser}")
public interface AdminUserSecurityService {

    /**
     * 用户登录
     *
     * @param username 用户名
     * @param password 密码
     * @return token
     */
    Token login(String username, String password);

    /**
     * 刷新token
     *
     * @param refreshToken
     * @return token
     */
    Token refreshToken(String refreshToken);

    /**
     * 登出
     *
     * @param userEnums token角色类型
     */
    void logout(UserEnums userEnums);

    /**
     * 登出
     *
     * @param adminUserIds 用户id
     */
    void logout(List<String> adminUserIds);

}
