package com.jzo2o.mall.member.controller;

import com.jzo2o.mall.common.enums.ResultUtil;
import com.jzo2o.mall.common.model.ResultMessage;
import com.jzo2o.mall.member.model.domain.StoreMenuRole;
import com.jzo2o.mall.member.service.StoreMenuRoleService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * 店铺端,角色菜单接口
 *
 * @author Chopper
 * @since 2020/11/22 11:40
 */
@RestController
@Api(tags = "店铺端,角色菜单接口")
@RequestMapping("/roleMenu")
public class StoreMenuRoleController {
    @Autowired
    private StoreMenuRoleService storeMenuRoleService;

    @GetMapping(value = "/{roleId}")
    @ApiOperation(value = "查看某角色拥有到菜单")
    public List<StoreMenuRole> get(@PathVariable String roleId) {
        List<StoreMenuRole> byRoleId = storeMenuRoleService.findByRoleId(roleId);
        return byRoleId;
    }

    @PostMapping(value = "/{roleId}")
    @ApiOperation(value = "保存角色菜单")
    public void save(@PathVariable String roleId, @RequestBody List<StoreMenuRole> roleMenus) {
        storeMenuRoleService.updateRoleMenu(roleId, roleMenus);
    }

}
