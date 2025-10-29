package com.jzo2o.mall.promotion.event;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.jzo2o.mall.common.enums.ModuleEnums;
import com.jzo2o.mall.common.enums.OrderStatusEnum;
import com.jzo2o.mall.common.event.OrderStatusChangeEvent;
import com.jzo2o.mall.common.model.message.OrderStatusMessage;
import com.jzo2o.mall.promotion.model.enums.PromotionTypeEnum;
import com.jzo2o.mall.promotion.service.CouponService;
import com.jzo2o.mall.promotion.service.MemberCouponService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 订单状态修改操作进行退回优惠券:
 * 取消已付款的订单则需要退回优惠券
 **/
@Service
public class MemberCouponExecute implements OrderStatusChangeEvent {


    @Autowired
    private MemberCouponService memberCouponService;

    @Autowired
    private CouponService couponService;

    @Override
    public void orderChange(OrderStatusMessage orderMessage) {
        // 订单取消返还优惠券
        if (orderMessage.getNewStatus() == OrderStatusEnum.CANCELLED) {
            OrderStatusMessage.OrderDetail orderDetail = orderMessage.getOrderDetail();
            OrderStatusMessage.OrderDetail.Order order = orderDetail.getOrder();
            //退回优惠券
            this.refundCoupon(order);
        }
    }

    @Override
    public ModuleEnums getModule() {
        return ModuleEnums.PROMOTION;
    }


//    @Override
//    public void afterSaleStatusChange(AfterSale afterSale) {
//        // 售后完成返还优惠券
//        if (afterSale.getServiceStatus().equals(AfterSaleStatusEnum.COMPLETE.name())) {
//            this.refundCoupon(afterSale.getOrderSn());
//        }
//    }

    /**
     * 取消订单返还优惠券
     * @param order 订单
     */
    private void refundCoupon( OrderStatusMessage.OrderDetail.Order order) {
        if (CharSequenceUtil.isNotEmpty(order.getUseStoreMemberCouponIds())) {
            memberCouponService.recoveryMemberCoupon(ListUtil.toList(order.getUseStoreMemberCouponIds().split(",")));
        }
        if (CharSequenceUtil.isNotEmpty(order.getUsePlatformMemberCouponId())) {
            memberCouponService.recoveryMemberCoupon(ListUtil.toList(order.getUsePlatformMemberCouponId().split(",")));
        }

    }
}
