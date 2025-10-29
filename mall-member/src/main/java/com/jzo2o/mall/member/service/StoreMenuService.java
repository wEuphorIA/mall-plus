package com.jzo2o.mall.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jzo2o.mall.member.model.domain.StoreMenu;
import com.jzo2o.mall.member.model.dto.MenuSearchParamsDTO;
import com.jzo2o.mall.member.model.dto.StoreMenuDTO;
import com.jzo2o.mall.member.model.dto.StoreUserMenuDTO;
import org.springframework.cache.annotation.CacheConfig;

import java.util.List;

/**
 * 店铺菜单权限业务层
 */
@CacheConfig(cacheNames = "{store_menu}")
public interface StoreMenuService extends IService<StoreMenu> {

    /**
     * 通过用户的菜单权限
     *
     * @return
     */
    List<StoreMenuDTO> findUserTree();

    /**
     * 通过用户id获取
     *
     * @param userId
     * @return
     */
    List<StoreMenu> findUserList(String userId, String clerkId);


    /**
     * 根据角色id获取菜单集合
     *
     * @param roleIds
     * @return
     */
    List<StoreMenu> findByRoleIds(String roleIds);

    /**
     * 树形结构
     *
     * @return
     */
    List<StoreMenuDTO> tree();

    /**
     * 查询列表
     *
     * @param menuSearchParams
     * @return
     */
    List<StoreMenu> searchList(MenuSearchParamsDTO menuSearchParams);

    /**
     * 批量删除
     *
     * @param ids
     */
    void deleteIds(List<String> ids);

    /**
     * 添加更新菜单
     *
     * @param storeMenu 菜单数据
     * @return 是否成功
     */
    boolean saveOrUpdateMenu(StoreMenu storeMenu);

    List<StoreUserMenuDTO> getUserRoleMenu(String clerkId);

}
