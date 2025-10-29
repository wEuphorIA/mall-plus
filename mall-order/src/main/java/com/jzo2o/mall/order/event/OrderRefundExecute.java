package com.jzo2o.mall.order.event;

import cn.hutool.core.convert.Convert;
import com.jzo2o.mall.common.enums.*;
import com.jzo2o.mall.common.event.OrderStatusChangeEvent;
import com.jzo2o.mall.common.model.message.OrderStatusMessage;
import com.jzo2o.mall.common.utils.SnowFlake;
import com.jzo2o.mall.common.utils.SpringContextUtil;
import com.jzo2o.mall.order.model.domain.Order;
import com.jzo2o.mall.order.service.OrderService;
import com.jzo2o.mall.payment.model.domain.RefundLog;
import com.jzo2o.mall.payment.model.dto.RefundResultResDTO;
import com.jzo2o.mall.payment.service.PayDelegate;
import com.jzo2o.mall.payment.service.RefundLogService;
import com.jzo2o.mall.product.model.domain.GoodsSku;
import com.jzo2o.mall.product.service.GoodsSkuService;
import com.jzo2o.redis.helper.Cache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 订单状态修改操作进行退款:
 * 取消已付款的订单则需要进行退款
 */
@Slf4j
@Service
public class OrderRefundExecute implements OrderStatusChangeEvent {

    @Autowired
    private PayDelegate payDelegate;

    @Autowired
    private RefundLogService refundLogService;

    @Autowired
    private OrderRefundExecute orderRefundExecute;

    /**
     * 保存退款日志
     * @param orderMessage 订单消息
     */
    @Transactional(rollbackFor = Exception.class)
    public RefundLog saveRefundLog(OrderStatusMessage orderMessage,boolean isRefund) {

        if (orderMessage.getNewStatus() == OrderStatusEnum.CANCELLED && OrderStatusEnum.UNDELIVERED == orderMessage.getOldStatus()) {
            OrderStatusMessage.OrderDetail orderDetail = orderMessage.getOrderDetail();
            OrderStatusMessage.OrderDetail.Order order = orderDetail.getOrder();

            //如果未付款，则不去要退回相关代码执行
            if (order.getPayStatus().equals(PayStatusEnum.UNPAID.name())) {
                return null;
            }
            //添加退款日志
            RefundLog refundLog = RefundLog.builder()
                    .isRefund(isRefund)
                    .totalAmount(order.getFlowPrice())
                    .payPrice(order.getFlowPrice())
                    .memberId(order.getMemberId())
                    .paymentName(order.getPaymentMethod())
                    .afterSaleNo("订单取消")
                    .orderSn(order.getSn())
                    .paymentReceivableNo(order.getReceivableNo())
//                    .outOrderNo("AF" + SnowFlake.getIdStr())
                    .refundReason("订单取消")
                    .build();
            //取消订单退款是针对整个订单，退款日志id为订单id
            refundLog.setId(order.getId());
            refundLogService.addRefundLog(refundLog);
            return refundLog;
        }
        return null;
    }

    @Override
    public void orderChange(OrderStatusMessage orderMessage) {
        //待发货状态下取消订单进行退款
        if (orderMessage.getNewStatus() == OrderStatusEnum.CANCELLED && OrderStatusEnum.UNDELIVERED == orderMessage.getOldStatus()) {
            OrderStatusMessage.OrderDetail orderDetail = orderMessage.getOrderDetail();
            OrderStatusMessage.OrderDetail.Order order = orderDetail.getOrder();
            //如果订单价格为0则不走退款流程直接保证退款日志且已退款
            if (order.getFlowPrice() == 0) {
                //保存退款日志，已退款
                orderRefundExecute.saveRefundLog(orderMessage,true);
                return;
            }

            //保存退款日志
            RefundLog refundLog = orderRefundExecute.saveRefundLog(orderMessage,false);

            //启动一个新线程进行退款
           new Thread(() -> {
               //进行退款
               BigDecimal refundAmount = new BigDecimal(order.getFlowPrice()).setScale(2, RoundingMode.HALF_UP);
                //远程调用退款接口
               RefundResultResDTO refundResult = payDelegate.refund(order.getReceivableNo(),refundLog.getId(), refundAmount);
               //如果退款成功则更新退款日志
               if (refundResult.getRefundStatus().equals(RefundStatusEnum.SUCCESS.getCode())) {
                   refundLogService.updateRefundResult(String.valueOf(refundResult.getRefundNo()), refundResult.getRefundId(), true);
//                   refundLogService.updateRefundResult(order.getReceivableNo(), refundResult.getRefundId(), true);
               }
           }).start();

        }

    }

    @Override
    public ModuleEnums getModule() {
        return ModuleEnums.ORDER;
    }

}
