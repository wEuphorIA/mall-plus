package com.jzo2o.mall.order.handler;

import com.jzo2o.common.utils.DateUtils;
import com.jzo2o.common.utils.IdUtils;
import com.jzo2o.common.utils.JsonUtils;
import com.jzo2o.common.utils.NumberUtils;
import com.jzo2o.mall.cart.model.dto.TradeDTO;
import com.jzo2o.mall.order.model.domain.Trade;
import com.jzo2o.mall.order.service.OrderService;
import com.jzo2o.mall.order.service.TradeService;
import com.jzo2o.redis.handler.SyncProcessHandler;
import com.jzo2o.redis.model.SyncMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;


/**
 * 秒杀成功同步任务
 */
@Component("SECKILL:SYNC")
@Slf4j
public class SeckillSyncProcessHandler implements SyncProcessHandler<Object> {

    @Resource
    private TradeService tradeService;

    @Resource
    private OrderService orderService;


    @Override
    public void batchProcess(List<SyncMessage<Object>> multiData) {
        throw new RuntimeException("不支持批量处理");
    }

    /**
     * signleData key activityId, value 抢单用户id
     * @param singleData
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void singleProcess(SyncMessage<Object> singleData) {
        log.info("秒杀结果同步开始 id ： {}",singleData.getKey());
        TradeDTO tradeDTO = (TradeDTO) singleData.getValue();
        String userId = singleData.getKey().split("_")[0];
        if (tradeDTO == null) {
            return;
        }

        // 2.新增交易信息及订单信息
        Trade trade = new Trade(tradeDTO);
        //添加交易
        tradeService.save(trade);
        //添加订单
        orderService.intoDB(tradeDTO);

       }
}
