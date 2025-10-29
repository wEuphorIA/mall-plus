package com.jzo2o.mall.product.listener;

import cn.hutool.json.JSONUtil;
import com.jzo2o.mall.common.constants.MallMqConstants;
import com.jzo2o.mall.common.enums.ModuleEnums;
import com.jzo2o.mall.common.event.OrderStatusChangeEvent;
import com.jzo2o.mall.common.event.ProductStatusChangeEvent;
import com.jzo2o.mall.common.model.message.OrderStatusMessage;
import com.jzo2o.mall.common.model.message.ProductStatusMessage;
import com.jzo2o.mall.product.event.ProductIndexUpdateExecute;
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
 * 监听mq消息，接收商品状态变更消息
 *
 * @author itcast
 **/
@Slf4j
@Component
public class ProductStatusListenerForProduct {


    @Autowired
    private ProductIndexUpdateExecute productIndexUpdateExecute;

    /**
     * 接收商品状态变更消息，更新索引
     * @param msg 消息
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = MallMqConstants.Queues.PRODUCT_INDEX_UPDATE_QUEUE),
            exchange = @Exchange(name = MallMqConstants.Exchanges.EXCHANGE_PRODUCT, type = ExchangeTypes.TOPIC),
            key = MallMqConstants.RoutingKeys.PRODUCT_STATUS_UPDATE_ROUTINGKEY
    ))
    public void listenProductStatusEvent(String msg) {
        log.info("接收商品状态变更消息 ({})-> {}", MallMqConstants.Queues.PRODUCT_INDEX_UPDATE_QUEUE, msg);
        //解析消息
        ProductStatusMessage productStatusMessage = JSONUtil.toBean(msg, ProductStatusMessage.class);
            try {
                productIndexUpdateExecute.onChange(productStatusMessage);
            } catch (Exception e) {
                log.error("商品{},更新索引时执行异常",
                        productStatusMessage.getGoodsId(),
                        e);
                //向外抛出异常
                throw new RuntimeException(e);
            }


    }



}
