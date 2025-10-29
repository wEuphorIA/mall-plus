package com.jzo2o.mall.payment.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jzo2o.mall.payment.model.domain.RefundLog;

import java.util.List;

/**
 * 退款日志 业务层
 */
public interface RefundLogService extends IService<RefundLog> {
    /**
     * 根据售后sn查询退款日志
     * @param sn
     * @return
     */
    RefundLog queryByAfterSaleSn(String sn);

    /**
     * 根据支付第三方付款流水查询退款日志
     */
    RefundLog queryByPaymentReceivableNo(String paymentReceivableNo);

    /**
     * 查询待退款成功的记录：
     * 条件：查询近1个小时内退款日志且没有退款成功的记录
     */
    List<RefundLog> queryUnRefundLogs();


    /**
     * 添加退款日志
     */
    void addRefundLog(RefundLog refundLog);

    /**
     * 更新退款结果，包括退款id、是否退款成功
     */
    void updateRefundResult(String id, String refundId, boolean isRefund);
}
