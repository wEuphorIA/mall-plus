package com.jzo2o.mall.system.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jzo2o.mall.system.model.domain.Role;
import com.jzo2o.mall.system.service.RoleService;
import com.jzo2o.mysql.domain.PageVO;
import com.jzo2o.mysql.utils.PageUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * 管理端,角色管理接口
 */
@RestController
@Api(tags = "管理端,角色管理接口")
@RequestMapping("/permission/role")
public class RoleManagerController {
    @Autowired
    private RoleService roleService;

    @PostMapping
    @ApiOperation(value = "添加")
    public Role add(Role role) {
        roleService.save(role);
        return role;
    }

    @GetMapping
    @ApiOperation(value = "查询")
    public Page add(PageVO pageVo, Role role) {
        Page page = roleService.page(PageUtils.initPage(pageVo), PageUtils.initWrapper(role));
        return page;
    }

    @PutMapping("/{roleId}")
    @ApiOperation(value = "编辑")
    public Role edit(@PathVariable String roleId, Role role) {
        role.setId(roleId);
        roleService.updateById(role);
        return role;
    }

    @DeleteMapping(value = "/{ids}")
    @ApiOperation(value = "批量删除")
    public void delByIds(@PathVariable List<String> ids) {
        roleService.deleteRoles(ids);
    }


}
