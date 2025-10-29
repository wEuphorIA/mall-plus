package com.jzo2o.mall.system.controller;

import com.jzo2o.common.utils.UserContext;
import com.jzo2o.mall.common.model.AuthUser;
import com.jzo2o.mall.system.model.domain.Menu;
import com.jzo2o.mall.system.model.dto.MenuDTO;
import com.jzo2o.mall.system.model.dto.MenuSearchParamsDTO;
import com.jzo2o.mall.system.service.MenuService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * 管理端,菜单管理接口
 */
@Slf4j
@RestController
@Api(tags = "管理端,菜单管理接口")
@RequestMapping("/permission/menu")
public class MenuManagerController {

    @Autowired
    private MenuService menuService;


    @ApiOperation(value = "搜索菜单")
    @GetMapping
    public List<Menu> searchPermissionList(MenuSearchParamsDTO searchParams) {
        return menuService.searchList(searchParams);
    }

    @ApiOperation(value = "添加")
    @PostMapping
    public Menu add(Menu menu) {
        try {
            menuService.saveOrUpdateMenu(menu);
        } catch (Exception e) {
            log.error("添加菜单错误", e);
        }
        return menu;
    }

    @ApiImplicitParam(name = "id", value = "菜单ID", required = true, paramType = "path", dataType = "String")
    @ApiOperation(value = "编辑")
    @PutMapping(value = "/{id}")
    public Menu edit(@PathVariable String id, Menu menu) {
        menu.setId(id);
        menuService.saveOrUpdateMenu(menu);
        return menu;
    }

    @ApiOperation(value = "批量删除")
    @DeleteMapping(value = "/{ids}")
    public void delByIds(@PathVariable List<String> ids) {
        menuService.deleteIds(ids);
    }

    @ApiOperation(value = "获取所有菜单")
    @GetMapping("/tree")
    public List<MenuDTO> getAllMenuList() {
        List<MenuDTO> tree = menuService.tree();
        return tree;
    }

    @ApiOperation(value = "获取所有菜单--根据当前用户角色")
    @GetMapping("/memberMenu")
    public List<MenuDTO> memberMenu() {
        AuthUser currentUser = UserContext.getCurrentUser();
        List<MenuDTO> userTree = menuService.findUserTree(currentUser);
        return userTree;
    }
}
