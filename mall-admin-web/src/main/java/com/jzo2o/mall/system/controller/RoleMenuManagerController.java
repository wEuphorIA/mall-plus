package com.jzo2o.mall.system.controller;

import com.jzo2o.mall.system.model.domain.RoleMenu;
import com.jzo2o.mall.system.service.RoleMenuService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * 管理端,角色菜单接口
 */
@RestController
@Api(tags = "管理端,角色菜单接口")
@RequestMapping("/permission/roleMenu")
public class RoleMenuManagerController {
    @Autowired
    private RoleMenuService roleMenuService;

    @GetMapping(value = "/{roleId}")
    @ApiOperation(value = "查看某角色拥有到菜单")
    public List<RoleMenu> get(@PathVariable String roleId) {
        List<RoleMenu> byRoleId = roleMenuService.findByRoleId(roleId);
        return byRoleId;
    }

    @PostMapping(value = "/{roleId}")
    @ApiOperation(value = "保存角色菜单")
    public void save(@PathVariable String roleId, @RequestBody List<RoleMenu> roleMenus) {
        roleMenuService.updateRoleMenu(roleId, roleMenus);
    }

}
