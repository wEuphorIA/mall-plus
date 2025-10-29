package com.jzo2o.mall.common.event;

import com.jzo2o.mall.common.enums.ModuleEnums;
import com.jzo2o.mall.common.model.message.OrderStatusMessage;

/**
 * 订单状态改变事件
 *
 */
public interface OrderStatusChangeEvent {

    /**
     * 订单改变
     * @param orderMessage 订单消息
     */
    void orderChange(OrderStatusMessage orderMessage);

    /**
     * 所属模块
     */
    ModuleEnums getModule();
}
