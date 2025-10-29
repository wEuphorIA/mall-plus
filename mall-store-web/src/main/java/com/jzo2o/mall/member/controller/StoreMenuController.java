package com.jzo2o.mall.member.controller;

import com.jzo2o.mall.common.enums.ResultUtil;
import com.jzo2o.mall.common.model.ResultMessage;
import com.jzo2o.mall.member.model.dto.StoreMenuDTO;
import com.jzo2o.mall.member.service.StoreMenuService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


/**
 * 管理端,菜单管理接口
 */
@Slf4j
@RestController
@Api(tags = "店铺端,菜单管理接口")
@RequestMapping("/menu")
public class StoreMenuController {

    @Autowired
    private StoreMenuService storeMenuService;


    @ApiOperation(value = "获取所有菜单")
    @GetMapping("/tree")
    public List<StoreMenuDTO> getAllMenuList() {
        List<StoreMenuDTO> tree = storeMenuService.tree();
        return tree;
    }

    @ApiOperation(value = "获取所有菜单---根据当前用户角色")
    @GetMapping("/memberMenu")
    public List<StoreMenuDTO> memberMenu() {
        List<StoreMenuDTO> userTree = storeMenuService.findUserTree();
        return userTree;
    }
}
