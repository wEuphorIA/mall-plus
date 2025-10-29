package com.jzo2o.mall.payment.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.jzo2o.mall.common.enums.*;
import com.jzo2o.mall.common.exception.ServiceException;
import com.jzo2o.mall.order.constant.TradeConstants;
import com.jzo2o.mall.order.model.domain.Order;
import com.jzo2o.mall.order.model.domain.OrderItem;
import com.jzo2o.mall.order.model.domain.Trade;
import com.jzo2o.mall.order.model.dto.OrderDetailDTO;
import com.jzo2o.mall.order.model.enums.*;
import com.jzo2o.mall.order.service.OrderService;
import com.jzo2o.mall.order.service.TradeService;
import com.jzo2o.mall.payment.model.dto.*;
import com.jzo2o.mall.payment.service.OrderCashierService;
import com.jzo2o.mall.payment.service.PayDelegate;
import com.jzo2o.mall.system.enums.SettingEnum;
import com.jzo2o.mall.system.model.domain.Setting;
import com.jzo2o.mall.system.model.dto.BaseSetting;
import com.jzo2o.mall.system.model.dto.OrderSetting;
import com.jzo2o.mall.system.model.dto.payment.PaymentSupportSetting;
import com.jzo2o.mall.system.model.dto.payment.WechatPaymentSetting;
import com.jzo2o.mall.system.model.dto.payment.dto.PaymentSupportItem;
import com.jzo2o.mall.system.service.SettingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 订单支付信息获取
 */
@Slf4j
@Component
public class OrderCashierServiceImpl implements OrderCashierService {
    /**
     * 订单
     */
    @Autowired
    private OrderService orderService;

    @Autowired
    private PayDelegate payDelegate;
    /**
     * 设置
     */
    @Autowired
    private SettingService settingService;

    @Autowired
    private TradeService tradeService;


    @Override
    public CashierParam getPaymentParams(PayParam payParam) {
        if (payParam.getOrderType().equals(CashierEnum.TRADE.name())) {
            //准备返回的数据
            CashierParam cashierParam = new CashierParam();
            //订单信息获取
            Trade trade = tradeService.getBySn(payParam.getSn());

            List<Order> orders = orderService.getByTradeSn(payParam.getSn());


//            String orderSns = orders.stream().map(Order::getSn).collect(Collectors.joining(", "));
//            cashierParam.setOrderSns(orderSns);
            //对整个交易支付这里设置交易sn
            cashierParam.setOrderSns(trade.getSn());

            for (Order order : orders) {
//                //如果订单已支付，则不能发起支付
//                if (order.getPayStatus().equals(PayStatusEnum.PAID.name())) {
//                    throw new ServiceException(ResultCode.PAY_PARTIAL_ERROR);
//                }
                //如果订单状态不是待付款，则抛出异常
                if (!order.getOrderStatus().equals(OrderStatusEnum.UNPAID.name())) {
                    throw new ServiceException(ResultCode.PAY_BAN);
                }
            }


            cashierParam.setPrice(trade.getFlowPrice());

            cashierParam.setTitle("青橙商城在线支付");
            String subject = "在线支付";
            cashierParam.setDetail(subject);
            cashierParam.setCreateTime(trade.getCreateTime());
            return cashierParam;
        }else if (payParam.getOrderType().equals(CashierEnum.ORDER.name())) {
            //准备返回的数据
            CashierParam cashierParam = new CashierParam();
            //订单信息获取
            OrderDetailDTO order = orderService.queryDetail(payParam.getSn());
//
//            //如果订单已支付，则不能发起支付
//            if (order.getOrder().getPayStatus().equals(PayStatusEnum.PAID.name())) {
//                throw new ServiceException(ResultCode.PAY_DOUBLE_ERROR);
//            }
            //如果订单状态不是待付款，则抛出异常
            if (!order.getOrder().getOrderStatus().equals(OrderStatusEnum.UNPAID.name())) {
                throw new ServiceException(ResultCode.PAY_BAN);
            }
            cashierParam.setPrice(order.getOrder().getFlowPrice());
            cashierParam.setTitle("青橙商城在线支付");
            List<OrderItem> orderItemList = order.getOrderItems();
            StringBuilder subject = new StringBuilder();
            for (OrderItem orderItem : orderItemList) {
                subject.append(orderItem.getGoodsName()).append(";");
            }

            cashierParam.setDetail(subject.toString());

            cashierParam.setOrderSns(payParam.getSn());
            cashierParam.setCreateTime(order.getOrder().getCreateTime());
            return cashierParam;
        }

        return null;
    }
//    @Override
//    public CashierParam getPaymentParams(PayParam payParam) {
//        if (payParam.getOrderType().equals(CashierEnum.ORDER.name())) {
//            //准备返回的数据
//            CashierParam cashierParam = new CashierParam();
//            //订单信息获取
//            OrderDetailDTO order = orderService.queryDetail(payParam.getSn());
//
//            //如果订单已支付，则不能发器支付
//            if (order.getOrder().getPayStatus().equals(PayStatusEnum.PAID.name())) {
//                throw new ServiceException(ResultCode.PAY_DOUBLE_ERROR);
//            }
//            //如果订单状态不是待付款，则抛出异常
//            if (!order.getOrder().getOrderStatus().equals(OrderStatusEnum.UNPAID.name())) {
//                throw new ServiceException(ResultCode.PAY_BAN);
//            }
//            cashierParam.setPrice(order.getOrder().getFlowPrice());
//            cashierParam.setOrderId(Long.parseLong(order.getOrder().getId()));
//
//            try {
//                BaseSetting baseSetting = JSONUtil.toBean(settingService.get(SettingEnum.BASE_SETTING.name()).getSettingValue(), BaseSetting.class);
//                cashierParam.setTitle(baseSetting.getSiteName());
//            } catch (Exception e) {
//                cashierParam.setTitle("多用户商城，在线支付");
//            }
//
//
//            List<OrderItem> orderItemList = order.getOrderItems();
//            StringBuilder subject = new StringBuilder();
//            for (OrderItem orderItem : orderItemList) {
//                subject.append(orderItem.getGoodsName()).append(";");
//            }
//
//            cashierParam.setDetail(subject.toString());
//
//            cashierParam.setOrderSns(payParam.getSn());
//            cashierParam.setCreateTime(order.getOrder().getCreateTime());
//            return cashierParam;
//        }
//
//        return null;
//    }

