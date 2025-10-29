package com.jzo2o.mall.promotion.model.dto;

import com.jzo2o.mall.promotion.model.domain.Coupon;
import com.jzo2o.mall.promotion.model.domain.PromotionGoods;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.beans.BeanUtils;

import java.util.List;

/**
 * 优惠券视图对象
 */
@EqualsAndHashCode(callSuper = true)
@Data
@ApiModel(value = "优惠券")
@ToString(callSuper = true)
@NoArgsConstructor
public class CouponDTO extends Coupon {

    private static final long serialVersionUID = 8372420376262437018L;

    /**
     * 促销关联的商品
     */
    @ApiModelProperty(value = "优惠券关联商品集合")
    private List<PromotionGoods> promotionGoodsList;

    public CouponDTO(Coupon coupon) {
        if (coupon == null) {
            return;
        }
        BeanUtils.copyProperties(coupon, this);
    }
}