package com.jzo2o.mall.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jzo2o.mall.order.model.domain.OrderPackage;
import com.jzo2o.mall.order.model.dto.OrderPackageDTO;

import java.util.List;

/**
 * 子订单业务层
 */
public interface OrderPackageService extends IService<OrderPackage> {


//    /**
//     * 根据订单编号获取订单包裹列表
//     * @param orderSn
//     * @return
//     */
//    List<OrderPackage> orderPackageList(String orderSn);

    /**
     * 获取指定订单编号的所有包裹
     * @param orderSn
     * @return
     */
    List<OrderPackageDTO> getOrderPackageList(String orderSn);
}