package com.jzo2o.mall.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jzo2o.mall.order.model.domain.OrderPackageItem;

import java.util.List;

/**
 * 子订单业务层
 */
public interface OrderPackageItemService extends IService<OrderPackageItem> {


    /**
     * 根据订单编号获取订单包裹列表
     * @param orderSn 订单编号
     * @return 子订单包裹列表
     */
    List<OrderPackageItem> getOrderPackageItemListByOrderSn(String orderSn);


    /**
     * 根据包裹编号获取子包裹列表
     * @param packageNo 包裹编号
     * @return 子包裹列表
     */
    List<OrderPackageItem> getOrderPackageItemListByPno(String packageNo);
}