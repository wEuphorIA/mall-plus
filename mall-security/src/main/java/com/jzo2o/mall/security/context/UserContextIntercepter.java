package com.jzo2o.mall.security.context;

import com.google.gson.Gson;
import com.jzo2o.common.utils.UserContext;
import com.jzo2o.mall.common.enums.SecurityEnum;
import com.jzo2o.mall.common.model.AuthUser;
import com.jzo2o.mall.security.token.SecretKeyUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author itcast
 */
@Slf4j
@Component
public class UserContextIntercepter implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        // 1.尝试获取头信息中的用户信息
        String accessToken = request.getHeader(SecurityEnum.HEADER_TOKEN.getValue());
        // 2.判断是否为空
        if (accessToken == null) {
            return true;
        }
        AuthUser authUser = getAuthUser(accessToken);
        //将当前用户信息放入ThreadLocal
        UserContext.set(authUser);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 清理用户信息
        UserContext.clear();
    }

    /**
     * 根据jwt获取token重的用户信息
     *
     * @param accessToken token
     * @return 授权用户
     */
    public  AuthUser getAuthUser(String accessToken) {
        try {
            //获取token的信息
            Claims claims
                    = Jwts.parser()
                    .setSigningKey(SecretKeyUtil.generalKeyByDecoders())
                    .parseClaimsJws(accessToken).getBody();
            //获取存储在claims中的用户信息
            String json = claims.get(SecurityEnum.USER_CONTEXT.getValue()).toString();
            AuthUser authUser = new Gson().fromJson(json, AuthUser.class);
            authUser.setAccessToken(accessToken);
            return authUser;
        } catch (Exception e) {
            return null;
        }
    }
}
