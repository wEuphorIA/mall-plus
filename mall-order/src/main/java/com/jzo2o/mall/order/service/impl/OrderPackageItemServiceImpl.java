package com.jzo2o.mall.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jzo2o.mall.order.mapper.OrderPackageItemMapper;
import com.jzo2o.mall.order.model.domain.OrderPackageItem;
import com.jzo2o.mall.order.service.OrderPackageItemService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 订单包裹业务层实现
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class OrderPackageItemServiceImpl extends ServiceImpl<OrderPackageItemMapper, OrderPackageItem> implements OrderPackageItemService {


    /**
     * 根据订单编号获取订单包裹列表
     * @param orderSn 订单编号
     * @return 子订单包裹列表
     */
    @Override
    public List<OrderPackageItem> getOrderPackageItemListByOrderSn(String orderSn) {
        return this.list(new LambdaQueryWrapper<OrderPackageItem>().eq(OrderPackageItem::getOrderSn, orderSn));
    }

    /**
     * 根据包裹编号获取子包裹列表
     * @param packageNo 包裹编号
     * @return 子包裹列表
     */
    @Override
    public List<OrderPackageItem> getOrderPackageItemListByPno(String packageNo) {
        return this.list(new LambdaQueryWrapper<OrderPackageItem>().eq(OrderPackageItem::getPackageNo, packageNo));
    }
}