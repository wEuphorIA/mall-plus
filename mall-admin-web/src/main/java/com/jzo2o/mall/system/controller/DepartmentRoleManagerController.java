package com.jzo2o.mall.system.controller;

import com.jzo2o.mall.system.model.domain.DepartmentRole;
import com.jzo2o.mall.system.service.DepartmentRoleService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * 管理端,部门角色接口
 *
 * @author Chopper
 * @since 2020/11/22 14:05
 */
@RestController
@Api(tags = "管理端,部门角色接口")
@RequestMapping("/permission/departmentRole")
public class DepartmentRoleManagerController {
    @Autowired
    private DepartmentRoleService departmentRoleService;

    @GetMapping(value = "/{departmentId}")
    @ApiOperation(value = "查看部门拥有的角色")
    public List<DepartmentRole> get(@PathVariable String departmentId) {
        List<DepartmentRole> departmentRoles = departmentRoleService.listByDepartmentId(departmentId);
        return departmentRoles;
    }

    @PutMapping("/{departmentId}")
    @ApiOperation(value = "更新部门角色")
    public void update(@PathVariable String departmentId, @RequestBody List<DepartmentRole> departmentRole) {

        departmentRoleService.updateByDepartmentId(departmentId, departmentRole);
    }

}
