package com.jzo2o.mall.common.event;

import com.jzo2o.mall.common.enums.ModuleEnums;
import com.jzo2o.mall.common.model.message.OrderStatusMessage;
import com.jzo2o.mall.common.model.message.ProductStatusMessage;

/**
 * 商品状态改变事件
 *
 */
public interface ProductStatusChangeEvent {

    /**
     * 商品状态改变
     * @param productStatusMessage 商品状态变更消息
     */
    void onChange(ProductStatusMessage productStatusMessage);

    /**
     * 所属模块
     */
    ModuleEnums getModule();
}
