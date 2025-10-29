package com.jzo2o.mall.aftersale.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jzo2o.mall.aftersale.mapper.AfterSaleLogMapper;
import com.jzo2o.mall.aftersale.model.domain.AfterSaleLog;
import com.jzo2o.mall.aftersale.service.AfterSaleLogService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 订单日志业务层实现
 */
@Service
public class AfterSaleLogServiceImpl extends ServiceImpl<AfterSaleLogMapper, AfterSaleLog> implements AfterSaleLogService {

    @Override
    public List<AfterSaleLog> getAfterSaleLog(String sn) {
        QueryWrapper queryWrapper = Wrappers.query();
        queryWrapper.eq("sn", sn);
        return this.list(queryWrapper);
    }
}