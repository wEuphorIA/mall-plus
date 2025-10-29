package com.jzo2o.mall.security.filter;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.google.gson.Gson;
import com.jzo2o.mall.common.cache.CachePrefix;
import com.jzo2o.mall.common.enums.PermissionEnum;
import com.jzo2o.mall.common.enums.SecurityEnum;
import com.jzo2o.mall.common.enums.UserEnums;
import com.jzo2o.mall.common.model.AuthUser;
import com.jzo2o.mall.common.utils.ResponseUtil;
import com.jzo2o.mall.security.token.SecretKeyUtil;
import com.jzo2o.redis.helper.Cache;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.util.PatternMatchUtils;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.naming.NoPermissionException;
import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 管理端token过滤
 */
@Slf4j
public class ManagerAuthenticationFilter extends BasicAuthenticationFilter {

    private final Cache cache;

    public ManagerAuthenticationFilter(AuthenticationManager authenticationManager,
                                       Cache cache) {
        super(authenticationManager);
        this.cache = cache;
    }

    @SneakyThrows
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) {

        //从header中获取jwt
        String jwt = request.getHeader(SecurityEnum.HEADER_TOKEN.getValue());
        //如果没有token 则return
        if (StrUtil.isBlank(jwt)) {
            chain.doFilter(request, response);
            return;
        }

        //获取用户信息，存入context
        UsernamePasswordAuthenticationToken authentication = getAuthentication(jwt, response);
        //自定义权限过滤
        if (authentication != null) {
            //权限过虑
            permissionFilter(request, response, authentication);
            //设置认证用户信息
            SecurityContextHolder.getContext().setAuthentication(authentication);

        }
        chain.doFilter(request, response);
    }


    /**
     * 自定义权限过滤
     *
     * @param request        请求
     * @param response       响应
     * @param authentication 用户信息
     */
    private void permissionFilter(HttpServletRequest request, HttpServletResponse response, UsernamePasswordAuthenticationToken authentication) throws NoPermissionException, IOException {
        AuthUser authUser = (AuthUser) authentication.getDetails();
        String requestUrl = request.getRequestURI();
        UserEnums userEnums = authUser.getRole();
        String[] urlPrefixs = userEnums.getUrlPrefix();
        boolean isMatch = false;
        //根据url前缀校验用户访问权限
        for (int i = 0; i < urlPrefixs.length; i++) {
            if(requestUrl.startsWith(urlPrefixs[i])){
                isMatch = true;
                break;
            }
        }
        if(!isMatch){
            throw new NoPermissionException("权限不足");
        }

        //如果不是超级管理员， 则鉴权
        if (Boolean.FALSE.equals(authUser.getIsSuper()) && !authUser.getRole().equals(UserEnums.MEMBER)) {
            //用户权限key
            String permissionCacheKey = CachePrefix.PERMISSION_LIST.getPrefix(userEnums) + authUser.getId();
            //获取缓存中的权限
            Map<String, List<String>> permission =
                    (Map<String, List<String>>) cache.get(permissionCacheKey);
            if (permission == null || permission.isEmpty()) {
                ResponseUtil.output(response, ResponseUtil.resultMap(false, 400, "权限不足"));
                throw new NoPermissionException("权限不足");
            }
            //获取数据(GET 请求)权限
            if (request.getMethod().equals(RequestMethod.GET.name())) {
                //如果用户的超级权限和查阅权限都不包含当前请求的api
                if (match(permission.get(PermissionEnum.SUPER.name()), requestUrl) ||
                        match(permission.get(PermissionEnum.QUERY.name()), requestUrl)) {
                } else {
                    ResponseUtil.output(response, ResponseUtil.resultMap(false, 400, "权限不足"));
                    log.error("当前请求路径：{},所拥有权限：{}", requestUrl, JSONUtil.toJsonStr(permission));
                    throw new NoPermissionException("权限不足");
                }
            }
            //非get请求（数据操作） 判定鉴权
            else {
                if (!match(permission.get(PermissionEnum.SUPER.name()), requestUrl)) {
                    ResponseUtil.output(response, ResponseUtil.resultMap(false, 400, "权限不足"));
                    log.error("当前请求路径：{},所拥有权限：{}", requestUrl, JSONUtil.toJsonStr(permission));
                    throw new NoPermissionException("权限不足");
                }
            }
        }
    }

    /**
     * 校验权限
     *
     * @param permissions 权限集合
     * @param url         请求地址
     * @return 是否拥有权限
     */
    boolean match(List<String> permissions, String url) {
        if (permissions == null || permissions.isEmpty()) {
            return false;
        }
        return PatternMatchUtils.simpleMatch(permissions.toArray(new String[0]), url);
    }

    /**
     * 获取token信息
     * @param jwt      token信息
     * @param response 响应
     * @return 获取鉴权对象
     */
    private UsernamePasswordAuthenticationToken getAuthentication(String jwt, HttpServletResponse response) {

        try {
            Claims claims
                    = Jwts.parser()
                    .setSigningKey(SecretKeyUtil.generalKeyByDecoders())
                    .parseClaimsJws(jwt).getBody();
            //获取存储在claims中的用户信息
            String json = claims.get(SecurityEnum.USER_CONTEXT.getValue()).toString();
            AuthUser authUser = new Gson().fromJson(json, AuthUser.class);
            UserEnums userEnums = authUser.getRole();
            //校验redis中是否有权限
            if (cache.hasKey(CachePrefix.ACCESS_TOKEN.getPrefix(userEnums, authUser.getIdString()) + jwt) ) {
                //用户角色
                List<GrantedAuthority> auths = new ArrayList<>();
                auths.add(new SimpleGrantedAuthority("ROLE_" + authUser.getRole().name()));
                //构造返回信息
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(authUser.getUsername(), null, auths);
                authentication.setDetails(authUser);
                return authentication;
            }
        } catch (ExpiredJwtException e) {
            log.debug("user analysis exception:", e);
        } catch (Exception e) {
            log.error("other exception:", e);
        }
        ResponseUtil.output(response, 403, ResponseUtil.resultMap(false, 403, "登录已失效，请重新登录"));
        return null;
    }
}

