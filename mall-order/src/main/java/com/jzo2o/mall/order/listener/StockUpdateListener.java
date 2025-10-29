package com.jzo2o.mall.order.listener;

import cn.hutool.json.JSONUtil;
import com.jzo2o.common.model.msg.TradeStatusMsg;
import com.jzo2o.common.utils.BeanUtils;
import com.jzo2o.mall.common.constants.MallMqConstants;
import com.jzo2o.mall.common.model.message.StockUpdateMessage;
import com.jzo2o.mall.order.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 监听mq消息，接收库存更新消息
 *
 * @author itcast
 **/
@Slf4j
@Component
public class StockUpdateListener {

    @Autowired
    private OrderService orderService;

    /**
     * 接收库存更新消息：
     * 正常扣减库存则更新订单状态为待发货
     * 扣减库存失败则更新订单状态为已取消
     *
     * @param msg 消息
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = MallMqConstants.Queues.ORDER_CONFIRM_QUEUE),
            exchange = @Exchange(name = MallMqConstants.Exchanges.EXCHANGE_ORDER, type = ExchangeTypes.TOPIC),
            key = MallMqConstants.RoutingKeys.PRODUCT_STOCK_UPDATE_ROUTINGKEY
    ))
    public void listenStockUpdateMsg(String msg) {
        log.info("接收到支付结果状态的消息 ({})-> {}", MallMqConstants.Queues.ORDER_CONFIRM_QUEUE,  msg);
        //解析消息
        StockUpdateMessage stockUpdateMessage = JSONUtil.toBean(msg, StockUpdateMessage.class);

        //是否回滚库存
        Boolean isRollback = stockUpdateMessage.getIsRollback();
        if(isRollback){
            //更新订单状态为已取消
            orderService.cancel(stockUpdateMessage.getOrderSn(), "库存不足");
        }
//        if(!isRollback){
//            //更新订单状态为待发货
//            orderService.normalOrderConfirm(stockUpdateMessage.getOrderSn());
//        }else{
//            //更新订单状态为已取消
//            orderService.cancel(stockUpdateMessage.getOrderSn(), "库存不足");
//        }

    }
}
