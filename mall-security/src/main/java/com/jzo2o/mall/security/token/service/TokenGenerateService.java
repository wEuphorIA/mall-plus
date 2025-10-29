package com.jzo2o.mall.security.token.service;


import com.jzo2o.mall.common.enums.UserEnums;
import com.jzo2o.mall.common.model.Token;

/**
 * AbstractToken
 * 抽象token，定义生成token类
 */
public interface TokenGenerateService<T> {

    /**
     * 生成token
     *
     * @param user 用户名
     * @param longTerm 是否长时间有效
     * @return TOKEN对象
     */
    public  Token createToken(T user, Boolean longTerm);

    /**
     * 刷新token
     *
     * @param refreshToken 刷新token
     * @return token
     */
    public Token refreshToken(String refreshToken);



}
