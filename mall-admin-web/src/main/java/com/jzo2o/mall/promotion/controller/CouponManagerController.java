package com.jzo2o.mall.promotion.controller;

import cn.hutool.core.util.PageUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jzo2o.common.utils.UserContext;
import com.jzo2o.mall.cart.model.dto.MemberCouponDTO;
import com.jzo2o.mall.common.enums.ResultCode;
import com.jzo2o.mall.common.enums.ResultUtil;
import com.jzo2o.mall.common.exception.ServiceException;
import com.jzo2o.mall.common.model.AuthUser;
import com.jzo2o.mall.common.model.ResultMessage;
import com.jzo2o.mall.promotion.model.domain.Coupon;
import com.jzo2o.mall.promotion.model.domain.MemberCoupon;
import com.jzo2o.mall.promotion.model.dto.CouponDTO;
import com.jzo2o.mall.promotion.model.dto.CouponSearchParams;
import com.jzo2o.mall.promotion.model.dto.MemberCouponExtDTO;
import com.jzo2o.mall.promotion.model.dto.MemberCouponSearchParams;
import com.jzo2o.mall.promotion.service.CouponService;
import com.jzo2o.mall.promotion.service.MemberCouponService;
import com.jzo2o.mall.promotion.service.PromotionTools;
import com.jzo2o.mysql.domain.PageVO;
import com.jzo2o.mysql.utils.PageUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * 管理端,优惠券接口
 **/
@RestController
@Api(tags = "管理端,优惠券接口")
@RequestMapping("/promotion/coupon")
public class CouponManagerController {
    @Autowired
    private CouponService couponService;
    @Autowired
    private MemberCouponService memberCouponService;

    @ApiOperation(value = "获取优惠券列表")
    @GetMapping
    public IPage<CouponDTO> getCouponList(CouponSearchParams queryParam, PageVO page) {
        if (queryParam.getStoreId() == null) {
            queryParam.setStoreId(PromotionTools.PLATFORM_ID);
        }
        IPage<CouponDTO> couponDTOIPage = couponService.pageVOFindAll(queryParam, page);
        return couponDTOIPage;
    }

    @ApiOperation(value = "获取优惠券详情")
    @GetMapping("/{couponId}")
    public CouponDTO getCoupon(@PathVariable String couponId) {
        CouponDTO coupon = couponService.getDetail(couponId);
        return coupon;
    }

    @ApiOperation(value = "添加优惠券")
    @PostMapping(consumes = "application/json", produces = "application/json")
    public CouponDTO addCoupon(@RequestBody CouponDTO coupon) {
        this.setStoreInfo(coupon);
        boolean b = couponService.savePromotions(coupon);
        return coupon;
    }

    @ApiOperation(value = "修改优惠券")
    @PutMapping(consumes = "application/json", produces = "application/json")
    public Coupon updateCoupon(@RequestBody CouponDTO couponDTO) {
        this.setStoreInfo(couponDTO);
        Coupon coupon = couponService.getById(couponDTO.getId());
        couponService.updatePromotions(couponDTO);
        return coupon;
    }

    @ApiOperation(value = "修改优惠券状态")
    @PutMapping("/status")
    public void updateCouponStatus(String couponIds, Long startTime, Long endTime) {
        String[] split = couponIds.split(",");
        boolean b = couponService.updateStatus(Arrays.asList(split), startTime, endTime);
        if (!b) {
            throw new ServiceException(ResultCode.COUPON_EDIT_STATUS_ERROR);
        }

    }

    @ApiOperation(value = "批量删除")
    @DeleteMapping(value = "/{ids}")
    public void delAllByIds(@PathVariable List<String> ids) {
        couponService.removePromotions(ids);
    }

    @ApiOperation(value = "会员优惠券作废")
    @PutMapping(value = "/member/cancellation/{id}")
    public void cancellation(@PathVariable String id) {
        AuthUser currentUser =  Objects.requireNonNull(UserContext.getCurrentUser());
        memberCouponService.cancellation(currentUser.getIdString(), id);
    }

    @ApiOperation(value = "根据优惠券id券分页获取会员领详情")
    @GetMapping(value = "/member/{id}")
    public IPage<MemberCoupon> getByPage(@PathVariable String id,
                                                        PageVO page) {
        QueryWrapper<MemberCoupon> queryWrapper = new QueryWrapper<>();
        IPage<MemberCoupon> data = memberCouponService.page(PageUtils.initPage(page),
                queryWrapper.eq("coupon_id", id)
        );
        return data;

    }

    @ApiOperation(value = "获取优惠券领取详情")
    @GetMapping(value = "/received")
    public IPage<MemberCouponExtDTO> getReceiveByPage(MemberCouponSearchParams searchParams,
                                                                  PageVO page) {
        Page<MemberCouponExtDTO> memberCouponsPage = memberCouponService.getMemberCouponsPage(PageUtils.initPage(page), searchParams);
        return memberCouponsPage;
    }

    private void setStoreInfo(CouponDTO coupon) {
        AuthUser currentUser = UserContext.getCurrentUser();
        if (currentUser == null) {
            throw new ServiceException(ResultCode.USER_NOT_EXIST);
        }
        coupon.setStoreId(PromotionTools.PLATFORM_ID);
        coupon.setStoreName(PromotionTools.PLATFORM_NAME);
    }

}
