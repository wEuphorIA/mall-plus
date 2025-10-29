package com.jzo2o.mall.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jzo2o.mall.common.model.AuthUser;
import com.jzo2o.mall.system.model.domain.Menu;
import com.jzo2o.mall.system.model.dto.MenuDTO;
import com.jzo2o.mall.system.model.dto.MenuSearchParamsDTO;
import com.jzo2o.mall.system.model.dto.UserMenuDTO;
import org.springframework.cache.annotation.CacheConfig;

import java.util.List;

/**
 * 权限业务层
 */
@CacheConfig(cacheNames = "{menu}")
public interface MenuService extends IService<Menu> {

    /**
     * 通过用户的菜单权限
     *
     * @return
     */
    List<MenuDTO> findUserTree(AuthUser authUser);

    /**
     * 通过用户id获取
     *
     * @param userId
     * @return
     */
    List<Menu> findUserList(String userId);


    /**
     * 根据角色id获取菜单集合
     *
     * @param roleIds
     * @return
     */
    List<Menu> findByRoleIds(String roleIds);

    /**
     * 树形结构
     *
     * @return
     */
    List<MenuDTO> tree();

    /**
     * 查询列表
     *
     * @param menuSearchParams
     * @return
     */
    List<Menu> searchList(MenuSearchParamsDTO menuSearchParams);

    /**
     * 批量删除
     *
     * @param ids
     */
    void deleteIds(List<String> ids);

    /**
     * 添加更新菜单
     *
     * @param menu 菜单数据
     * @return 是否成功
     */
    boolean saveOrUpdateMenu(Menu menu);

    /**
     * 根据角色集合获取拥有的菜单具体权限
     *
     * @param userId
     * @return
     */
    List<UserMenuDTO> findAllMenu(String userId);

}
