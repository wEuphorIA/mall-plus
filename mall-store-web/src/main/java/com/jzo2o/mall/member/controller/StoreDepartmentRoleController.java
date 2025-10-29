package com.jzo2o.mall.member.controller;

import com.jzo2o.mall.common.enums.ResultUtil;
import com.jzo2o.mall.common.model.ResultMessage;
import com.jzo2o.mall.member.model.domain.StoreDepartmentRole;
import com.jzo2o.mall.member.service.StoreDepartmentRoleService;
import com.jzo2o.mall.system.model.domain.DepartmentRole;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * 店铺端,部门角色接口
 */
@RestController
@Api(tags = "店铺端,部门角色接口")
@RequestMapping("/departmentRole")
public class StoreDepartmentRoleController {
    @Autowired
    private StoreDepartmentRoleService storeDepartmentRoleService;

    @GetMapping(value = "/{departmentId}")
    @ApiOperation(value = "查看部门拥有的角色")
    public List<StoreDepartmentRole> get(@PathVariable String departmentId) {
        List<StoreDepartmentRole> storeDepartmentRoles = storeDepartmentRoleService.listByDepartmentId(departmentId);
        return storeDepartmentRoles;
    }

    @PutMapping("/{departmentId}")
    @ApiOperation(value = "更新部门角色")
    public  void update(@PathVariable String departmentId, @RequestBody List<StoreDepartmentRole> storeDepartmentRoles) {
        storeDepartmentRoleService.updateByDepartmentId(departmentId, storeDepartmentRoles);
    }

}
