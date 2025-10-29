package com.jzo2o.mall.system.controller;

import cn.hutool.core.util.PageUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jzo2o.mall.common.enums.ResultUtil;
import com.jzo2o.mall.common.model.ResultMessage;
import com.jzo2o.mall.order.model.domain.Logistics;
import com.jzo2o.mall.order.service.LogisticsService;
import com.jzo2o.mysql.domain.PageVO;
import com.jzo2o.mysql.utils.PageUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * 管理端,物流公司接口
 */
@RestController
@Api(tags = "管理端,物流公司接口")
@RequestMapping("/other/logistics")
public class LogisticsManagerController {
    @Autowired
    private LogisticsService logisticsService;

    @ApiOperation(value = "通过id获取物流公司")
    @GetMapping(value = "/get/{id}")
    public Logistics get(@PathVariable String id) {
        Logistics logistics = logisticsService.getById(id);
        return logistics;
    }

    @ApiOperation(value = "分页获取物流公司")
    @GetMapping(value = "/getByPage")
    public IPage<Logistics> getByPage(PageVO page) {
        Page<Logistics> page1 = logisticsService.page(PageUtils.initPage(page));
        return page1;
    }

    @ApiOperation(value = "编辑物流公司")
    @ApiImplicitParam(name = "id", value = "物流公司ID", required = true, paramType = "path", dataType = "string")
    @PutMapping(value = "/{id}")
    public Logistics update(@NotNull @PathVariable String id, @Valid Logistics logistics) {
        logistics.setId(id);
        logisticsService.updateById(logistics);
        return logistics;
    }

    @ApiOperation(value = "添加物流公司")
    @PostMapping(value = "/save")
    public Logistics save(@Valid Logistics logistics) {
        logisticsService.save(logistics);
        return logistics;
    }

    @ApiOperation(value = "删除物流公司")
    @ApiImplicitParam(name = "id", value = "物流公司ID", required = true, dataType = "String", paramType = "path")
    @DeleteMapping(value = "/delete/{id}")
    public void  delAllByIds(@PathVariable String id) {
        logisticsService.removeById(id);
    }
}