    @Override
    public void paymentSuccess(Long orderId,String paymentMethod,String receivableNo) {

        //更新订单状态为PAID，并向mq发送订单状态变更消息,由consumer服务监听mq更新订单扣减库存，扣减库存成功则更新订单状态为待发货否则取消订单
        orderService.payOrder(orderId,
                paymentMethod,
                receivableNo);
        log.info("订单{}支付成功,金额{},方式{}",orderId,
                paymentMethod,
                receivableNo);
    }

    @Override
    public Boolean paymentResult(PayParam payParam) {
        //远程调用支付服务接口查询支付结果
        TradingResDTO tradingResDTO = payDelegate.findYjsTradByProductOrderNo(TradeConstants.PRODUCT_APP_ID,payParam.getSn());
        if (tradingResDTO != null) {
            //支付成功
            if (ObjectUtil.equals(tradingResDTO.getTradingState(), TradingStateEnum.YJS.getCode())) {
                if (payParam.getOrderType().equals(CashierEnum.ORDER.name())){
                    Order order = orderService.getBySn(payParam.getSn());
                    if(ObjectUtil.isNotNull(order)){
                        //更新支付状态-支付成功
                        paymentSuccess(Long.parseLong(order.getId()),tradingResDTO.getTradingChannel(),String.valueOf(tradingResDTO.getTradingOrderNo()));
                    }
                }else if(payParam.getOrderType().equals(CashierEnum.TRADE.name())){
                    List<Order> orders = orderService.getByTradeSn(payParam.getSn());
                    orders.forEach(order -> {
                        paymentSuccess(Long.parseLong(order.getId()), tradingResDTO.getTradingChannel(),String.valueOf(tradingResDTO.getTradingOrderNo()));
                    });
                }
                return true;
            }
        }
        return false;
    }

