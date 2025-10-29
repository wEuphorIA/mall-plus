package com.jzo2o.mall.aftersale.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jzo2o.mall.aftersale.model.domain.AfterSaleLog;

import java.util.List;

/**
 * 订单日志业务层
 */
public interface AfterSaleLogService extends IService<AfterSaleLog> {

    /**
     * 获取售后日志
     *
     * @param sn 售后编号
     * @return 售后日志列表
     */
    List<AfterSaleLog> getAfterSaleLog(String sn);
}