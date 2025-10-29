package com.jzo2o.mall.cart.model.enums;

/**
 * 购物车类型
 *
 */
public enum CartTypeEnum {

    /**
     * 购物车
     */
    CART,
    /**
     * 秒杀商品
     */
    SECKILL,
    /**
     * 立即购买
     */
    BUY_NOW;

    public String getPrefix() {
        return "{" + this.name() + "}_";
    }

}
