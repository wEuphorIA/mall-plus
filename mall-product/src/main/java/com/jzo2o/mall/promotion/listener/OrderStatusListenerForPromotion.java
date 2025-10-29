package com.jzo2o.mall.promotion.listener;

import cn.hutool.json.JSONUtil;
import com.jzo2o.mall.common.constants.MallMqConstants;
import com.jzo2o.mall.common.enums.ModuleEnums;
import com.jzo2o.mall.common.event.OrderStatusChangeEvent;
import com.jzo2o.mall.common.model.message.OrderStatusMessage;
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
 * 监听mq消息，接收订单状态变更消息,处理优惠券状态
 *
 * @author itcast
 **/
@Slf4j
@Component
public class OrderStatusListenerForPromotion {


    @Autowired
    private List<OrderStatusChangeEvent> orderStatusChangeEvents;

    /**
     * 接收订单状态变更消息:
     * 取消订单进行退款
     * @param msg 消息
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = MallMqConstants.Queues.COUPON_REFUND_QUEUE),
            exchange = @Exchange(name = MallMqConstants.Exchanges.EXCHANGE_ORDER, type = ExchangeTypes.TOPIC),
            key = MallMqConstants.RoutingKeys.ORDER_STATUS_UPDATE_ROUTINGKEY
    ))
    public void listenOrderStatusEvent(String msg) {
        log.info("接收订单状态变更消息 ({})-> {}", MallMqConstants.Queues.COUPON_REFUND_QUEUE, msg);
        //解析消息
        OrderStatusMessage orderMessage = JSONUtil.toBean(msg, OrderStatusMessage.class);
        //执行订单变更事件
        for (OrderStatusChangeEvent orderStatusChangeEvent : orderStatusChangeEvents) {
            try {
                if(orderStatusChangeEvent.getModule().equals(ModuleEnums.PROMOTION)){
                    orderStatusChangeEvent.orderChange(orderMessage);
                }
            } catch (Exception e) {
                log.error("订单{},在{}业务中，状态修改事件执行异常",
                        e.getMessage(),
                        orderStatusChangeEvent.getClass().getName(),
                        e);
                //向外抛出异常
                throw new RuntimeException(e);
            }
        }


    }



}
