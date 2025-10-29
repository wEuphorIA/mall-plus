package com.jzo2o.mall.security.token.service.impl;

import cn.hutool.core.text.CharSequenceUtil;
import com.jzo2o.common.utils.UserContext;
import com.jzo2o.mall.common.cache.CachePrefix;
import com.jzo2o.mall.common.enums.ResultCode;
import com.jzo2o.mall.common.enums.UserEnums;
import com.jzo2o.mall.common.exception.ServiceException;
import com.jzo2o.mall.common.model.AuthUser;
import com.jzo2o.mall.common.model.Token;
import com.jzo2o.mall.security.token.service.AdminUserSecurityService;
import com.jzo2o.mall.system.model.domain.AdminUser;
import com.jzo2o.mall.system.service.AdminUserService;
import com.jzo2o.redis.helper.Cache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 用户业务层实现
 */
@Slf4j
@Service
public class AdminUserSecurityServiceImpl implements AdminUserSecurityService {
    /**
     * 角色长度
     */
    private final int rolesMaxSize = 10;
    @Autowired
    private ManagerTokenGenerateServiceImpl managerTokenGenerate;
    @Autowired
    private Cache cache;

    @Autowired
    private AdminUserService adminUserService;

    @Override
    public Token login(String username, String password) {
        AdminUser adminUser = adminUserService.findByUsername(username);

        if (adminUser == null || !adminUser.getStatus()) {
            throw new ServiceException(ResultCode.USER_PASSWORD_ERROR);
        }
        if (!new BCryptPasswordEncoder().matches(password, adminUser.getPassword())) {
            throw new ServiceException(ResultCode.USER_PASSWORD_ERROR);
        }
        try {
            return managerTokenGenerate.createToken(adminUser, false);
        } catch (Exception e) {
            log.error("管理员登录错误", e);
        }
        return null;

    }

    @Override
    public Token refreshToken(String refreshToken) {
        return managerTokenGenerate.refreshToken(refreshToken);
    }

    @Override
    public void logout(UserEnums userEnums) {
        AuthUser authUser = UserContext.getCurrentUser();
        String currentUserToken = authUser.getAccessToken();

        if (CharSequenceUtil.isNotEmpty(currentUserToken)) {
            cache.remove(CachePrefix.ACCESS_TOKEN.getPrefix(userEnums, authUser.getIdString()) + currentUserToken);
            cache.vagueDel(CachePrefix.REFRESH_TOKEN.getPrefix(userEnums, authUser.getIdString()));
        }
    }

    @Override
    public void logout(List<String> adminUserIds) {
        if (adminUserIds == null || adminUserIds.isEmpty()) {
            return;
        }
        adminUserIds.forEach(adminUserId -> {
            cache.vagueDel(CachePrefix.ACCESS_TOKEN.getPrefix(UserEnums.MANAGER, adminUserId));
            cache.vagueDel(CachePrefix.REFRESH_TOKEN.getPrefix(UserEnums.MANAGER, adminUserId));
        });
    }


    public static void main(String[] args) {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        /**
         $2a$10$1sp7I0OdYH3Azs/2lK8YYeuiaGZzOGshGT9j.IYArZftsGNsXqlma
         $2a$10$m983E2nmJ7ITlesbXzjbzO/M7HL2wP8EgpgX.pPACDm1wG38Lt.na
         $2a$10$rZvathrW98vVPenLhOnl0OMpUtRTdBkWJ45IkIsTebITS9AFgKqGK
         $2a$10$2gaMKWCRoKdc42E0jsq7b.munjzOSPOM4yr3GG9M6194E7dOH5LyS
         $2a$10$I/n93PIKpKL8m4O3AuT5kuZncZhfqV51bfx5sJrplnYoM7FimdboC
         */
        for (int i = 0; i < 5; i++) {
            String encode = passwordEncoder.encode("96e79218965eb72c92a549dd5a330112");
            System.out.println(encode);
        }

        boolean matches = passwordEncoder.matches("111111", "$2a$10$6fuG666M1pXv84D5nRTikObcvMWaz.zAbLAySwZCIW9MnAMixnhma");
        System.out.println(matches);
    }

}
