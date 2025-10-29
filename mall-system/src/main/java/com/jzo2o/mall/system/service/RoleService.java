package com.jzo2o.mall.system.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.jzo2o.mall.system.model.domain.Role;

import java.util.List;

/**
 * 角色业务层
 */
public interface RoleService extends IService<Role> {

    /**
     * 获取默认角色
     *
     * @param defaultRole
     * @return
     */
    List<Role> findByDefaultRole(Boolean defaultRole);


    /**
     * 批量删除角色
     * @param roleIds
     */
    void deleteRoles(List<String> roleIds);
}
