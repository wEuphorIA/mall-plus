package com.jzo2o.mall.member.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jzo2o.mall.common.enums.ResultUtil;
import com.jzo2o.mall.common.model.ResultMessage;
import com.jzo2o.mall.member.model.domain.Store;
import com.jzo2o.mall.member.model.dto.*;
import com.jzo2o.mall.member.service.StoreDetailService;
import com.jzo2o.mall.member.service.StoreService;
import com.jzo2o.mysql.domain.PageVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 管理端,店铺管理接口
 */
@Api(tags = "管理端,店铺管理接口")
@RestController
@RequestMapping("/store/store")
public class StoreManagerController {

    /**
     * 店铺
     */
    @Autowired
    private StoreService storeService;
    /**
     * 店铺详情
     */
    @Autowired
    private StoreDetailService storeDetailService;

    @ApiOperation(value = "获取店铺分页列表")
    @GetMapping("/all")
    public List<Store> getAll() {
        List<Store> list = storeService.list(new QueryWrapper<Store>().eq("store_disable", "OPEN"));
        return list;
    }

    @ApiOperation(value = "获取店铺分页列表")
    @GetMapping
    public IPage<StoreDTO> getByPage(StoreSearchParams entity) {
        IPage<StoreDTO> byConditionPage = storeService.findByConditionPage(entity);
        return byConditionPage;
    }

    @ApiOperation(value = "获取店铺详情")
    @ApiImplicitParam(name = "storeId", value = "店铺ID", required = true, paramType = "path", dataType = "String")
    @GetMapping(value = "/get/detail/{storeId}")
    public StoreDetailDTO detail(@PathVariable String storeId) {
        // todo 对于刚提交审核的信息需要等待缓存失效后才能操作,否则缓存信息还在
        StoreDetailDTO storeDetailDTO = storeDetailService.getStoreDetailDTO(storeId);
        return storeDetailDTO;
    }

    @ApiOperation(value = "添加店铺")
    @PostMapping(value = "/add")
    public Store add(@Valid AdminStoreApplyDTO adminStoreApplyDTO) {
        Store add = storeService.add(adminStoreApplyDTO);
        return add;
    }

    @ApiOperation(value = "编辑店铺")
    @ApiImplicitParam(name = "id", value = "店铺ID", required = true, paramType = "path", dataType = "String")
    @PutMapping(value = "/edit/{id}")
    public Store edit(@PathVariable String id, @Valid StoreEditDTO storeEditDTO) {
        storeEditDTO.setStoreId(id);
        Store edit = storeService.edit(storeEditDTO);
        return edit;
    }

    @ApiOperation(value = "审核店铺申请")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "passed", value = "是否通过审核 0 通过 1 拒绝 编辑操作则不需传递", required = false,paramType = "query", dataType = "int"),
            @ApiImplicitParam(name = "id", value = "店铺id", required = true, paramType = "path", dataType = "String")
    })
    @PutMapping(value = "/audit/{id}/{passed}")
    public void audit(@PathVariable String id, @PathVariable Integer passed) {
        storeService.audit(id, passed);
    }


    @ApiOperation(value = "关闭店铺")
    @ApiImplicitParam(name = "id", value = "店铺id", required = true, dataType = "String", paramType = "path")
    @PutMapping(value = "/disable/{id}")
    public void disable(@PathVariable String id) {
        storeService.disable(id);
    }

    @ApiOperation(value = "开启店铺")
    @ApiImplicitParam(name = "id", value = "店铺id", required = true, dataType = "String", paramType = "path")
    @PutMapping(value = "/enable/{id}")
    public void enable(@PathVariable String id) {
        storeService.enable(id);
    }

//    @ApiOperation(value = "查询一级分类列表")
//    @ApiImplicitParam(name = "storeId", value = "店铺id", required = true, dataType = "String", paramType = "path")
//    @GetMapping(value = "/managementCategory/{storeId}")
//    public ResultMessage<List<StoreManagementCategoryVO>> firstCategory(@PathVariable String storeId) {
//        return ResultUtil.data(this.storeDetailService.goodsManagementCategory(storeId));
//    }


    @ApiOperation(value = "根据会员id查询店铺信息")
    @GetMapping("/{memberId}/member")
    public Store getByMemberId(@Valid @PathVariable String memberId) {
        List<Store> list = storeService.list(new QueryWrapper<Store>().eq("member_id", memberId));
        if (list.size() > 0) {
            return list.get(0);
        }
        return null;
    }

//    @ApiOperation(value = "将所有店铺导入店员表")
//    @PostMapping("store/to/clerk")
//    public ResultMessage<Object> storeToClerk(){
//        this.storeService.storeToClerk();
//        return ResultUtil.success();
//    }
}
