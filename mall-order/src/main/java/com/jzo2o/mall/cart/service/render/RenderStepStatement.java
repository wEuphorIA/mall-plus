package com.jzo2o.mall.cart.service.render;


import com.jzo2o.mall.cart.model.enums.RenderStepEnums;

/**
 * 价格渲染 步骤声明
 */
public class RenderStepStatement {

    /**
     * 购物车购物车渲染
     * 校验商品 》 满优惠渲染  》  渲染优惠  》计算价格
     */
    public static RenderStepEnums[] cartRender = {
            //检查商品的有效性并对购物车商品按店铺分组
            RenderStepEnums.CHECK_DATA,
            RenderStepEnums.BASEPRICE,
//            RenderStepEnums.SKU_PROMOTION,
//            RenderStepEnums.FULL_DISCOUNT,
            RenderStepEnums.COUPON,
            RenderStepEnums.CART_PRICE};

    /**
     * 结算页渲染
     * 过滤选择的商品 》 校验商品 》 满优惠渲染  》  渲染优惠  》
     * 优惠券渲染  》 计算运费  》  计算价格
     */
    public static RenderStepEnums[] checkedRender = {
            //过滤购物车中选择的商品
            RenderStepEnums.CHECKED_FILTER,
            //检查商品的有效性并对购物车商品按店铺分组
            RenderStepEnums.CHECK_DATA,
            RenderStepEnums.BASEPRICE,
//            RenderStepEnums.SKU_PROMOTION,
//            RenderStepEnums.FULL_DISCOUNT,
            RenderStepEnums.COUPON,
            RenderStepEnums.SKU_FREIGHT,
            RenderStepEnums.CART_PRICE,
    };


//    /**
//     * 单个商品优惠，不需要渲染满减优惠
//     * 用于特殊场景：例如积分商品，拼团商品，虚拟商品等等
//     */
//    public static RenderStepEnums[] checkedSingleRender = {
//            RenderStepEnums.CHECK_DATA,
//            RenderStepEnums.SKU_PROMOTION,
//            RenderStepEnums.COUPON,
//            RenderStepEnums.SKU_FREIGHT,
//            RenderStepEnums.CART_PRICE
//    };
//
//    /**
//     * 交易创建前渲染
//     * 渲染购物车 生成SN 》分销人员佣金渲染 》平台佣金渲染
//     */
//    public static RenderStepEnums[] singleTradeRender = {
//            RenderStepEnums.CHECK_DATA,
//            RenderStepEnums.SKU_PROMOTION,
//            RenderStepEnums.SKU_FREIGHT,
//            RenderStepEnums.CART_PRICE,
//            RenderStepEnums.CART_SN,
//            RenderStepEnums.DISTRIBUTION,
//            RenderStepEnums.PLATFORM_COMMISSION
//    };
//
//    /**
//     * 交易创建前渲染
//     * 渲染购物车 生成SN 》分销人员佣金渲染 》平台佣金渲染
//     */
//    public static RenderStepEnums[] pintuanTradeRender = {
//            RenderStepEnums.CHECK_DATA,
//            RenderStepEnums.SKU_PROMOTION,
//            RenderStepEnums.COUPON,
//            RenderStepEnums.SKU_FREIGHT,
//            RenderStepEnums.CART_PRICE,
//            RenderStepEnums.CART_SN,
//            RenderStepEnums.DISTRIBUTION,
//            RenderStepEnums.PLATFORM_COMMISSION
//    };

    /**
     * 交易创建前渲染
     * 渲染购物车 生成SN
     */
    public static RenderStepEnums[] tradeRender = {
            RenderStepEnums.CHECKED_FILTER,
            RenderStepEnums.CHECK_DATA,
            RenderStepEnums.BASEPRICE,
//            RenderStepEnums.SKU_PROMOTION,
//            RenderStepEnums.FULL_DISCOUNT,
            RenderStepEnums.COUPON,
            RenderStepEnums.SKU_FREIGHT,
            RenderStepEnums.CART_PRICE,
            RenderStepEnums.CART_SN
//            RenderStepEnums.DISTRIBUTION,
//            RenderStepEnums.PLATFORM_COMMISSION
    };
    /**
     * 秒杀交易创建前渲染
     * 渲染购物车 生成SN 》分销人员佣金渲染 》平台佣金渲染
     */
    public static RenderStepEnums[] seckillRender = {

            //过滤购物车中选择的商品
            RenderStepEnums.CHECKED_FILTER,
            //检查商品的有效性并对购物车商品按店铺分组
            RenderStepEnums.CHECK_DATA,
            RenderStepEnums.BASEPRICE,
//            RenderStepEnums.SKU_PROMOTION,
//            RenderStepEnums.FULL_DISCOUNT,
//            RenderStepEnums.COUPON,
            RenderStepEnums.SKU_FREIGHT,
            RenderStepEnums.CART_PRICE
    };
    /**
     * 秒杀交易创建前渲染
     * 渲染购物车 生成SN 》分销人员佣金渲染 》平台佣金渲染
     */
    public static RenderStepEnums[] seckillTradeRender = {

            //过滤购物车中选择的商品
            RenderStepEnums.CHECKED_FILTER,
            //检查商品的有效性并对购物车商品按店铺分组
            RenderStepEnums.CHECK_DATA,
            RenderStepEnums.BASEPRICE,
//            RenderStepEnums.SKU_PROMOTION,
//            RenderStepEnums.FULL_DISCOUNT,
//            RenderStepEnums.COUPON,
            RenderStepEnums.SKU_FREIGHT,
            RenderStepEnums.CART_PRICE,
            RenderStepEnums.CART_SN
    };
}
