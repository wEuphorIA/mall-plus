package com.jzo2o.mall.cart.model.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 购物车基础
 */
@Data
public class CartBaseDTO implements Serializable {

    private static final long serialVersionUID = -5172752506920017597L;

    @ApiModelProperty(value = "卖家id")
    private String storeId;

    @ApiModelProperty(value = "卖家姓名")
    private String storeName;


    @ApiModelProperty(value = "此商品价格流水计算")
    private PriceDetailDTO priceDetailDTO;


    public CartBaseDTO() {
        priceDetailDTO = new PriceDetailDTO();
    }


}
