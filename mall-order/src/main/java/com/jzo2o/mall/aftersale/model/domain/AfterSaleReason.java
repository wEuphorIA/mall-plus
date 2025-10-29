package com.jzo2o.mall.aftersale.model.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.jzo2o.mall.aftersale.model.enums.AfterSaleTypeEnum;
import com.jzo2o.mysql.domain.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 售后原因
 */
@Data
@TableName("oms_after_sale_reason")
@ApiModel(value = "售后原因")
public class AfterSaleReason extends BaseEntity {

    @NotNull
    @ApiModelProperty(value = "售后原因")
    private String reason;

    /**
     * @see AfterSaleTypeEnum
     */
    @ApiModelProperty(value = "原因类型", allowableValues = "CANCEL,RETURN_GOODS,RETURN_MONEY,COMPLAIN")
    @NotNull
    private String serviceType;

}
