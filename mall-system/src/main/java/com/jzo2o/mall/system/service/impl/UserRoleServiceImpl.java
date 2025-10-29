package com.jzo2o.mall.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jzo2o.mall.common.cache.CachePrefix;
import com.jzo2o.mall.common.enums.UserEnums;
import com.jzo2o.mall.system.mapper.UserRoleMapper;
import com.jzo2o.mall.system.model.domain.UserRole;
import com.jzo2o.mall.system.service.UserRoleService;
import com.jzo2o.redis.helper.Cache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 用户权限业务层实现
 */
@Service
public class UserRoleServiceImpl extends ServiceImpl<UserRoleMapper, UserRole> implements UserRoleService {

    @Autowired
    private Cache cache;

    @Override
    public List<UserRole> listByUserId(String userId) {
        QueryWrapper<UserRole> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        return this.baseMapper.selectList(queryWrapper);
    }

    @Override
    public List<String> listId(String userId) {
        List<UserRole> userRoleList = this.listByUserId(userId);
        List<String> strings = new ArrayList<>();
        userRoleList.forEach(item -> strings.add(item.getRoleId()));
        return strings;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUserRole(String userId, List<UserRole> userRoles) {

        //删除
        QueryWrapper<UserRole> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        this.remove(queryWrapper);

        //保存
        this.saveBatch(userRoles);
        cache.vagueDel(CachePrefix.USER_MENU.getPrefix(UserEnums.MANAGER));
        cache.vagueDel(CachePrefix.PERMISSION_LIST.getPrefix(UserEnums.MANAGER));
    }

}
