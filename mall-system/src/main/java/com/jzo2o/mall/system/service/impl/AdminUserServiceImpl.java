package com.jzo2o.mall.system.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jzo2o.common.utils.StringUtils;
import com.jzo2o.mall.common.cache.CachePrefix;
import com.jzo2o.mall.common.enums.ResultCode;
import com.jzo2o.mall.common.enums.UserEnums;
import com.jzo2o.mall.common.exception.ServiceException;
import com.jzo2o.mall.common.model.AuthUser;
import com.jzo2o.mall.system.mapper.AdminUserMapper;
import com.jzo2o.mall.system.model.domain.AdminUser;
import com.jzo2o.mall.system.model.domain.Department;
import com.jzo2o.mall.system.model.domain.Role;
import com.jzo2o.mall.system.model.domain.UserRole;
import com.jzo2o.mall.system.model.dto.AdminUserDTO;
import com.jzo2o.mall.system.service.*;
import com.jzo2o.redis.helper.Cache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户业务层实现
 */
@Slf4j
@Service
public class AdminUserServiceImpl extends ServiceImpl<AdminUserMapper, AdminUser> implements AdminUserService {
    /**
     * 角色长度
     */
    private final int rolesMaxSize = 10;
    @Autowired
    private UserRoleService userRoleService;
    @Autowired
    private RoleService roleService;
    @Autowired
    private DepartmentService departmentService;
    @Autowired
    private Cache cache;

    @Override
    public IPage<AdminUserDTO> adminUserPage(Page initPage, QueryWrapper<AdminUser> initWrapper) {
        Page<AdminUser> adminUserPage = page(initPage, initWrapper);
        List<Role> roles = roleService.list();
        List<Department> departments = departmentService.list();

        List<AdminUserDTO> result = new ArrayList<>();

        adminUserPage.getRecords().forEach(adminUser -> {
            AdminUserDTO adminUserDTO = new AdminUserDTO(adminUser);
            if (!CharSequenceUtil.isEmpty(adminUser.getDepartmentId())) {
                try {
                    List<Department> collect = departments.stream().filter
                                    (department -> department.getId().equals(adminUser.getDepartmentId()))
                            .collect(Collectors.toList());
                    if(!collect.isEmpty()){
                        adminUserDTO.setDepartmentTitle(collect.get(0).getTitle());
                    }

                } catch (Exception e) {
                    log.debug("权限部门信息异常", e);
                }
            }
            if (!CharSequenceUtil.isEmpty(adminUser.getRoleIds())) {
                try {
                    List<String> memberRoles = Arrays.asList(adminUser.getRoleIds().split(","));
                    adminUserDTO.setRoles(
                            roles.stream().filter
                                            (role -> memberRoles.contains(role.getId()))
                                    .collect(Collectors.toList())
                    );
                } catch (Exception e) {
                    log.debug("权限色信息异常", e);
                }
            }
            result.add(adminUserDTO);
        });
        Page<AdminUserDTO> pageResult = new Page<>(adminUserPage.getCurrent(), adminUserPage.getSize(), adminUserPage.getTotal());
        pageResult.setRecords(result);
        return pageResult;

    }


    @Override
    public AdminUser findByUsername(String username) {
        return getOne(new LambdaQueryWrapper<AdminUser>().eq(AdminUser::getUsername, username), false);
    }

    @Override
//    @SystemLogPoint(description = "修改管理员", customerLog = "'修改管理员:'+#adminUser.username")
    @Transactional(rollbackFor = Exception.class)
    public boolean updateAdminUser(AdminUser adminUser, List<String> roles) {

        if (roles != null && !roles.isEmpty()) {

            if (roles.size() > rolesMaxSize) {
                throw new ServiceException(ResultCode.PERMISSION_BEYOND_TEN);
            }
            adminUser.setRoleIds(CharSequenceUtil.join(",", roles));

            updateRole(adminUser.getId(), roles);
        } else {
            adminUser.setRoleIds("");
        }

        this.updateById(adminUser);
        return true;
    }


    @Override
    public void editPassword(AuthUser tokenUser,String password, String newPassword) {
        AdminUser user = this.getById(tokenUser.getIdString());
        if (!new BCryptPasswordEncoder().matches(password, user.getPassword())) {
            throw new ServiceException(ResultCode.USER_OLD_PASSWORD_ERROR);
        }
        String newEncryptPass = new BCryptPasswordEncoder().encode(newPassword);
        user.setPassword(newEncryptPass);
        this.updateById(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetPassword(List<String> ids) {
        LambdaQueryWrapper<AdminUser> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.in(AdminUser::getId, ids);
        List<AdminUser> adminUsers = this.list(lambdaQueryWrapper);
        String password = StringUtils.md5("123456");
        String newEncryptPass = new BCryptPasswordEncoder().encode(password);
        if (null != adminUsers && !adminUsers.isEmpty()) {
            adminUsers.forEach(item -> item.setPassword(newEncryptPass));
            this.updateBatchById(adminUsers);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveAdminUser(AdminUserDTO adminUser, List<String> roles) {
        AdminUser dbUser = new AdminUser();
        BeanUtil.copyProperties(adminUser, dbUser);
        dbUser.setPassword(new BCryptPasswordEncoder().encode(dbUser.getPassword()));
        if (roles.size() > rolesMaxSize) {
            throw new ServiceException(ResultCode.PERMISSION_BEYOND_TEN);
        }
        if (!roles.isEmpty()) {
            dbUser.setRoleIds(CharSequenceUtil.join(",", roles));
        }
        this.save(dbUser);
        dbUser = this.findByUsername(dbUser.getUsername());
        updateRole(dbUser.getId(), roles);
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCompletely(List<String> ids) {
        //彻底删除超级管理员
        this.removeByIds(ids);

        //删除管理员角色
        QueryWrapper<UserRole> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("user_id", ids);
        userRoleService.remove(queryWrapper);

//        this.logout(ids);
    }

    /**
     * 更新用户角色
     *
     * @param userId 用户id
     * @param roles  角色id集合
     */
    private void updateRole(String userId, List<String> roles) {

        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("user_id", userId);
        userRoleService.remove(queryWrapper);

        if (roles.isEmpty()) {
            return;
        }
        List<UserRole> userRoles = new ArrayList<>(roles.size());
        roles.forEach(id -> userRoles.add(new UserRole(userId, id)));
        userRoleService.updateUserRole(userId, userRoles);
        cache.vagueDel(CachePrefix.USER_MENU.getPrefix(UserEnums.MANAGER));
        cache.vagueDel(CachePrefix.PERMISSION_LIST.getPrefix(UserEnums.MANAGER));
    }
}
