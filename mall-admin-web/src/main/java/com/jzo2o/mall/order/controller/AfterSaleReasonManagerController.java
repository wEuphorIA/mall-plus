package com.jzo2o.mall.order.controller;

import cn.hutool.core.util.PageUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jzo2o.mall.aftersale.model.domain.AfterSaleReason;
import com.jzo2o.mall.aftersale.service.AfterSaleReasonService;
import com.jzo2o.mall.common.enums.ResultUtil;
import com.jzo2o.mall.common.model.ResultMessage;
import com.jzo2o.mysql.domain.PageVO;
import com.jzo2o.mysql.utils.PageUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 管理端,售后原因接口
 */
@RestController
@RequestMapping("/order/afterSaleReason")
@Api(tags = "管理端,售后原因接口")
public class AfterSaleReasonManagerController {

    /**
     * 售后原因
     */
    @Autowired
    private AfterSaleReasonService afterSaleReasonService;

    @ApiOperation(value = "查看售后原因")
    @ApiImplicitParam(name = "id", value = "售后原因ID", required = true, dataType = "String", paramType = "path")
    @GetMapping(value = "/{id}")
    public ResultMessage<AfterSaleReason> get(@PathVariable String id) {

        return ResultUtil.data(afterSaleReasonService.getById(id));
    }

    @ApiOperation(value = "分页获取售后原因")
    @GetMapping(value = "/getByPage")
    @ApiImplicitParam(name = "serviceType", value = "售后类型", required = true, dataType = "String", paramType = "query")
    public IPage<AfterSaleReason> getByPage(PageVO page, @RequestParam String serviceType) {
        Page<AfterSaleReason> objectPage = PageUtils.initPage(page);
        QueryWrapper<AfterSaleReason> service_type = new QueryWrapper<AfterSaleReason>().eq("service_Type", serviceType);
        Page<AfterSaleReason> result = afterSaleReasonService.page(objectPage, service_type);
        return result;
    }

    @ApiOperation(value = "添加售后原因")
    @PostMapping
    public AfterSaleReason save(@Valid AfterSaleReason afterSaleReason) {
        boolean save = afterSaleReasonService.save(afterSaleReason);
        return afterSaleReason;
    }

    @ApiOperation(value = "修改售后原因")
    @ApiImplicitParam(name = "id", value = "关键词ID", required = true, dataType = "String", paramType = "path")
    @PutMapping("update/{id}")
    public AfterSaleReason update(@Valid AfterSaleReason afterSaleReason, @PathVariable("id") String id) {
        afterSaleReason.setId(id);
        AfterSaleReason afterSaleReason1 = afterSaleReasonService.editAfterSaleReason(afterSaleReason);
        return afterSaleReason1;
    }

    @ApiOperation(value = "删除售后原因")
    @ApiImplicitParam(name = "id", value = "售后原因ID", required = true, dataType = "String", paramType = "path")
    @DeleteMapping(value = "/delByIds/{id}")
    public void delAllByIds(@PathVariable String id) {
        afterSaleReasonService.removeById(id);
    }
}
