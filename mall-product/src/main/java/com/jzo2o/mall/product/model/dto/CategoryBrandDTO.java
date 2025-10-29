package com.jzo2o.mall.product.model.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 分类品牌DTO
 */
@Data
public class CategoryBrandDTO {
    /**
     * 品牌id
     */
    @ApiModelProperty(value = "品牌id", required = true)
    private String id;

    /**
     * 品牌名称
     */
    @ApiModelProperty(value = "品牌名称", required = true)
    private String name;
}
