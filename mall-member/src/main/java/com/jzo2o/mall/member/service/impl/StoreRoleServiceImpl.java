package com.jzo2o.mall.member.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jzo2o.common.utils.UserContext;
import com.jzo2o.mall.common.cache.CachePrefix;
import com.jzo2o.mall.common.enums.ResultCode;
import com.jzo2o.mall.common.enums.UserEnums;
import com.jzo2o.mall.common.exception.ServiceException;
import com.jzo2o.mall.common.model.AuthUser;
import com.jzo2o.mall.member.mapper.StoreRoleMapper;
import com.jzo2o.mall.member.model.domain.StoreRole;
import com.jzo2o.mall.member.service.StoreClerkRoleService;
import com.jzo2o.mall.member.service.StoreDepartmentRoleService;
import com.jzo2o.mall.member.service.StoreMenuRoleService;
import com.jzo2o.mall.member.service.StoreRoleService;
import com.jzo2o.redis.helper.Cache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
 * 角色业务层实现
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class StoreRoleServiceImpl extends ServiceImpl<StoreRoleMapper, StoreRole> implements StoreRoleService {

    /**
     * 部门角色
     */
    @Autowired
    private StoreDepartmentRoleService storeDepartmentRoleService;
    /**
     * 用户权限
     */
    @Autowired
    private StoreClerkRoleService storeClerkRoleService;

    @Autowired
    private StoreMenuRoleService storeMenuRoleService;

    @Autowired
    private Cache cache;

    @Override
    public List<StoreRole> findByDefaultRole(Boolean defaultRole) {
        QueryWrapper<StoreRole> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("default_role", true);
        return baseMapper.selectList(queryWrapper);
    }

    @Override
    public void deleteRoles(List<String> roleIds) {
        AuthUser tokenUser =  UserContext.getCurrentUser();
        //校验是否为当前店铺
        QueryWrapper queryWrapper = new QueryWrapper<>();
        queryWrapper.in("id", roleIds);
        List<StoreRole> roles = this.baseMapper.selectList(queryWrapper);
        roles.forEach(role -> {
            if (!role.getStoreId().equals(tokenUser.getStoreId())) {
                throw new ServiceException(ResultCode.USER_AUTHORITY_ERROR);
            }
        });
        queryWrapper = new QueryWrapper<>();
        queryWrapper.in("role_id", roleIds);
        //校验是否绑定店铺部门
        if (storeDepartmentRoleService.count(queryWrapper) > 0) {
            throw new ServiceException(ResultCode.PERMISSION_DEPARTMENT_ROLE_ERROR);
        }
        //校验是否绑定店员
        if (storeClerkRoleService.count(queryWrapper) > 0) {
            throw new ServiceException(ResultCode.PERMISSION_USER_ROLE_ERROR);
        }
        //删除角色
        this.removeByIds(roleIds);
        //删除角色与菜单关联
        storeMenuRoleService.remove(queryWrapper);
        cache.vagueDel(CachePrefix.PERMISSION_LIST.getPrefix(UserEnums.STORE));
        cache.vagueDel(CachePrefix.STORE_USER_MENU.getPrefix());
    }

    @Override
    public Boolean update(StoreRole storeRole) {
        AuthUser tokenUser =  UserContext.getCurrentUser();
        StoreRole storeRoleTemp = this.getById(storeRole.getId());
        //校验店铺角色是否存在
        if (storeRoleTemp == null) {
            throw new ServiceException(ResultCode.PERMISSION_ROLE_NOT_FOUND_ERROR);
        }
        //校验店铺角色是否属于当前店铺
        if (!storeRoleTemp.getStoreId().equals(tokenUser.getStoreId())) {
            throw new ServiceException(ResultCode.PERMISSION_ROLE_NOT_FOUND_ERROR);
        }
        cache.vagueDel(CachePrefix.PERMISSION_LIST.getPrefix(UserEnums.STORE));
        cache.vagueDel(CachePrefix.STORE_USER_MENU.getPrefix());
        return updateById(storeRole);
    }


    @Override
    public Boolean saveStoreRole(StoreRole storeRole) {
        AuthUser tokenUser =  UserContext.getCurrentUser();
        storeRole.setStoreId(tokenUser.getStoreId());
        return save(storeRole);
    }

    @Override
    public List<StoreRole> list(List<String> ids) {
        AuthUser tokenUser =  UserContext.getCurrentUser();
        QueryWrapper<StoreRole> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("store_id", tokenUser.getStoreId());
        queryWrapper.in("id", ids);
        return this.baseMapper.selectList(queryWrapper);
    }
}
