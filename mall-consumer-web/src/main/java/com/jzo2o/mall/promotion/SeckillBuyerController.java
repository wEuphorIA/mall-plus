package com.jzo2o.mall.promotion;

import cn.hutool.core.io.unit.DataUnit;
import com.jzo2o.common.utils.DateUtils;
import com.jzo2o.mall.common.enums.ResultUtil;
import com.jzo2o.mall.common.model.ResultMessage;
import com.jzo2o.mall.common.utils.DateUtil;
import com.jzo2o.mall.promotion.model.dto.SeckillGoodsDTO;
import com.jzo2o.mall.promotion.model.dto.SeckillTimelineDTO;
import com.jzo2o.mall.promotion.service.SeckillApplyService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;


/**
 * 买家端,秒杀活动接口
 */
@Api(tags = "买家端,秒杀活动接口")
@RestController
@RequestMapping("/promotion/seckill")
public class SeckillBuyerController {

    /**
     * 秒杀活动
     */
    @Autowired
    private SeckillApplyService seckillApplyService;

    @ApiOperation(value = "获取当天秒杀活动信息")
    @GetMapping
    public List<SeckillTimelineDTO> getSeckillTime() {
        //获取当前时间(年月日 yyyy-mm-dd)
        String now = DateUtils.format(LocalDateTime.now(), "yyyy-MM-dd");
        List<SeckillTimelineDTO> seckillTimeline = seckillApplyService.getSeckillTimeline(now);
        return seckillTimeline;
    }

    @ApiOperation(value = "获取某个时刻的秒杀活动商品信息")
    @GetMapping("/{timeline}")
    public List<SeckillGoodsDTO> getSeckillGoods(@PathVariable Integer timeline) {
        List<SeckillGoodsDTO> seckillGoods = seckillApplyService.getSeckillGoods(timeline);
        return seckillGoods;
    }

}