    /**
     * 支付
     *
     * @param paymentMethodEnum 支付渠道枚举
     * @param paymentClientEnum 支付方式枚举
     * @return 支付消息
     */
    public String payment(PaymentMethodEnum paymentMethodEnum, PaymentClientEnum paymentClientEnum,
                          PayParam payParam) {
        if (paymentClientEnum == null || paymentMethodEnum == null) {
            throw new ServiceException(ResultCode.PAY_NOT_SUPPORT);
        }
        //从ams_setting表获取PAYMENT_SUPPORT参数，判断是否支持此支付渠道
        if (!support(paymentClientEnum.name()).contains(paymentMethodEnum.name())) {
            throw new ServiceException(ResultCode.PAY_NOT_SUPPORT);
        }
        //获取支付参数
        CashierParam cashierParam = cashierParam(payParam);

//        WechatPaymentSetting wechatPaymentSetting = wechatPaymentSetting();
        //构建支付请求参数
        NativePayReqDTO nativePayReqDTO = new NativePayReqDTO();
        //商户订单号
        nativePayReqDTO.setProductOrderNo(cashierParam.getOrderSns());
        //支付渠道
        nativePayReqDTO.setTradingChannel(PayChannelEnum.valueOf(paymentMethodEnum.getPlugin()));
        //支付金额
        BigDecimal amount = new BigDecimal(cashierParam.getPrice()).setScale(2, RoundingMode.HALF_UP);
        nativePayReqDTO.setTradingAmount(amount);
        //如果是微信支付则取出微信商户id
        if (paymentMethodEnum.equals(PaymentMethodEnum.WECHAT)){
            Setting setting = settingService.get(SettingEnum.WECHAT_PAYMENT.name());
            WechatPaymentSetting wechatPaymentSetting = JSONUtil.toBean(setting.getSettingValue(), WechatPaymentSetting.class);
            nativePayReqDTO.setEnterpriseId(Long.parseLong(wechatPaymentSetting.getMchId()));
        }
        //商户id
//        nativePayReqDTO.setEnterpriseId(1561414331L);
        //业务系统标识
        nativePayReqDTO.setProductAppId(TradeConstants.PRODUCT_APP_ID);//指定支付来源是商城
        nativePayReqDTO.setMemo(cashierParam.getTitle());
        //不切换支付渠道
        nativePayReqDTO.setChangeChannel(false);

        //远程请求支付服务生成支付二维码,url=/trade/inner/native
        NativePayResDTO nativePayResDTO = payDelegate.createDownLineTrading(nativePayReqDTO);
        if(nativePayResDTO==null){
            return null;
        }
        if(payParam.getOrderType().equals(CashierEnum.ORDER.name())){
            //更新订单支付信息
            LambdaUpdateWrapper<Order> updateWrapper = Wrappers.<Order>lambdaUpdate()
                    .eq(Order::getSn,payParam.getSn())//订单号
                    .set(Order::getReceivableNo, String.valueOf(nativePayResDTO.getTradingOrderNo()))//第三方支付订单号
                    .set(Order::getPaymentMethod, nativePayResDTO.getTradingChannel());//支付渠道
            orderService.update(updateWrapper);

        }else{
            List<Order> orders = orderService.getByTradeSn(payParam.getSn());
            orders.forEach(order -> {
                LambdaUpdateWrapper<Order> updateWrapper = Wrappers.<Order>lambdaUpdate()
                        .eq(Order::getSn,order.getSn())//订单号
                        .set(Order::getReceivableNo, String.valueOf(nativePayResDTO.getTradingOrderNo()))//第三方支付订单号
                        .set(Order::getPaymentMethod, nativePayResDTO.getTradingChannel());//支付渠道
                orderService.update(updateWrapper);
            });
        }
        return nativePayResDTO.getQrCode();

    }

    /**
     * 获取微信支付配置
     *
     * @return
     */
    private WechatPaymentSetting wechatPaymentSetting() {
        try {
            Setting systemSetting = settingService.get(SettingEnum.WECHAT_PAYMENT.name());
            WechatPaymentSetting wechatPaymentSetting = JSONUtil.toBean(systemSetting.getSettingValue(), WechatPaymentSetting.class);
            return wechatPaymentSetting;
        } catch (Exception e) {
            log.error("微信支付暂不支持", e);
            throw new ServiceException(ResultCode.PAY_NOT_SUPPORT);
        }
    }

