package com.jzo2o.mall.member.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jzo2o.mall.common.cache.CachePrefix;
import com.jzo2o.mall.common.enums.UserEnums;
import com.jzo2o.mall.member.mapper.StoreDepartmentRoleMapper;
import com.jzo2o.mall.member.model.domain.StoreDepartmentRole;
import com.jzo2o.mall.member.service.StoreDepartmentRoleService;
import com.jzo2o.redis.helper.Cache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 部门角色业务层实现
 */
@Service
public class StoreDepartmentRoleServiceImpl extends ServiceImpl<StoreDepartmentRoleMapper, StoreDepartmentRole> implements StoreDepartmentRoleService {

    @Autowired
    private Cache cache;

    @Override
    public List<StoreDepartmentRole> listByDepartmentId(String storeDepartmentId) {
        QueryWrapper queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("department_id", storeDepartmentId);
        return this.baseMapper.selectList(queryWrapper);
    }

    @Override
    public void updateByDepartmentId(String storeDepartmentId, List<StoreDepartmentRole> storeDepartmentRoles) {
        if (!storeDepartmentRoles.isEmpty()) {
            QueryWrapper queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("department_id", storeDepartmentId);
            this.remove(queryWrapper);
            this.saveBatch(storeDepartmentRoles);
            cache.vagueDel(CachePrefix.PERMISSION_LIST.getPrefix(UserEnums.STORE));
            cache.vagueDel(CachePrefix.STORE_USER_MENU.getPrefix());
        }
    }

    @Override
    public void deleteByDepartment(List<String> ids) {

        QueryWrapper queryWrapper = new QueryWrapper<>();
        queryWrapper.in("department_id", ids);
        this.remove(queryWrapper);
        cache.vagueDel(CachePrefix.PERMISSION_LIST.getPrefix(UserEnums.STORE));
        cache.vagueDel(CachePrefix.STORE_USER_MENU.getPrefix());
    }
}