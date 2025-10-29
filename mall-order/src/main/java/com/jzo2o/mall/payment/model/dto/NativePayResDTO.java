package com.jzo2o.mall.payment.model.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 扫码支付响应数据
 *
 * @author zzj
 * @version 1.0
 */
@Data
public class NativePayResDTO {

    @ApiModelProperty(value = "二维码base64数据")
    private String qrCode;
    @ApiModelProperty(value = "业务系统订单号")
    private String productOrderNo;
    @ApiModelProperty(value = "交易系统订单号【对于三方来说：商户订单】")
    private Long tradingOrderNo;
    @ApiModelProperty(value = "支付渠道【支付宝、微信、现金、免单挂账】")
    private String tradingChannel;
    @ApiModelProperty(value = "统一下单返回信息")
    private String placeOrderMsg;



}
