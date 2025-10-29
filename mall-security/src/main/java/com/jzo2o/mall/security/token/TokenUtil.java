package com.jzo2o.mall.security.token;

import com.google.gson.Gson;
import com.jzo2o.mall.common.cache.CachePrefix;
import com.jzo2o.mall.common.enums.ResultCode;
import com.jzo2o.mall.common.enums.SecurityEnum;
import com.jzo2o.mall.common.enums.UserEnums;
import com.jzo2o.mall.common.exception.ServiceException;
import com.jzo2o.mall.common.model.AuthUser;
import com.jzo2o.mall.common.model.Token;
import com.jzo2o.mall.security.properties.JWTTokenProperties;
import com.jzo2o.redis.helper.Cache;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * TokenUtil
 */
@Component
public class TokenUtil {
    @Autowired
    private JWTTokenProperties tokenProperties;
    @Autowired
    private Cache cache;

    /**
     * 构建token
     *
     * @param authUser 私有声明
     * @return TOKEN
     */
    public Token createToken(AuthUser authUser) {
        Token token = new Token();
        //访问token
        String accessToken = createToken(authUser, tokenProperties.getTokenExpireTime());

        cache.put(CachePrefix.ACCESS_TOKEN.getPrefix(authUser.getRole(), authUser.getIdString()) + accessToken, 1,
                tokenProperties.getTokenExpireTime(), TimeUnit.MINUTES);
        //刷新token生成策略：如果是长时间有效的token（用于app）则默认15天有效期刷新token，如果是普通用户登录，则刷新token为普通token2倍数
        Long expireTime = authUser.getLongTerm() ? 15 * 24 * 60L : tokenProperties.getTokenExpireTime() * 2;
        String refreshToken = createToken(authUser, expireTime);

        cache.put(CachePrefix.REFRESH_TOKEN.getPrefix(authUser.getRole(), authUser.getIdString()) + refreshToken, 1, expireTime, TimeUnit.MINUTES);

        token.setAccessToken(accessToken);
        token.setRefreshToken(refreshToken);
        return token;
    }

    /**
     * 刷新token
     *
     * @param oldRefreshToken 刷新token
     * @return token
     */
    public Token refreshToken(String oldRefreshToken) {

        Claims claims;
        try {
            claims = Jwts.parser()
                    .setSigningKey(SecretKeyUtil.generalKeyByDecoders())
                    .parseClaimsJws(oldRefreshToken).getBody();
        } catch (ExpiredJwtException | UnsupportedJwtException | MalformedJwtException | SignatureException |
                 IllegalArgumentException e) {
            //token 过期 认证失败等
            throw new ServiceException(ResultCode.USER_AUTH_EXPIRED);
        }

        //获取存储在claims中的用户信息
        String json = claims.get(SecurityEnum.USER_CONTEXT.getValue()).toString();
        AuthUser authUser = new Gson().fromJson(json, AuthUser.class);
        UserEnums userEnums = authUser.getRole();

        String username = authUser.getUsername();
        //获取是否长期有效的token
        boolean longTerm = authUser.getLongTerm();

        //如果缓存中有刷新token &&
        if (cache.hasKey(CachePrefix.REFRESH_TOKEN.getPrefix(userEnums, authUser.getIdString()) + oldRefreshToken)) {
            Token token = new Token();
            //访问token
            String accessToken = createToken(authUser, tokenProperties.getTokenExpireTime());
            cache.put(CachePrefix.ACCESS_TOKEN.getPrefix(userEnums, authUser.getIdString()) + accessToken, 1, tokenProperties.getTokenExpireTime(),
                    TimeUnit.MINUTES);

            //如果是信任登录设备，则刷新token长度继续延长
            Long expirationTime = tokenProperties.getTokenExpireTime() * 2;
            if (longTerm) {
                expirationTime = 60 * 24 * 15L;
                authUser.setLongTerm(true);
            }

            //刷新token生成策略：如果是长时间有效的token（用于app），则默认15天有效期刷新token。如果是普通用户登录，则刷新token为普通token2倍数
            String refreshToken = createToken(authUser, expirationTime);

            cache.put(CachePrefix.REFRESH_TOKEN.getPrefix(userEnums, authUser.getIdString()) + refreshToken, 1, expirationTime, TimeUnit.MINUTES);
            token.setAccessToken(accessToken);
            token.setRefreshToken(refreshToken);
            cache.remove(CachePrefix.REFRESH_TOKEN.getPrefix(userEnums, authUser.getIdString()) + oldRefreshToken);
            return token;
        } else {
            throw new ServiceException(ResultCode.USER_AUTH_EXPIRED);
        }

    }

    /**
     * 生成token
     *
     * @param authUser       jwt主体对象
     * @param expirationTime 过期时间（分钟）
     * @return token字符串
     */
    private String createToken(AuthUser authUser, Long expirationTime) {
        //JWT 生成
        return Jwts.builder()
                //jwt 私有声明
                .claim(SecurityEnum.USER_CONTEXT.getValue(), new Gson().toJson(authUser))
                //JWT的主体
                .setSubject(authUser.getUsername())
                //失效时间 当前时间+过期分钟
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime * 60 * 1000))
                //签名算法和密钥
                .signWith(SecretKeyUtil.generalKey())
                .compact();
    }
}
