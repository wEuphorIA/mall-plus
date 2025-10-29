package com.jzo2o.mall.aftersale.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jzo2o.mall.aftersale.mapper.AfterSaleReasonMapper;
import com.jzo2o.mall.aftersale.model.domain.AfterSaleReason;
import com.jzo2o.mall.aftersale.service.AfterSaleReasonService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 售后原因业务层实现
 */
@Service
public class AfterSaleReasonServiceImpl extends ServiceImpl<AfterSaleReasonMapper, AfterSaleReason> implements AfterSaleReasonService {


    @Override
    public List<AfterSaleReason> afterSaleReasonList(String serviceType) {
        LambdaQueryWrapper<AfterSaleReason> lambdaQueryWrapper = Wrappers.lambdaQuery();
        lambdaQueryWrapper.eq(AfterSaleReason::getServiceType, serviceType);
        return this.list(lambdaQueryWrapper);
    }

    @Override
    public AfterSaleReason editAfterSaleReason(AfterSaleReason afterSaleReason) {
        LambdaUpdateWrapper<AfterSaleReason> lambdaQueryWrapper = Wrappers.lambdaUpdate();
        lambdaQueryWrapper.eq(AfterSaleReason::getId, afterSaleReason.getId());
        lambdaQueryWrapper.set(AfterSaleReason::getReason, afterSaleReason.getReason());
        lambdaQueryWrapper.set(AfterSaleReason::getServiceType, afterSaleReason.getServiceType());
        this.update(lambdaQueryWrapper);
        return afterSaleReason;
    }
}