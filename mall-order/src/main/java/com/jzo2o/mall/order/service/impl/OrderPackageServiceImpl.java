package com.jzo2o.mall.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jzo2o.mall.common.enums.ResultCode;
import com.jzo2o.mall.common.exception.ServiceException;
import com.jzo2o.mall.order.mapper.OrderPackageMapper;
import com.jzo2o.mall.order.model.domain.OrderPackage;
import com.jzo2o.mall.order.model.domain.OrderPackageItem;
import com.jzo2o.mall.order.model.dto.OrderPackageDTO;
import com.jzo2o.mall.order.service.LogisticsService;
import com.jzo2o.mall.order.service.OrderPackageItemService;
import com.jzo2o.mall.order.service.OrderPackageService;
import com.jzo2o.mall.system.model.dto.TracesDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

/**
 * 订单包裹业务层实现
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class OrderPackageServiceImpl extends ServiceImpl<OrderPackageMapper, OrderPackage> implements OrderPackageService {

    @Autowired
    private OrderPackageItemService orderpackageItemService;

    @Autowired
    private LogisticsService logisticsService;

//    @Override
//    public List<OrderPackage> orderPackageList(String orderSn) {
//        return this.list(new LambdaQueryWrapper<OrderPackage>().eq(OrderPackage::getOrderSn, orderSn));
//    }

    @Override
    public List<OrderPackageDTO> getOrderPackageList(String orderSn) {
        List<OrderPackage> orderPackages = this.list(new LambdaQueryWrapper<OrderPackage>().eq(OrderPackage::getOrderSn, orderSn));
//        List<OrderPackage> orderPackages = this.orderPackageList(orderSn);
        if (orderPackages == null){
            throw new ServiceException(ResultCode.ORDER_PACKAGE_NOT_EXIST);
        }
        List<OrderPackageDTO> orderPackageVOS = new ArrayList<>();
        orderPackages.forEach(orderPackage -> {
            OrderPackageDTO orderPackageDTO = new OrderPackageDTO(orderPackage);
            // 获取子订单包裹详情
            List<OrderPackageItem> orderPackageItemList = orderpackageItemService.getOrderPackageItemListByPno(orderPackage.getPackageNo());
            orderPackageDTO.setOrderPackageItemList(orderPackageItemList);
            String str = orderPackage.getConsigneeMobile();
            str = str.substring(str.length() - 4);
            TracesDTO traces = logisticsService.getLogisticTrack(orderPackage.getLogisticsCode(), orderPackage.getLogisticsNo(), str);
            orderPackageDTO.setTraces(traces);
            orderPackageVOS.add(orderPackageDTO);
        });

        return orderPackageVOS;
    }


}