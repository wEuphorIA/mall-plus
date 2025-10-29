package com.jzo2o.mall.common.enums;

/**
 * 订单状态枚举
 */
public enum PayStatusEnum {

    /**
     * 支付状态
     */
    UNPAID("待付款"),
    PAID("已付款"),
    CANCEL("已取消");

    private final String description;

    PayStatusEnum(String description) {
        this.description = description;
    }

    public String description() {
        return this.description;
    }


}
