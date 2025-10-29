package com.jzo2o.mall.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jzo2o.mall.cart.model.dto.TradeDTO;
import com.jzo2o.mall.order.model.domain.Trade;
import com.jzo2o.mall.order.model.dto.TradeParamsDTO;

/**
 * 交易业务层
 */
public interface TradeService extends IService<Trade> {

    /**
     * 创建交易
     * 1.获取购物车类型，不同的购物车类型有不同的订单逻辑
     * 购物车类型：购物车、立即购买、虚拟商品、拼团、积分
     * 2.校验用户的收件人信息
     * 3.设置交易的基础参数
     * 4.交易信息存储到缓存中
     * 5.创建交易
     * 6.清除购物车选择数据
     *
     * @param tradeParams 创建交易参数
     * @return 交易信息
     */
    TradeDTO createTrade(TradeParamsDTO tradeParams);

    /**
     * 提交秒杀订单
     * @param tradeParams
     * @return
     */
    TradeDTO createSeckillTrade(TradeParamsDTO tradeParams);
//    /**
//     * 创建交易
//     * 1.订单数据校验
//     * 2.积分预处理
//     * 3.优惠券预处理
//     * 4.添加交易
//     * 5.添加订单
//     * 6.将交易写入缓存供消费者调用
//     * 7.发送交易创建消息
//     *
//     * @param tradeDTO 购物车视图
//     * @return 交易
//     */
//    Trade createTrade(TradeDTO tradeDTO);

    /**
     * 获取交易详情
     *
     * @param sn 交易编号
     * @return 交易详情
     */
    Trade getBySn(String sn);

//    /**
//     * 整笔交易付款
//     *
//     * @param tradeSn      交易编号
//     * @param receivableNo 第三方流水号
//     * @param paymentName  支付方式
//     */
//    void payTrade(String tradeSn, String paymentName, String receivableNo);
//
    void updateTradePrice(String tradeSn);

}