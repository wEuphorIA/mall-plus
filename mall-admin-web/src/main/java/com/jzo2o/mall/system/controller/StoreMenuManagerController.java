package com.jzo2o.mall.system.controller;//package com.jzo2o.mall.permission.controller;
//
//import com.jzo2o.mall.common.enums.ResultUtil;
//import io.swagger.annotations.Api;
//import io.swagger.annotations.ApiImplicitParam;
//import io.swagger.annotations.ApiOperation;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
//
///**
// * 管理端,菜单管理接口
// *
// * @author Chopper
// * @since 2020/11/20 12:07
// */
//@Slf4j
//@RestController
//@Api(tags = "管理端,菜单管理接口")
//@RequestMapping("/manager/permission/storeMenu")
//public class StoreMenuManagerController {
//
//    @Autowired
//    private StoreMenuService storeMenuService;
//
//    @ApiOperation(value = "搜索菜单")
//    @GetMapping
//    public ResultMessage<List<StoreMenu>> searchPermissionList(MenuSearchParams searchParams) {
//        return ResultUtil.data(storeMenuService.searchList(searchParams));
//    }
//
//    @ApiOperation(value = "添加")
//    @PostMapping
//    @DemoSite
//    public ResultMessage<StoreMenu> add(StoreMenu menu) {
//        try {
//            storeMenuService.saveOrUpdateMenu(menu);
//        } catch (Exception e) {
//            log.error("添加菜单错误", e);
//        }
//        return ResultUtil.data(menu);
//    }
//
//    @ApiImplicitParam(name = "id", value = "菜单ID", required = true, paramType = "path", dataType = "String")
//    @ApiOperation(value = "编辑")
//    @PutMapping(value = "/{id}")
//    @DemoSite
//    public ResultMessage<StoreMenu> edit(@PathVariable String id, StoreMenu menu) {
//        menu.setId(id);
//        storeMenuService.saveOrUpdateMenu(menu);
//        return ResultUtil.data(menu);
//    }
//
//    @ApiOperation(value = "批量删除")
//    @DeleteMapping(value = "/{ids}")
//    @DemoSite
//    public ResultMessage<Menu> delByIds(@PathVariable List<String> ids) {
//        storeMenuService.deleteIds(ids);
//        return ResultUtil.success();
//    }
//
//    @ApiOperation(value = "获取所有菜单")
//    @GetMapping("/tree")
//    public ResultMessage<List<StoreMenuVO>> getAllMenuList() {
//        return ResultUtil.data(storeMenuService.tree());
//    }
//
//}
