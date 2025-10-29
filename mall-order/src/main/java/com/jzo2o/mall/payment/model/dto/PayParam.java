package com.jzo2o.mall.payment.model.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

import javax.validation.constraints.NotNull;

/**
 * 支付参数
 */
@Data
@ToString
public class PayParam {


    @NotNull
    @ApiModelProperty(value = "交易类型", allowableValues = "TRADE,ORDER")
    private String orderType;

    @NotNull
    @ApiModelProperty(value = "订单号")
    private String sn;

    @NotNull
    @ApiModelProperty(value = "客户端类型(PC,H5,WECHAT_MP,APP)")
    private String clientType;



}
