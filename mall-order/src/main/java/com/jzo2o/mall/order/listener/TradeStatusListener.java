package com.jzo2o.mall.order.listener;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.jzo2o.common.constants.MqConstants;
import com.jzo2o.common.model.msg.TradeStatusMsg;
import com.jzo2o.mall.common.constants.MallMqConstants;
import com.jzo2o.mall.common.enums.PaymentMethodEnum;
import com.jzo2o.mall.order.model.domain.Order;
import com.jzo2o.mall.order.model.enums.TradingStateEnum;
import com.jzo2o.mall.order.constant.TradeConstants;
import com.jzo2o.mall.order.service.OrderService;
import com.jzo2o.mall.order.service.TradeService;
import com.jzo2o.mall.payment.service.OrderCashierService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 监听mq消息，接收支付结果更新订单状态
 *
 * @author itcast
 **/
@Slf4j
@Component
public class TradeStatusListener {
    @Resource
    private OrderCashierService orderCashierService;

    @Resource
    private OrderService orderService;

    /**
     * 接收支付结果：
     * 支付成功则更新订单状态为已支付
     *
     * @param msg 消息
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = MallMqConstants.Queues.ORDER_PAYMENT_STATUS_UPDATE_QUEUE),
            exchange = @Exchange(name = MqConstants.Exchanges.TRADE, type = ExchangeTypes.TOPIC),
            key = MqConstants.RoutingKeys.TRADE_UPDATE_STATUS
    ))
    public void listenTradePayStatusMsg(String msg) {
        log.info("接收到支付结果状态的消息 ({})-> {}", MallMqConstants.Queues.ORDER_PAYMENT_STATUS_UPDATE_QUEUE,  msg);
        List<TradeStatusMsg> tradeStatusMsgList = JSONUtil.toList(msg, TradeStatusMsg.class);
//        List<TradeStatusMsg> tradeStatusMsgList = JSONUtil.toBean(msg, new TypeReference<>() {
//        }, false);

        // 只处理支付成功的
        List<TradeStatusMsg> msgList = tradeStatusMsgList.stream().filter(v -> v.getStatusCode().equals(TradingStateEnum.YJS.getCode()) && TradeConstants.PRODUCT_APP_ID.equals(v.getProductAppId())).collect(Collectors.toList());
        if (CollUtil.isEmpty(msgList)) {
            return;
        }

        //修改订单状态
        msgList.forEach(m -> {
            // 支付系统的交易单号
            String tradingOrderNo = String.valueOf(m.getTradingOrderNo());
            //业务系统订单号(以T开头表示它是一个交易号)
            String productOrderNo = m.getProductOrderNo();
            //如果前缀以“T”开头表示是一个交易号，根据交易号获取交易下的所有订单
            if (productOrderNo.startsWith("T")) {
                List<Order> orders = orderService.getByTradeSn(productOrderNo);
                orders.forEach(order -> {
                    orderCashierService.paymentSuccess(Long.parseLong(order.getId()), PaymentMethodEnum.WECHAT.getPlugin(),tradingOrderNo);
                });
            }else{
                //更新订单状态
                orderCashierService.paymentSuccess(Long.parseLong(productOrderNo), PaymentMethodEnum.WECHAT.getPlugin(),tradingOrderNo);
            }

        });
    }
}
