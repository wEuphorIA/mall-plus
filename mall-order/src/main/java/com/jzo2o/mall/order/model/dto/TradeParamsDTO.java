package com.jzo2o.mall.order.model.dto;

import com.jzo2o.mall.cart.model.dto.StoreRemarkDTO;
import com.jzo2o.mall.common.enums.ClientTypeEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 交易参数
 **/
@Data
public class TradeParamsDTO implements Serializable {

    private static final long serialVersionUID = -8383072817737513063L;

    @ApiModelProperty(value = "购物车购买：CART/立即购买：BUY_NOW")
    private String way;

    /**
     * @see ClientTypeEnum
     */
    @ApiModelProperty(value = "客户端：H5/移动端 PC/PC端,WECHAT_MP/小程序端,APP/移动应用端")
    private String client;

    @ApiModelProperty(value = "店铺备注")
    private List<StoreRemarkDTO> remark;

    @ApiModelProperty(value = "是否为其他订单下的订单，如果是则为依赖订单的sn，否则为空")
    private String parentOrderSn;


}
