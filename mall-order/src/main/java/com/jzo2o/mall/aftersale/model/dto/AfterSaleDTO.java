package com.jzo2o.mall.aftersale.model.dto;

import com.jzo2o.mall.aftersale.model.domain.AfterSale;
import lombok.Data;

/**
 * 售后VO
 */
@Data
public class AfterSaleDTO extends AfterSale {
    /**
     * 初始化自身状态
     */
    public AfterSaleAllowOperationDTO getAfterSaleAllowOperationVO() {

        //设置订单的可操作状态
        return new AfterSaleAllowOperationDTO(this);
    }
}
