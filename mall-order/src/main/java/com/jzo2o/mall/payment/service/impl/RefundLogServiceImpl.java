package com.jzo2o.mall.payment.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jzo2o.mall.order.model.domain.OrderItem;
import com.jzo2o.mall.order.model.enums.RefundStatusEnum;
import com.jzo2o.mall.order.service.OrderItemService;
import com.jzo2o.mall.payment.mapper.RefundLogMapper;
import com.jzo2o.mall.payment.model.domain.RefundLog;
import com.jzo2o.mall.payment.service.PayDelegate;
import com.jzo2o.mall.payment.service.RefundLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 退款日志 业务实现
 */
@Service
public class RefundLogServiceImpl extends ServiceImpl<RefundLogMapper, RefundLog> implements RefundLogService {

    @Autowired
    private OrderItemService orderItemService;

    @Override
    public RefundLog queryByAfterSaleSn(String sn) {
        return this.getOne(new LambdaUpdateWrapper<RefundLog>().eq(RefundLog::getAfterSaleNo, sn));
    }

    @Override
    public RefundLog queryByPaymentReceivableNo(String paymentReceivableNo) {
        if (paymentReceivableNo != null) {
            return this.getOne(new LambdaUpdateWrapper<RefundLog>().eq(RefundLog::getPaymentReceivableNo, paymentReceivableNo));
        }
        return null;
    }

    @Override
    public List<RefundLog> queryUnRefundLogs() {
        //查询近1个小时内退款日志且没有退款成功的记录，一资查询100条
        LambdaUpdateWrapper<RefundLog> refundLogLambdaUpdateWrapper = new LambdaUpdateWrapper<RefundLog>().eq(RefundLog::getIsRefund, false)
                .gt(RefundLog::getCreateTime, LocalDateTime.now().minusHours(1))
                .last("limit 100");
        List<RefundLog> list = this.list(refundLogLambdaUpdateWrapper);
        return list;
    }

    @Override
    public void addRefundLog(RefundLog refundLog) {
//        //先根据第三方付款编号查询退款日志
//        RefundLog one = queryByPaymentReceivableNo(refundLog.getPaymentReceivableNo());
//        //如果有则更新，没有则新增
//        if (one == null) {
//            this.save(refundLog);
//        }
        RefundLog one = null;
        if (refundLog.getId() != null) {
            //先根据id查询退款日志
            one = this.getById(refundLog.getId());
        }
        if (one == null) {
            this.save(refundLog);
        }
    }

    @Override
    public void updateRefundResult(String id, String refundId, boolean isRefund) {
        //先根据第三方付款编号查询退款日志
//        RefundLog one = queryByPaymentReceivableNo(paymentReceivableNo);
        RefundLog one = this.getById(id);
        if (one != null) {
            one.setOutOrderNo(refundId);
            one.setIsRefund(isRefund);
            this.updateById(one);

        }
    }
}