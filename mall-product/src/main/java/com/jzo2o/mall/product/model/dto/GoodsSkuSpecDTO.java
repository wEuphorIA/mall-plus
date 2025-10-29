package com.jzo2o.mall.product.model.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * 商品规格VO
 *
 * @author paulG
 * @since 2020-02-26 23:24:13
 */
@Data
public class GoodsSkuSpecDTO {


    @ApiModelProperty(value = "商品skuId")
    private String skuId;

    @ApiModelProperty(value = "商品sku所包含规格")
    private List<SpecValueDTO> specValues;

    @ApiModelProperty(value = "库存")
    private Integer quantity;

}
