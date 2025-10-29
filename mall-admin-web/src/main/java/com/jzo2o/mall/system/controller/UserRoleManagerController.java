package com.jzo2o.mall.system.controller;

import com.jzo2o.mall.system.model.domain.UserRole;
import com.jzo2o.mall.system.service.UserRoleService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * 管理端,管理员角色接口
 */
@RestController
@Api(tags = "管理端,管理员角色接口")
@RequestMapping("/permission/userRole")
public class UserRoleManagerController {
    @Autowired
    private UserRoleService userRoleService;

    @GetMapping(value = "/{userId}")
    @ApiOperation(value = "查看管理员角色")
    public UserRole get(@PathVariable String userId) {
        UserRole userRole = userRoleService.getById(userId);
        return userRole;
    }

    @PutMapping("/{userId}")
    @ApiOperation(value = "更新角色菜单")
    public void update(@PathVariable String userId, List<UserRole> userRole) {
        userRoleService.updateUserRole(userId, userRole);
    }

}
