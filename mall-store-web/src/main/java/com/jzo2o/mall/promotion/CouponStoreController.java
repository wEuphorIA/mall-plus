package com.jzo2o.mall.promotion;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jzo2o.common.utils.UserContext;
import com.jzo2o.mall.common.enums.ResultCode;
import com.jzo2o.mall.common.enums.ResultUtil;
import com.jzo2o.mall.common.exception.ServiceException;
import com.jzo2o.mall.common.model.AuthUser;
import com.jzo2o.mall.common.model.ResultMessage;
import com.jzo2o.mall.promotion.model.domain.Coupon;
import com.jzo2o.mall.promotion.model.dto.CouponDTO;
import com.jzo2o.mall.promotion.model.dto.CouponSearchParams;
import com.jzo2o.mall.promotion.model.dto.MemberCouponExtDTO;
import com.jzo2o.mall.promotion.model.dto.MemberCouponSearchParams;
import com.jzo2o.mall.promotion.service.CouponService;
import com.jzo2o.mall.promotion.service.MemberCouponService;
import com.jzo2o.mysql.domain.PageVO;
import com.jzo2o.mysql.utils.PageUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 店铺端,优惠券接口
 **/
@RestController
@Api(tags = "店铺端,优惠券接口")
@RequestMapping("/promotion/coupon")
public class CouponStoreController {

    @Autowired
    private CouponService couponService;


    @Autowired
    private MemberCouponService memberCouponService;

    @GetMapping
    @ApiOperation(value = "获取优惠券列表")
    public IPage<CouponDTO> getCouponList(CouponSearchParams queryParam, PageVO page) {
        AuthUser authUser = UserContext.getCurrentUser();
        String storeId = Objects.requireNonNull(authUser).getStoreId();
        queryParam.setStoreId(storeId);
        IPage<CouponDTO> coupons = couponService.pageVOFindAll(queryParam, page);
        return coupons;
    }

    @ApiOperation(value = "获取优惠券详情")
    @GetMapping("/{couponId}")
    public CouponDTO getCouponList(@PathVariable String couponId) {
        CouponDTO coupon = couponService.getDetail(couponId);
        return coupon;
    }

    @ApiOperation(value = "添加优惠券")
    @PostMapping(consumes = "application/json", produces = "application/json")
    public CouponDTO addCoupon(@RequestBody CouponDTO couponDTO) {
        AuthUser currentUser = Objects.requireNonNull(UserContext.getCurrentUser());
        couponDTO.setStoreId(currentUser.getStoreId());
        couponDTO.setStoreName(currentUser.getStoreName());
        if (couponService.savePromotions(couponDTO)) {
            return couponDTO;
        }
        throw new ServiceException(ResultCode.COUPON_SAVE_ERROR);
    }

    @PutMapping(consumes = "application/json", produces = "application/json")
    @ApiOperation(value = "修改优惠券")
    public CouponDTO updateCoupon(@RequestBody CouponDTO couponDTO) {
        AuthUser currentUser = Objects.requireNonNull(UserContext.getCurrentUser());
        couponDTO.setStoreId(currentUser.getStoreId());
        couponDTO.setStoreName(currentUser.getStoreName());
        if (couponService.updatePromotions(couponDTO)) {
            return couponDTO;
        }
        throw new ServiceException(ResultCode.COUPON_SAVE_ERROR);
    }

    @DeleteMapping(value = "/{ids}")
    @ApiOperation(value = "批量删除")
    public void delAllByIds(@PathVariable List<String> ids) {
        AuthUser authUser = UserContext.getCurrentUser();
        String storeId = Objects.requireNonNull(authUser).getStoreId();
        LambdaQueryWrapper<Coupon> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Coupon::getId, ids);
        queryWrapper.eq(Coupon::getStoreId, storeId);
        List<Coupon> list = couponService.list(queryWrapper);
        List<String> filterIds = list.stream().map(Coupon::getId).collect(Collectors.toList());
        boolean b = couponService.removePromotions(filterIds);
        if(!b){
            throw new ServiceException(ResultCode.COUPON_DELETE_ERROR);
        }
    }

    @ApiOperation(value = "获取优惠券领取详情")
    @GetMapping(value = "/received")
    public IPage<MemberCouponExtDTO> getReceiveByPage(MemberCouponSearchParams searchParams,
                                                      PageVO page) {
        AuthUser authUser = UserContext.getCurrentUser();
        searchParams.setStoreId(Objects.requireNonNull(authUser).getStoreId());
        IPage<MemberCouponExtDTO> result = memberCouponService.getMemberCouponsPage(PageUtils.initPage(page), searchParams);
        return result;
    }

    @ApiOperation(value = "修改优惠券状态")
    @PutMapping("/status")
    public ResultMessage<Object> updateCouponStatus(String couponIds, Long startTime, Long endTime) {
        AuthUser currentUser = Objects.requireNonNull(UserContext.getCurrentUser());
        String[] split = couponIds.split(",");
        List<String> couponIdList = couponService.list(new LambdaQueryWrapper<Coupon>().in(Coupon::getId, Arrays.asList(split)).eq(Coupon::getStoreId, currentUser.getStoreId())).stream().map(Coupon::getId).collect(Collectors.toList());
        if (couponService.updateStatus(couponIdList, startTime, endTime)) {
            return ResultUtil.success(ResultCode.COUPON_EDIT_STATUS_SUCCESS);
        }
        throw new ServiceException(ResultCode.COUPON_EDIT_STATUS_ERROR);
    }
}
