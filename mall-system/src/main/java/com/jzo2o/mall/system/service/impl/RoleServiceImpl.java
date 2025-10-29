package com.jzo2o.mall.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jzo2o.mall.common.cache.CachePrefix;
import com.jzo2o.mall.common.enums.ResultCode;
import com.jzo2o.mall.common.enums.UserEnums;
import com.jzo2o.mall.common.exception.ServiceException;
import com.jzo2o.mall.system.mapper.RoleMapper;
import com.jzo2o.mall.system.model.domain.Role;
import com.jzo2o.mall.system.service.DepartmentRoleService;
import com.jzo2o.mall.system.service.RoleMenuService;
import com.jzo2o.mall.system.service.RoleService;
import com.jzo2o.mall.system.service.UserRoleService;
import com.jzo2o.redis.helper.Cache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 角色业务层实现
 */
@Service
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements RoleService {

    /**
     * 部门角色
     */
    @Autowired
    private DepartmentRoleService departmentRoleService;
    /**
     * 用户权限
     */
    @Autowired
    private UserRoleService userRoleService;

    @Autowired
    private RoleMenuService roleMenuService;
    @Autowired
    private Cache cache;

    @Override
    public List<Role> findByDefaultRole(Boolean defaultRole) {
        QueryWrapper<Role> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("default_role", true);
        return baseMapper.selectList(queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRoles(List<String> roleIds) {
        QueryWrapper queryWrapper = new QueryWrapper<>();
        queryWrapper.in("role_id", roleIds);
        if (departmentRoleService.count(queryWrapper) > 0) {
            throw new ServiceException(ResultCode.PERMISSION_DEPARTMENT_ROLE_ERROR);
        }
        if (userRoleService.count(queryWrapper) > 0) {
            throw new ServiceException(ResultCode.PERMISSION_USER_ROLE_ERROR);
        }
        //删除角色
        this.removeByIds(roleIds);
        //删除角色与菜单关联
        roleMenuService.remove(queryWrapper);
        cache.vagueDel(CachePrefix.USER_MENU.getPrefix(UserEnums.MANAGER));
        cache.vagueDel(CachePrefix.PERMISSION_LIST.getPrefix(UserEnums.MANAGER));
    }
}
