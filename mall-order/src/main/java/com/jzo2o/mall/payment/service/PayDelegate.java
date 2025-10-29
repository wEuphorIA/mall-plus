package com.jzo2o.mall.payment.service;

import com.jzo2o.mall.payment.model.dto.NativePayReqDTO;
import com.jzo2o.mall.payment.model.dto.NativePayResDTO;
import com.jzo2o.mall.payment.model.dto.RefundResultResDTO;
import com.jzo2o.mall.payment.model.dto.TradingResDTO;

import java.math.BigDecimal;

/**
 * @author Mr.M
 * @version 1.0
 * @description 支付代理接口
 * @date 2024/5/12 9:46
 */
public interface PayDelegate {

    /**
     * 生成支付二维码
     * @param nativePayDTO
     * @return
     */
    public NativePayResDTO createDownLineTrading(NativePayReqDTO nativePayDTO);

    /**
     * 退款接口
     * @param receivableNo 第三方支付流水号
     * @param refundNo 退款单号
     * @param refundAmount 退款金额
     * @return RefundResultResDTO退款结果
     */
    public RefundResultResDTO refund( String receivableNo,String refundNo, BigDecimal refundAmount);

    /**
     * 根据第三方支付流水号查询交易结果
     * @param tradingOrderNo
     * @return
     */
    public TradingResDTO findTradResultByTradingOrderNo(String tradingOrderNo);

    /**
     * 根据订单id查询已付款的交易单
     * @param productAppId 业务系统标识
     * @param productOrderNo 订单id
     * @return
     */
    public TradingResDTO findYjsTradByProductOrderNo(String productAppId,String productOrderNo);

}
