package com.jzo2o.mall.member.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jzo2o.common.utils.UserContext;
import com.jzo2o.mall.common.cache.CachePrefix;
import com.jzo2o.mall.common.enums.UserEnums;
import com.jzo2o.mall.common.model.AuthUser;
import com.jzo2o.mall.member.mapper.StoreMenuRoleMapper;
import com.jzo2o.mall.member.model.domain.StoreMenuRole;
import com.jzo2o.mall.member.model.dto.StoreUserMenuDTO;
import com.jzo2o.mall.member.service.StoreMenuRoleService;
import com.jzo2o.mall.member.service.StoreMenuService;
import com.jzo2o.redis.helper.Cache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
 * 角色菜单业务层实现
 *
 * @author Chopper
 * @since 2020/11/22 11:43
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class StoreMenuRoleServiceImpl extends ServiceImpl<StoreMenuRoleMapper, StoreMenuRole> implements StoreMenuRoleService {

    /**
     * 菜单
     */
    @Autowired
    private StoreMenuService storeMenuService;

    @Autowired
    private Cache cache;

    @Override
    public List<StoreMenuRole> findByRoleId(String roleId) {
        LambdaQueryWrapper<StoreMenuRole> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(StoreMenuRole::getRoleId, roleId);
        return this.baseMapper.selectList(queryWrapper);
    }

    @Override
    public List<StoreUserMenuDTO> findAllMenu(String clerkId, String memberId) {
        String cacheKey = CachePrefix.STORE_USER_MENU.getPrefix() + memberId;
        List<StoreUserMenuDTO> menuList = (List<StoreUserMenuDTO>) cache.get(cacheKey);
        if (menuList == null || menuList.isEmpty()) {
            menuList = storeMenuService.getUserRoleMenu(clerkId);
            cache.put(cacheKey, menuList);
        }
        return menuList;
    }


    @Override
    public void updateRoleMenu(String roleId, List<StoreMenuRole> roleMenus) {
        AuthUser tokenUser =  UserContext.getCurrentUser();
        try {
            roleMenus.forEach(role -> role.setStoreId(tokenUser.getStoreId()));
            //删除角色已经绑定的菜单
            this.delete(roleId);
            //重新保存角色菜单关系
            this.saveBatch(roleMenus);

            cache.vagueDel(CachePrefix.PERMISSION_LIST.getPrefix(UserEnums.STORE));
            cache.vagueDel(CachePrefix.STORE_USER_MENU.getPrefix());
        } catch (Exception e) {
            log.error("修改用户权限错误", e);
        }
    }

    @Override
    public void delete(String roleId) {
        //删除
        QueryWrapper<StoreMenuRole> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("role_id", roleId);
        this.remove(queryWrapper);
        cache.vagueDel(CachePrefix.PERMISSION_LIST.getPrefix(UserEnums.STORE));
        cache.vagueDel(CachePrefix.STORE_USER_MENU.getPrefix());
    }

    @Override
    public void delete(List<String> roleId) {
        //删除
        QueryWrapper<StoreMenuRole> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("role_id", roleId);
        this.remove(queryWrapper);
        cache.vagueDel(CachePrefix.PERMISSION_LIST.getPrefix(UserEnums.STORE));
        cache.vagueDel(CachePrefix.STORE_USER_MENU.getPrefix());
    }
}