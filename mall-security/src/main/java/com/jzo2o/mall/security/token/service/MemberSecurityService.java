package com.jzo2o.mall.security.token.service;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jzo2o.mall.common.enums.UserEnums;
import com.jzo2o.mall.common.model.Token;
import com.jzo2o.mall.member.model.domain.Member;
import com.jzo2o.mall.member.model.dto.*;
import com.jzo2o.mysql.domain.PageVO;

import java.util.List;
import java.util.Map;

/**
 * 会员业务层
 */
public interface MemberSecurityService extends IService<Member> {
    /**
     * 默认密码
     */
    static String DEFAULT_PASSWORD = "111111";

    /**
     * 获取当前登录的用户信息
     *
     * @return 会员信息
     */
    Member getUserInfo();


    /**
     * 登录：用户名、密码登录
     *
     * @param username 用户名
     * @param password 密码
     * @return token
     */
    Token usernameLogin(String username, String password);

    /**
     * 商家登录：用户名、密码登录
     *
     * @param username 用户名
     * @param password 密码
     * @return token
     */
    Token usernameStoreLogin(String username, String password);
//
//    /**
//     * 商家登录：用户名、密码登录
//     *
//     * @param mobilePhone 用户名
//     * @return token
//     */
//    Token mobilePhoneStoreLogin(String mobilePhone);
//
//    /**
//     * 注册：手机号、验证码登录
//     *
//     * @param mobilePhone 手机号
//     * @return token
//     */
//    Token mobilePhoneLogin(String mobilePhone);


//    /**
//     * 刷新token
//     *
//     * @param refreshToken
//     * @return Token
//     */
//    Token refreshToken(String refreshToken);
//
    /**
     * 刷新token
     *
     * @param refreshToken
     * @return Token
     */
    Token refreshStoreToken(String refreshToken);

    /**
     * 登出
     *
     * @param userEnums token角色类型
     */
    void logout(UserEnums userEnums);

    /**
     * 登出
     *
     * @param userId 用户id
     */
    void logout(String userId);

}