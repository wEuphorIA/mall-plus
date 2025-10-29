package com.jzo2o.mall.order.controller;

import com.jzo2o.mall.order.model.enums.PaymentClientEnum;
import com.jzo2o.mall.common.enums.PaymentMethodEnum;
import com.jzo2o.mall.common.exception.ServiceException;
import com.jzo2o.mall.payment.model.dto.CashierParam;
import com.jzo2o.mall.payment.model.dto.PayParam;
import com.jzo2o.mall.payment.service.OrderCashierService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 买家端,收银台接口
 */
@Slf4j
@RestController
@Api(tags = "买家端,收银台接口")
@RequestMapping("/payment/cashier")
public class OrderCashierController {

    @Autowired
    private OrderCashierService orderCashierService;


    @GetMapping(value = "/tradeDetail")
    @ApiOperation(value = "获取支付详情")
    public CashierParam paymentParams(@Validated PayParam payParam) {
        CashierParam cashierParam = orderCashierService.cashierParam(payParam);
        return cashierParam;
    }


    //目前仅支持微信小程序，测试时传入WECHAT、WECHAT_MP
    @ApiImplicitParams({
            @ApiImplicitParam(name = "paymentMethod", value = "支付方式", paramType = "path", allowableValues = "WECHAT,ALIPAY"),
            @ApiImplicitParam(name = "paymentClient", value = "调起方式", paramType = "path", allowableValues = "APP,NATIVE,JSAPI,H5,WECHAT_MP")
    })
    @GetMapping(value = "/pay/{paymentMethod}/{paymentClient}")
    @ApiOperation(value = "支付")
    public String payment(
            @PathVariable String paymentMethod,
            @PathVariable String paymentClient,
            @Validated PayParam payParam) {
        PaymentMethodEnum paymentMethodEnum = PaymentMethodEnum.valueOf(paymentMethod);
        PaymentClientEnum paymentClientEnum = PaymentClientEnum.valueOf(paymentClient);

        try {
            return orderCashierService.payment(paymentMethodEnum, paymentClientEnum, payParam);
        } catch (ServiceException se) {
            log.info("支付异常", se);
            throw se;
        } catch (Exception e) {
            log.error("收银台支付错误", e);
        }
        return null;

    }


    @ApiOperation(value = "查询支付结果")
    @GetMapping(value = "/result")
    public Boolean paymentResult(PayParam payParam) {
        Boolean aBoolean = orderCashierService.paymentResult(payParam);
        return aBoolean;
    }
}
