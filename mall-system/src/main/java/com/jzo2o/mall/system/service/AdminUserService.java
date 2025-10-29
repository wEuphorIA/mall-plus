package com.jzo2o.mall.system.service;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jzo2o.mall.common.model.AuthUser;
import com.jzo2o.mall.system.model.domain.AdminUser;
import com.jzo2o.mall.system.model.dto.AdminUserDTO;
import org.springframework.cache.annotation.CacheConfig;

import java.util.List;

/**
 * 用户业务层
 */
@CacheConfig(cacheNames = "{adminuser}")
public interface AdminUserService extends IService<AdminUser> {


    /**
     * 获取管理员分页
     *
     * @param initPage
     * @param initWrapper
     * @return
     */
    IPage<AdminUserDTO> adminUserPage(Page initPage, QueryWrapper<AdminUser> initWrapper);

    /**
     * 通过用户名获取用户
     *
     * @param username
     * @return
     */
    AdminUser findByUsername(String username);


    /**
     * 更新管理员
     *
     * @param adminUser
     * @param roles
     * @return
     */
    boolean updateAdminUser(AdminUser adminUser, List<String> roles);


    /**
     * 修改管理员密码
     *
     * @param password
     * @param newPassword
     */
    void editPassword(AuthUser tokenUser,String password, String newPassword);

    /**
     * 重置密码
     *
     * @param ids id集合
     */
    void resetPassword(List<String> ids);

    /**
     * 新增管理员
     *
     * @param adminUser
     * @param roles
     */
    void saveAdminUser(AdminUserDTO adminUser, List<String> roles);

    /**
     * 彻底删除
     *
     * @param ids
     */
    void deleteCompletely(List<String> ids);



}
