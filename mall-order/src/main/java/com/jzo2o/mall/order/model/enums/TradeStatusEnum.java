package com.jzo2o.mall.order.model.enums;

/**
 * 交易状态枚举
 */
public enum TradeStatusEnum {

    /**
     * 交易状态
     */
    UNPAID("未付款"),
    PAID("已付款"),
    CANCELLED("已取消");

    private final String description;

    TradeStatusEnum(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public String description() {
        return this.description;
    }


}
