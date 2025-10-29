package com.jzo2o.mall.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jzo2o.mall.common.cache.CachePrefix;
import com.jzo2o.mall.common.enums.UserEnums;
import com.jzo2o.mall.system.mapper.DepartmentRoleMapper;
import com.jzo2o.mall.system.model.domain.DepartmentRole;
import com.jzo2o.mall.system.service.DepartmentRoleService;
import com.jzo2o.redis.helper.Cache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 部门角色业务层实现
 */
@Service
public class DepartmentRoleServiceImpl extends ServiceImpl<DepartmentRoleMapper, DepartmentRole> implements DepartmentRoleService {

    @Autowired
    private Cache cache;

    @Override
    public List<DepartmentRole> listByDepartmentId(String departmentId) {
        QueryWrapper queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("department_id", departmentId);
        return this.baseMapper.selectList(queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateByDepartmentId(String departmentId, List<DepartmentRole> departmentRoles) {
        QueryWrapper queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("department_id", departmentId);
        this.remove(queryWrapper);

        this.saveBatch(departmentRoles);
        cache.vagueDel(CachePrefix.USER_MENU.getPrefix(UserEnums.MANAGER));
        cache.vagueDel(CachePrefix.PERMISSION_LIST.getPrefix(UserEnums.MANAGER));
    }

    @Override
    public void deleteByDepartment(List<String> ids) {
        QueryWrapper queryWrapper = new QueryWrapper<>();
        queryWrapper.in("department_id", ids);
        this.remove(queryWrapper);
        cache.vagueDel(CachePrefix.USER_MENU.getPrefix(UserEnums.MANAGER));
        cache.vagueDel(CachePrefix.PERMISSION_LIST.getPrefix(UserEnums.MANAGER));
    }
}