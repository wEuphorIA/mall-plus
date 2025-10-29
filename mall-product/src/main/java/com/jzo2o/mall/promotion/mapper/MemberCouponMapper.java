package com.jzo2o.mall.promotion.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jzo2o.mall.promotion.model.domain.MemberCoupon;
import com.jzo2o.mall.promotion.model.dto.MemberCouponExtDTO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 会员优惠券数据处理层
 */
public interface MemberCouponMapper extends BaseMapper<MemberCoupon> {

    @Select("SELECT mc.*,c.coupon_name FROM mks_member_coupon mc LEFT JOIN mks_coupon c ON mc.coupon_id = c.id ${ew.customSqlSegment}")
    Page<MemberCouponExtDTO> getMemberCoupons(Page<MemberCoupon> page, @Param(Constants.WRAPPER) Wrapper<MemberCouponExtDTO> queryWrapper);

}