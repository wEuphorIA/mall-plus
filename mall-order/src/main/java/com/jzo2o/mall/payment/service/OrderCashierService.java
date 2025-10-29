package com.jzo2o.mall.payment.service;


import com.jzo2o.mall.order.model.enums.PaymentClientEnum;
import com.jzo2o.mall.common.enums.PaymentMethodEnum;
import com.jzo2o.mall.payment.model.dto.CashierParam;
import com.jzo2o.mall.payment.model.dto.PayParam;

/**
 * 收银台接口
 */
public interface OrderCashierService {

    /**
     * 获取支付参数
     *
     * @param payParam 收银台支付参数
     * @return 收银台所需支付参数
     */
    CashierParam getPaymentParams(PayParam payParam);

    /**
     * 支付成功
     *
     * @param orderId 订单id
     * @param paymentMethod 支付方式
     * @param receivableNo 第三方交易号
     */
    void paymentSuccess(Long orderId,String paymentMethod,String receivableNo);
    /**
     * 支付结果查询
     *
     * @param payParam
     * @return
     */
    Boolean paymentResult(PayParam payParam);

    /**
     * 收银台参数
     *
     * @param payParam
     * @return
     */
    CashierParam cashierParam(PayParam payParam);

    /**
     * 支付
     *
     * @param payParam
     * @return
     */
    /**
     * 支付
     *
     * @param paymentMethodEnum 支付渠道枚举
     * @param paymentClientEnum 支付方式枚举
     * @return 支付消息
     */
    public String payment(PaymentMethodEnum paymentMethodEnum, PaymentClientEnum paymentClientEnum,
                          PayParam payParam);


}
