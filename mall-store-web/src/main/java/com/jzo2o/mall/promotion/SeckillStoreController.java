package com.jzo2o.mall.promotion;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jzo2o.common.utils.UserContext;
import com.jzo2o.mall.common.enums.ResultUtil;
import com.jzo2o.mall.common.model.AuthUser;
import com.jzo2o.mall.common.model.ResultMessage;
import com.jzo2o.mall.promotion.model.domain.Seckill;
import com.jzo2o.mall.promotion.model.domain.SeckillApply;
import com.jzo2o.mall.promotion.model.dto.SeckillApplyDTO;
import com.jzo2o.mall.promotion.model.dto.SeckillSearchParams;
import com.jzo2o.mall.promotion.service.SeckillApplyService;
import com.jzo2o.mall.promotion.service.SeckillService;
import com.jzo2o.mysql.domain.PageVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

/**
 * 店铺端,秒杀活动接口
 **/
@RestController
@Api(tags = "店铺端,秒杀活动接口")
@RequestMapping("/promotion/seckill")
public class SeckillStoreController {
    @Autowired
    private SeckillService seckillService;
    @Autowired
    private SeckillApplyService seckillApplyService;

    @GetMapping
    @ApiOperation(value = "获取秒杀活动列表")
    public IPage<Seckill> getSeckillPage(SeckillSearchParams queryParam, PageVO pageVo) {
        IPage<Seckill> seckillPage = seckillService.pageFindAll(queryParam, pageVo);
        return seckillPage;
    }

    @GetMapping("/apply")
    @ApiOperation(value = "获取秒杀活动申请列表")
    public IPage<SeckillApply> getSeckillApplyPage(SeckillSearchParams queryParam, PageVO pageVo) {
        AuthUser authUser = UserContext.getCurrentUser();
        String storeId = Objects.requireNonNull(authUser).getStoreId();
        queryParam.setStoreId(storeId);
        IPage<SeckillApply> seckillPage = seckillApplyService.getSeckillApplyPage(queryParam, pageVo);
        return seckillPage;
    }

    @GetMapping("/{seckillId}")
    @ApiOperation(value = "获取秒杀活动信息")
    public Seckill getSeckill(@PathVariable String seckillId) {
        Seckill seckill = seckillService.getById(seckillId);
        return seckill;
    }

    @GetMapping("/apply/{seckillApplyId}")
    @ApiOperation(value = "获取秒杀活动申请")
    public SeckillApply getSeckillApply(@PathVariable String seckillApplyId) {
        SeckillApply seckillApply = seckillApplyService.getById(seckillApplyId);
        return seckillApply;
    }

    @PostMapping(path = "/apply/{seckillId}", consumes = "application/json", produces = "application/json")
    @ApiOperation(value = "添加秒杀活动申请")
    public void addSeckillApply(@PathVariable String seckillId, @RequestBody List<SeckillApplyDTO> applyVos) {
        AuthUser authUser = UserContext.getCurrentUser();
        String storeId = Objects.requireNonNull(authUser).getStoreId();
        seckillApplyService.addSeckillApply(seckillId, storeId, applyVos);
    }

    @DeleteMapping("/apply/{seckillId}/{id}")
    @ApiOperation(value = "删除秒杀活动商品")
    public void deleteSeckillApply(@PathVariable String seckillId, @PathVariable String id) {
        seckillApplyService.removeSeckillApply(seckillId, id);
    }


}
