package com.jzo2o.mall.product.model.dto;

import com.jzo2o.mall.product.model.domain.Goods;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 商品VO
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class GoodsDTO extends Goods {

    private static final long serialVersionUID = 6377623919990713567L;

    @ApiModelProperty(value = "存储三级分类的名称")
    private List<String> categoryName;

    @ApiModelProperty(value = "商品参数")
    private List<GoodsParamsDTO> goodsParamsDTOList;

    @ApiModelProperty(value = "商品图片")
    private List<String> goodsGalleryList;

    @ApiModelProperty(value = "sku列表")
    private List<GoodsSkuDTO> skuList;

}
