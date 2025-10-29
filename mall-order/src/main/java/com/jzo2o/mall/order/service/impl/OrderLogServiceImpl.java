package com.jzo2o.mall.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jzo2o.mall.order.mapper.OrderLogMapper;
import com.jzo2o.mall.order.model.domain.OrderLog;
import com.jzo2o.mall.order.service.OrderLogService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 订单日志业务层实现
 *
 */
@Service
public class OrderLogServiceImpl extends ServiceImpl<OrderLogMapper, OrderLog> implements OrderLogService {

    @Override
    public List<OrderLog> getOrderLog(String orderSn) {
        LambdaQueryWrapper<OrderLog> lambdaQueryWrapper = Wrappers.lambdaQuery();
        lambdaQueryWrapper.eq(OrderLog::getOrderSn, orderSn);
        return this.list(lambdaQueryWrapper);
    }
}