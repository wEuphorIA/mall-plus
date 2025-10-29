package com.jzo2o.mall.promotion.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jzo2o.mall.common.enums.ResultUtil;
import com.jzo2o.mall.common.model.ResultMessage;
import com.jzo2o.mall.promotion.model.domain.Seckill;
import com.jzo2o.mall.promotion.model.domain.SeckillApply;
import com.jzo2o.mall.promotion.model.dto.SeckillDTO;
import com.jzo2o.mall.promotion.model.dto.SeckillSearchParams;
import com.jzo2o.mall.promotion.service.SeckillApplyService;
import com.jzo2o.mall.promotion.service.SeckillService;
import com.jzo2o.mysql.domain.PageVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;

/**
 * 管理端,秒杀活动接口
 **/
@RestController
@Api(tags = "管理端,秒杀活动接口")
@RequestMapping("/promotion/seckill")
public class SeckillManagerController {
    @Autowired
    private SeckillService seckillService;
    @Autowired
    private SeckillApplyService seckillApplyService;


    @ApiOperation(value = "初始化秒杀活动(初始化方法，默认初始化30天内的活动）")
    @GetMapping("/init")
    public void addSeckill() {
        seckillService.init();
    }


    @ApiOperation(value = "修改秒杀活动")
    @PutMapping(consumes = "application/json", produces = "application/json")
    public SeckillDTO updateSeckill(@RequestBody SeckillDTO seckill) {
        seckillService.updatePromotions(seckill);
        return seckill;
    }

    @ApiOperation(value = "通过id获取")
    @ApiImplicitParam(name = "id", value = "秒杀活动ID", required = true, dataType = "String", paramType = "path")
    @GetMapping(value = "/{id}")
    public Seckill get(@PathVariable String id) {
        Seckill seckill = seckillService.getById(id);
        return seckill;
    }

    @ApiOperation(value = "分页查询秒杀活动列表")
    @GetMapping
    public IPage<Seckill> getAll(SeckillSearchParams param, PageVO pageVo) {
        IPage<Seckill> seckillIPage = seckillService.pageFindAll(param, pageVo);
        return seckillIPage;
    }

    @ApiOperation(value = "删除一个秒杀活动")
    @ApiImplicitParam(name = "id", value = "秒杀活动ID", required = true, dataType = "String", paramType = "path")
    @DeleteMapping("/{id}")
    public void deleteSeckill(@PathVariable String id) {
        seckillService.removePromotions(Collections.singletonList(id));
    }

    @ApiOperation(value = "操作秒杀活动状态")
    @ApiImplicitParam(name = "id", value = "秒杀活动ID", required = true, dataType = "String", paramType = "path")
    @PutMapping("/status/{id}")
    public void updateSeckillStatus(@PathVariable String id, Long startTime, Long endTime) {
        seckillService.updateStatus(Collections.singletonList(id), startTime, endTime);
    }

    @ApiOperation(value = "获取秒杀活动申请列表")
    @GetMapping("/apply")
    public IPage<SeckillApply> getSeckillApply(SeckillSearchParams param, PageVO pageVo) {
        IPage<SeckillApply> seckillApply = seckillApplyService.getSeckillApplyPage(param, pageVo);
        return seckillApply;
    }

    @DeleteMapping("/apply/{seckillId}/{id}")
    @ApiOperation(value = "删除秒杀活动申请")
    public void deleteSeckillApply(@PathVariable String seckillId, @PathVariable String id) {
        seckillApplyService.removeSeckillApply(seckillId, id);
    }


}
