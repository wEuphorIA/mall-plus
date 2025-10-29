package com.jzo2o.mall.member.controller;

import com.jzo2o.mall.common.enums.ResultUtil;
import com.jzo2o.mall.common.model.ResultMessage;
import com.jzo2o.mall.member.model.domain.StoreMenu;
import com.jzo2o.mall.member.model.dto.MenuSearchParamsDTO;
import com.jzo2o.mall.member.model.dto.StoreMenuDTO;
import com.jzo2o.mall.member.service.StoreMenuService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * 管理端,菜单管理接口
 *
 * @author Chopper
 * @since 2020/11/20 12:07
 */
@Slf4j
@RestController
@Api(tags = "管理端,菜单管理接口")
@RequestMapping("/permission/storeMenu")
public class StoreMenuManagerController {

    @Autowired
    private StoreMenuService storeMenuService;

    @ApiOperation(value = "搜索菜单")
    @GetMapping
    public List<StoreMenu> searchPermissionList(MenuSearchParamsDTO searchParams) {
        List<StoreMenu> storeMenus = storeMenuService.searchList(searchParams);
        return storeMenus;
    }

    @ApiOperation(value = "添加")
    @PostMapping
    public StoreMenu add(StoreMenu menu) {
        try {
            storeMenuService.saveOrUpdateMenu(menu);
        } catch (Exception e) {
            log.error("添加菜单错误", e);
        }
        return menu;
    }

    @ApiImplicitParam(name = "id", value = "菜单ID", required = true, paramType = "path", dataType = "String")
    @ApiOperation(value = "编辑")
    @PutMapping(value = "/{id}")
    public StoreMenu edit(@PathVariable String id, StoreMenu menu) {
        menu.setId(id);
        storeMenuService.saveOrUpdateMenu(menu);
        return menu;
    }

    @ApiOperation(value = "批量删除")
    @DeleteMapping(value = "/{ids}")
    public void delByIds(@PathVariable List<String> ids) {
        storeMenuService.deleteIds(ids);
    }

    @ApiOperation(value = "获取所有菜单")
    @GetMapping("/tree")
    public List<StoreMenuDTO> getAllMenuList() {
        List<StoreMenuDTO> tree = storeMenuService.tree();
        return tree;
    }

}
