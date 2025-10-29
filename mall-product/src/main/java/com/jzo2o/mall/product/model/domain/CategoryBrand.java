package com.jzo2o.mall.product.model.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.jzo2o.mysql.domain.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * 分类品牌关联
 *
 */
@Data
@TableName("pms_category_brand")
@ApiModel(value = "商品分类品牌关联")
@NoArgsConstructor
public class CategoryBrand extends BaseEntity {

    private static final long serialVersionUID = 3315719881926878L;


    /**
     * 分类id
     */
    @TableField(value = "category_id")
    @ApiModelProperty(value = "分类id")
    private String categoryId;
    /**
     * 品牌id
     */
    @TableField(value = "brand_id")
    @ApiModelProperty(value = "品牌id")
    private String brandId;

    public CategoryBrand(String categoryId, String brandId) {
        this.categoryId = categoryId;
        this.brandId = brandId;
    }
}