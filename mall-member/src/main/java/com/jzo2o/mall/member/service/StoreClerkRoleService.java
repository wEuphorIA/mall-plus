package com.jzo2o.mall.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jzo2o.mall.member.model.domain.StoreClerkRole;

import java.util.List;

/**
 * 店铺店员角色业务层
 */
public interface StoreClerkRoleService extends IService<StoreClerkRole> {

    /**
     * 根据用户查看拥有的角色
     *
     * @param clerkId 店员id
     * @return
     */
    List<StoreClerkRole> listByUserId(String clerkId);

    /**
     * 根据店员id查看角色
     *
     * @param clerkId 店员id
     * @return
     */
    List<String> listId(String clerkId);

    /**
     * 更新用户拥有的角色
     *
     * @param clerkId         店员id
     * @param storeClerkRoles 角色权限
     */
    void updateClerkRole(String clerkId, List<StoreClerkRole> storeClerkRoles);


}
