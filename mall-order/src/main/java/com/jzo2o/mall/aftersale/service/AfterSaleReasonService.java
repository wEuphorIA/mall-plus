package com.jzo2o.mall.aftersale.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jzo2o.mall.aftersale.model.domain.AfterSaleReason;

import java.util.List;

/**
 * 售后原因业务层
 */
public interface AfterSaleReasonService extends IService<AfterSaleReason> {

    /**
     * 获取售后原因列表
     * @param serviceType
     * @return
     */
    List<AfterSaleReason> afterSaleReasonList(String serviceType);


    /**
     * 修改售后原因
     * @param afterSaleReason 售后原因
     * @return 售后原因
     */
    AfterSaleReason editAfterSaleReason(AfterSaleReason afterSaleReason);

}