package com.jzo2o.mall.promotion.model.dto;

import cn.hutool.core.bean.BeanUtil;
import com.jzo2o.mall.promotion.model.domain.MemberCoupon;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * MemberCoupon扩展信息
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class MemberCouponExtDTO extends MemberCoupon {

    private static final long serialVersionUID = -5533168813075444962L;

    @ApiModelProperty(value = "优惠券名称")
    private String couponName;

    @ApiModelProperty(value = "无法使用原因")
    private String reason;

    public MemberCouponExtDTO(MemberCoupon memberCoupon, String reason) {
        BeanUtil.copyProperties(memberCoupon, this);
        this.reason = reason;
    }

}
