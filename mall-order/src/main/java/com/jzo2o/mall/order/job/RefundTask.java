package com.jzo2o.mall.order.job;

import com.jzo2o.common.utils.StringUtils;
import com.jzo2o.mall.aftersale.service.AfterSaleService;
import com.jzo2o.mall.common.enums.RefundStatusEnum;
import com.jzo2o.mall.order.model.domain.OrderItem;
import com.jzo2o.mall.order.service.OrderItemService;
import com.jzo2o.mall.payment.model.domain.RefundLog;
import com.jzo2o.mall.payment.model.dto.RefundResultResDTO;
import com.jzo2o.mall.payment.service.PayDelegate;
import com.jzo2o.mall.payment.service.RefundLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * @author Mr.M
 * @version 1.0
 * @description 退款任务
 * @date 2024/5/26 7:15
 */
@Component
@Slf4j
public class RefundTask {
    @Autowired
    private PayDelegate payDelegate;
    @Autowired
    private RefundLogService refundLogService;

    //注入售后单service
    @Autowired
    private AfterSaleService afterSaleService;

    @Autowired
    private OrderItemService orderItemService;

    /**
     * 使用 SpringTask实现每隔10分钟扫描退款日志表，查询近1个小时内退款日志且没有退款成功的记录
     */
    @Scheduled(cron = "0/10 * * * * ?")
    public void refundTask() {
        List<RefundLog> refundLogs = refundLogService.queryUnRefundLogs();
        int size = refundLogs.size();
//      log.info("定时查询退款日志，查询未退款记录{}",size);

        //遍历未退款记录，请求支付服务进行退款，如果退款成功则更新退款日志状态
        refundLogs.forEach(refundLog -> {
            //进行退款
            BigDecimal refundAmount = new BigDecimal(refundLog.getPayPrice()).setScale(2, RoundingMode.HALF_UP);
            RefundResultResDTO refundResult = payDelegate.refund(refundLog.getPaymentReceivableNo(),refundLog.getId(),refundAmount );
            //如果退款成功则更新退款日志
            if (refundResult!=null && refundResult.getRefundStatus().equals(RefundStatusEnum.SUCCESS.getCode())) {
//                refundLogService.updateRefundResult(refundLog.getPaymentReceivableNo(), refundResult.getRefundId(), true);
                refundLogService.updateRefundResult(refundLog.getId(), refundResult.getRefundId(), true);
                //更新售后单为已完成
                if(StringUtils.isNotBlank(refundLog.getAfterSaleNo())){
                    afterSaleService.completeAfterSale(refundLog.getAfterSaleNo());
                }
                String orderSn = refundLog.getOrderSn();
                //如果orderSn是以“OI”为前缀
                if (orderSn!=null && orderSn.startsWith("OI")) {
                    //则更新订单状态
                    //查询子订单
                    OrderItem orderItem = orderItemService.getBySn(orderSn);
                    if(orderItem!=null){
                        //更新退款状态为已退款
                        //如果退货数量和商品数量一致说明是全部退款
                        if (orderItem.getReturnGoodsNumber().equals(orderItem.getNum())) {
                            orderItem.setIsRefund(com.jzo2o.mall.order.model.enums.RefundStatusEnum.ALL_REFUND.name());
                        } else {
                            orderItem.setIsRefund(com.jzo2o.mall.order.model.enums.RefundStatusEnum.PART_REFUND.name());
                        }
                        orderItemService.updateById(orderItem);
                    }
                }

            }
        });
    }

}
