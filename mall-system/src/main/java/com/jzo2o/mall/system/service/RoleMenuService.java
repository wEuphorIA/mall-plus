package com.jzo2o.mall.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jzo2o.mall.system.model.domain.RoleMenu;

import java.util.List;

/**
 * 角色菜单接口
 */
public interface RoleMenuService extends IService<RoleMenu> {

    /**
     * 通过角色获取菜单权限列表
     *
     * @param roleId
     * @return
     */
    List<RoleMenu> findByRoleId(String roleId);


    /**
     * 更新某角色拥有到菜单
     *
     * @param roleId
     * @param roleMenus
     */
    void updateRoleMenu(String roleId, List<RoleMenu> roleMenus);

    /**
     * 根据角色id 删除数据
     *
     * @param roleId
     */
    void deleteRoleMenu(String roleId);

    /**
     * 根据角色id 删除数据
     *
     * @param roleId
     */
    void deleteRoleMenu(List<String> roleId);

}