    /**
     * 支付 支持的支付方式
     *
     * @param client 客户端类型
     * @return 支持的支付方式
     */
    private List<String> support(String client) {

        ClientTypeEnum clientTypeEnum;
        try {
            clientTypeEnum = ClientTypeEnum.valueOf(client);
        } catch (IllegalArgumentException e) {
            throw new ServiceException(ResultCode.PAY_CLIENT_TYPE_ERROR);
        }
        //支付方式 循环获取
        Setting setting = settingService.get(SettingEnum.PAYMENT_SUPPORT.name());
        PaymentSupportSetting paymentSupportSetting = JSONUtil.toBean(setting.getSettingValue(), PaymentSupportSetting.class);
        for (PaymentSupportItem paymentSupportItem : paymentSupportSetting.getPaymentSupportItems()) {
            if (paymentSupportItem.getClient().equals(clientTypeEnum.name())) {
                return paymentSupportItem.getSupports();
            }
        }
        throw new ServiceException(ResultCode.PAY_NOT_SUPPORT);
    }
//
//    /**
//     * 支付回调
//     *
//     * @param paymentMethodEnum 支付渠道枚举
//     * @return 回调消息
//     */
//    public void callback(PaymentMethodEnum paymentMethodEnum,
//                         HttpServletRequest request) {
//
//        log.info("支付回调：支付类型：{}", paymentMethodEnum.name());
//
//        //获取支付插件
//        Payment payment = (Payment) SpringContextUtil.getBean(paymentMethodEnum.getPlugin());
//        payment.callBack(request);
//    }
//
//    /**
//     * 支付通知
//     *
//     * @param paymentMethodEnum 支付渠道
//     */
//    public void notify(PaymentMethodEnum paymentMethodEnum,
//                       HttpServletRequest request) {
//
//        log.info("支付异步通知：支付类型：{}", paymentMethodEnum.name());
//
//        //获取支付插件
//        Payment payment = (Payment) SpringContextUtil.getBean(paymentMethodEnum.getPlugin());
//        payment.notify(request);
//    }
//
//    /**
//     * 用户提现
//     *
//     * @param paymentMethodEnum   支付渠道
//     * @param memberWithdrawApply 用户提现申请
//     */
//    public TransferResultDTO transfer(PaymentMethodEnum paymentMethodEnum, MemberWithdrawApply memberWithdrawApply) {
//        Payment payment = (Payment) SpringContextUtil.getBean(paymentMethodEnum.getPlugin());
//        return payment.transfer(memberWithdrawApply);
//    }

    /**
     * 获取收银台参数
     *
     * @param payParam 支付请求参数
     * @return 收银台参数
     */
    public CashierParam cashierParam(PayParam payParam) {
        CashierParam cashierParam = getPaymentParams(payParam);
        //如果为空，则表示收银台参数初始化不匹配，继续匹配下一条
        if (cashierParam == null) {
            log.error("错误的支付请求:{}", payParam.toString());
            throw new ServiceException(ResultCode.PAY_CASHIER_ERROR);
        }
        //如果订单不需要付款，则抛出异常，直接返回
        //如果支付金额为零则自动更新支付状态为已支付
        if (cashierParam.getPrice() <= 0) {
            if (payParam.getOrderType().equals(CashierEnum.ORDER.name())){
                Order order = orderService.getBySn(payParam.getSn());
                if(ObjectUtil.isNotNull(order)){
                    //更新支付状态-支付成功
                    paymentSuccess(Long.parseLong(order.getId()),PaymentMethodEnum.WECHAT.name(), order.getReceivableNo());
                }
            }else if(payParam.getOrderType().equals(CashierEnum.TRADE.name())){
                List<Order> orders = orderService.getByTradeSn(payParam.getSn());
                orders.forEach(order -> {
                    paymentSuccess(Long.parseLong(order.getId()),PaymentMethodEnum.WECHAT.name(),order.getReceivableNo());
                });
            }
            throw new ServiceException(ResultCode.PAY_UN_WANTED);
        }

        cashierParam.setSupport(support(payParam.getClientType()));
//            cashierParam.setWalletValue(memberWalletService.getMemberWallet(UserContext.getCurrentUser().getId()).getMemberWallet());
        OrderSetting orderSetting = JSONUtil.toBean(settingService.get(SettingEnum.ORDER_SETTING.name()).getSettingValue(), OrderSetting.class);
        //订单未支付超时时间(分钟)
        Integer minute = orderSetting.getAutoCancel();
        //订单自动取消时间
        cashierParam.setAutoCancel(cashierParam.getCreateTime().toInstant(ZoneOffset.UTC).toEpochMilli() + minute * 1000 * 60);

        return cashierParam;

    }


}
