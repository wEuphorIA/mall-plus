package com.jzo2o.mall.payment.service.impl;

import cn.hutool.core.map.MapUtil;
import cn.hutool.http.HttpException;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;
import com.jzo2o.common.utils.JsonUtils;
import com.jzo2o.mall.common.utils.HuHttpUtils;
import com.jzo2o.mall.payment.model.dto.NativePayReqDTO;
import com.jzo2o.mall.payment.model.dto.NativePayResDTO;
import com.jzo2o.mall.payment.model.dto.RefundResultResDTO;
import com.jzo2o.mall.payment.model.dto.TradingResDTO;
import com.jzo2o.mall.payment.properties.PayTradeProperties;
import com.jzo2o.mall.payment.service.PayDelegate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Mr.M
 * @version 1.0
 * @description TODO
 * @date 2024/5/12 9:50
 */
@Slf4j
@Service
public class PayDelegateJzo2o implements PayDelegate {

    @Autowired
    private PayTradeProperties payTradeProperties;

    @Override
    public NativePayResDTO createDownLineTrading(NativePayReqDTO nativePayDTO) {
        //远程请求支付服务生成支付二维码,url=/trade/inner/native
        NativePayResDTO nativePayResDTO = null;
        try {
            HuHttpUtils huHttpUtils = new HuHttpUtils();
            String url = payTradeProperties.getUrl()+"/inner/native";
            String body = JSONUtil.toJsonStr(nativePayDTO);
            HttpResponse response = huHttpUtils.post(url, body, null);
            if (!response.isOk()) {
                //支付失败
                log.error("远程调用支付服务生成支付二维码失败:"+response.body());
                return null;
            }
            nativePayResDTO = JsonUtils.toBean(response.body(), NativePayResDTO.class);
        } catch (HttpException e) {
           log.error("远程调用支付服务生成支付二维码失败");
        }
        return nativePayResDTO;
    }

    @Override
    public RefundResultResDTO refund( String receivableNo,String refundNo,  BigDecimal refundAmount) {
        //远程请求支付服务进行退款,url=/trade/inner/refund-record/refund
        //退款结果
        RefundResultResDTO refundResultResDTO = null;
        try {
            HuHttpUtils huHttpUtils = new HuHttpUtils();
            String url = payTradeProperties.getUrl()+"/inner/refund-record/refund";
            //请求参数
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("tradingOrderNo",receivableNo);
            paramMap.put("refundNo",refundNo);
            paramMap.put("refundAmount",refundAmount.toString());
//            String params=String.format("tradingOrderNo=%s&refundAmount=%s",receivableNo,refundAmount);
//            String response = huHttpUtils.post(url,params);
            HttpResponse response = huHttpUtils.post(url, paramMap, null);
            if (!response.isOk()) {
                //退款失败
                log.error("远程调用支付服务退款失败:"+response.body());
                return null;
            }
            refundResultResDTO = JsonUtils.toBean(response.body(), RefundResultResDTO.class);
        } catch (HttpException e) {
            log.error("远程调用支付服务退款失败");
        }
        return refundResultResDTO;

    }

    @Override
    public TradingResDTO findTradResultByTradingOrderNo(String tradingOrderNo) {

        //远程请求支付服务生成支付二维码,url=/trade/inner/tradings/findTradResultByTradingOrderNo
        TradingResDTO tradingResDTO = null;
        try {
            HuHttpUtils huHttpUtils = new HuHttpUtils();
            String url = payTradeProperties.getUrl()+"/inner/tradings/findTradResultByTradingOrderNo?tradingOrderNo="+tradingOrderNo;
            HttpResponse response = huHttpUtils.get(url, null,null);
            if (!response.isOk()) {
                //查询失败
                log.error("远程调用支付服务查询支付结果失败:"+response.body());
                return null;
            }
            tradingResDTO = JsonUtils.toBean(response.body(), TradingResDTO.class);
        } catch (HttpException e) {
            log.error("远程调用支付服务查询支付结果失败");
            e.printStackTrace();
        }
        return tradingResDTO;

    }
    @Override
    public TradingResDTO findYjsTradByProductOrderNo(String productAppId,String productOrderNo){
        //远程请求支付服务生成支付二维码,url=/trade/inner/tradings/findYjsTradByProductOrderNo
        TradingResDTO tradingResDTO = null;
        try {
            HuHttpUtils huHttpUtils = new HuHttpUtils();
            //请求参数
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("productAppId",productAppId);
            paramMap.put("productOrderNo",productOrderNo);
            String url = payTradeProperties.getUrl()+"/inner/tradings/findYjsTradByProductOrderNo";
            HttpResponse response = huHttpUtils.get(url, paramMap,null);
            if (!response.isOk()) {
                //查询失败
                log.error("远程调用支付服务查询支付结果失败:"+response.body());
                return null;
            }
            tradingResDTO = JsonUtils.toBean(response.body(), TradingResDTO.class);
        } catch (HttpException e) {
            log.error("远程调用支付服务查询支付结果失败");
            e.printStackTrace();
        }
        return tradingResDTO;
    }
}
