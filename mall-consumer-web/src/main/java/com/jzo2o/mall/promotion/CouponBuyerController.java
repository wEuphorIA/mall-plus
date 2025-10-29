package com.jzo2o.mall.promotion;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jzo2o.common.utils.UserContext;
import com.jzo2o.mall.common.enums.ResultUtil;
import com.jzo2o.mall.common.model.AuthUser;
import com.jzo2o.mall.common.model.ResultMessage;
import com.jzo2o.mall.promotion.model.domain.MemberCoupon;
import com.jzo2o.mall.promotion.model.dto.CouponDTO;
import com.jzo2o.mall.promotion.model.dto.CouponSearchParams;
import com.jzo2o.mall.promotion.model.dto.MemberCouponSearchParams;
import com.jzo2o.mall.promotion.model.enums.CouponGetEnum;
import com.jzo2o.mall.promotion.model.enums.PromotionsStatusEnum;
import com.jzo2o.mall.promotion.service.CouponService;
import com.jzo2o.mall.promotion.service.MemberCouponService;
import com.jzo2o.mysql.domain.PageVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Objects;

/**
 * 买家端,买家优惠券接口
 *
 */
@RestController
@Api(tags = "买家端,买家优惠券接口")
@RequestMapping("/promotion/coupon")
public class CouponBuyerController {

    /**
     * 优惠券
     */
    @Autowired
    private CouponService couponService;

    /**
     * 优惠券活动
     */
//    @Autowired
//    private CouponActivityService couponActivityService;

    /**
     * 会员优惠券
     */
    @Autowired
    private MemberCouponService memberCouponService;

//    @GetMapping("/activity")
//    @ApiOperation(value = "自动领取优惠券")
//    public ResultMessage<List<MemberCoupon>> activity() {
//        if (UserContext.getCurrentUser() == null) {
//            return ResultUtil.success();
//        }
//        return ResultUtil.data(couponActivityService.trigger(
//                CouponActivityTrigger.builder()
//                        .couponActivityTypeEnum(CouponActivityTypeEnum.AUTO_COUPON)
//                        .nickName(UserContext.getCurrentUser().getNickName())
//                        .userId(UserContext.getCurrentUser().getId())
//                        .build())
//        );
//    }

    @GetMapping
    @ApiOperation(value = "获取可领取优惠券列表")
    public IPage<CouponDTO> getCouponList(CouponSearchParams queryParam, PageVO page) {
        queryParam.setPromotionStatus(PromotionsStatusEnum.START.name());
        queryParam.setGetType(CouponGetEnum.FREE.name());
        IPage<CouponDTO> canUseCoupons = couponService.pageVOFindAll(queryParam, page);
        return canUseCoupons;
    }

    @ApiOperation(value = "获取当前会员的优惠券列表")
    @GetMapping("/getCoupons")
    public IPage<MemberCoupon> getCoupons(MemberCouponSearchParams param, PageVO pageVo) {
        AuthUser currentUser = Objects.requireNonNull(UserContext.getCurrentUser());
        param.setMemberId(currentUser.getIdString());
        IPage<MemberCoupon> memberCoupons = memberCouponService.getMemberCoupons(param, pageVo);
        return memberCoupons;
    }

    @ApiOperation(value = "获取当前会员的对于当前商品可使用的优惠券列表")
    @GetMapping("/canUse")
    public IPage<MemberCoupon> getCouponsByCanUse(MemberCouponSearchParams param, Double totalPrice, PageVO pageVo) {
        AuthUser currentUser = Objects.requireNonNull(UserContext.getCurrentUser());
        param.setMemberId(currentUser.getIdString());
        IPage<MemberCoupon> memberCouponsByCanUse = memberCouponService.getMemberCouponsByCanUse(param, totalPrice, pageVo);
        return memberCouponsByCanUse;
    }

    @ApiOperation(value = "获取当前会员可使用的优惠券数量")
    @GetMapping("/getCouponsNum")
    public Long getMemberCouponsNum() {
        long memberCouponsNum = memberCouponService.getMemberCouponsNum();
        return memberCouponsNum;
    }

    @ApiOperation(value = "会员领取优惠券")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "couponId", value = "优惠券ID", required = true, dataType = "Long", paramType = "path")
    })
    @GetMapping("/receive/{couponId}")
    public void receiveCoupon(@NotNull(message = "优惠券ID不能为空") @PathVariable("couponId") String couponId) {
        AuthUser currentUser = Objects.requireNonNull(UserContext.getCurrentUser());
        memberCouponService.receiveBuyerCoupon(couponId, currentUser.getIdString(), currentUser.getNickName());
    }

    @ApiOperation(value = "通过id获取")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "优惠券ID", required = true, dataType = "Long", paramType = "path")
    })
    @GetMapping(value = "/get/{id}")
    public MemberCoupon get(@NotNull(message = "优惠券ID不能为空") @PathVariable("id") String id) {
//        MemberCoupon memberCoupon = OperationalJudgment.judgment(memberCouponService.getById(id));
        MemberCoupon memberCoupon = memberCouponService.getById(id);
        return memberCoupon;
    }


}
