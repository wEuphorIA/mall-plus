package com.jzo2o.mall.promotion.model.dto;

import com.jzo2o.mall.promotion.model.enums.PromotionTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 促销skuVO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PromotionSkuDTO implements Serializable {

    private static final long serialVersionUID = -8587010496940375179L;

    /**
     * 促销类型
     * @see PromotionTypeEnum
     */
    private String promotionType;

    /**
     * 促销活动
     */
    private String activityId;

}
