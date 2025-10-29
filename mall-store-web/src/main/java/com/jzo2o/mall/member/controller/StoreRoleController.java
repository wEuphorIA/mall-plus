package com.jzo2o.mall.member.controller;

import cn.hutool.core.util.PageUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jzo2o.common.utils.UserContext;
import com.jzo2o.mall.common.enums.ResultUtil;
import com.jzo2o.mall.common.model.AuthUser;
import com.jzo2o.mall.common.model.ResultMessage;
import com.jzo2o.mall.member.model.domain.StoreRole;
import com.jzo2o.mall.member.service.StoreRoleService;
import com.jzo2o.mysql.domain.PageVO;
import com.jzo2o.mysql.utils.PageUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;


/**
 * 店铺端,角色管理接口
 */
@RestController
@Api(tags = "店铺端,店铺角色管理接口")
@RequestMapping("/role")
public class StoreRoleController {
    @Autowired
    private StoreRoleService storeRoleService;

    @PostMapping
    @ApiOperation(value = "添加角色")
    public StoreRole add(StoreRole storeRole) {
        storeRoleService.saveStoreRole(storeRole);
        return storeRole;
    }

    @GetMapping
    @ApiOperation(value = "查询店铺角色")
    public Page page(PageVO pageVo, StoreRole storeRole) {
        AuthUser tokenUser =  UserContext.getCurrentUser();
        storeRole.setStoreId(tokenUser.getStoreId());
        Page page = storeRoleService.page(PageUtils.initPage(pageVo), PageUtils.initWrapper(storeRole));
        return page;
    }

    @PutMapping("/{roleId}")
    @ApiOperation(value = "编辑店铺角色")
    public StoreRole edit(@PathVariable String roleId, StoreRole storeRole) {
        storeRole.setId(roleId);
        storeRoleService.update(storeRole);
        return storeRole;
    }

    @DeleteMapping(value = "/{ids}")
    @ApiOperation(value = "批量删除店铺角色")
    public void delByIds(@PathVariable List<String> ids) {
        storeRoleService.deleteRoles(ids);
    }


}
