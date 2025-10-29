package com.jzo2o.mall.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jzo2o.mall.member.model.domain.StoreMenuRole;
import com.jzo2o.mall.member.model.dto.StoreUserMenuDTO;

import java.util.List;

/**
 * 角色菜单接口
 *
 * @author Chopper
 * @since 2020/11/22 11:43
 */
public interface StoreMenuRoleService extends IService<StoreMenuRole> {

    /**
     * 通过角色获取菜单权限列表
     *
     * @param roleId
     * @return
     */
    List<StoreMenuRole> findByRoleId(String roleId);


    /**
     * 根据角色集合获取拥有的菜单具体权限
     *
     * @param clerkId
     * @return
     */
    List<StoreUserMenuDTO> findAllMenu(String clerkId, String memberId);


    /**
     * 更新某角色拥有到菜单
     *
     * @param roleId    角色id
     * @param roleMenus
     */
    void updateRoleMenu(String roleId, List<StoreMenuRole> roleMenus);

    /**
     * 根据角色id 删除数据
     *
     * @param roleId
     */
    void delete(String roleId);

    /**
     * 根据角色id 删除数据
     *
     * @param roleId
     */
    void delete(List<String> roleId);

}