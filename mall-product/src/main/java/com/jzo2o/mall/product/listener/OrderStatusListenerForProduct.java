package com.jzo2o.mall.product.listener;

import cn.hutool.json.JSONUtil;
import com.jzo2o.mall.common.constants.MallMqConstants;
import com.jzo2o.mall.common.enums.ModuleEnums;
import com.jzo2o.mall.common.event.OrderStatusChangeEvent;
import com.jzo2o.mall.common.model.message.OrderStatusMessage;
import com.jzo2o.mall.product.event.StockUpdateExecute;
import com.jzo2o.mall.product.service.GoodsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 监听mq消息，接收订单状态变更消息
 *
 * @author itcast
 **/
@Slf4j
@Component
public class OrderStatusListenerForProduct {

//    @Autowired
//    private StockUpdateExecute stockUpdateExecute;

    @Autowired
    private List<OrderStatusChangeEvent> orderStatusChangeEvents;

    /**
     * 接收订单状态变更消息，更新库存
     * @param msg 消息
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = MallMqConstants.Queues.PRODUCT_STOCK_UPDATE_QUEUE),
            exchange = @Exchange(name = MallMqConstants.Exchanges.EXCHANGE_ORDER, type = ExchangeTypes.TOPIC),
            key = MallMqConstants.RoutingKeys.ORDER_STATUS_UPDATE_ROUTINGKEY
    ))
    public void stockUpdateExecute(String msg) {
        log.info("接收订单状态变更消息 ({})-> {}", MallMqConstants.Queues.PRODUCT_STOCK_UPDATE_QUEUE, msg);
        //解析消息
        OrderStatusMessage orderMessage = JSONUtil.toBean(msg, OrderStatusMessage.class);
        //执行订单变更事件
            try {
                orderStatusChangeEvents.forEach(orderStatusChangeEvent -> {
                    if (orderStatusChangeEvent.getModule().equals(ModuleEnums.PRODUCT)) {
                        orderStatusChangeEvent.orderChange(orderMessage);
                    }
                });
            } catch (Exception e) {
                log.error("订单{}状态变更后续操作执行异常",
                        orderMessage.getOrderDetail().getOrder().getSn(),
                        e);
                //向外抛出异常
                throw new RuntimeException(e);
            }


    }



}
