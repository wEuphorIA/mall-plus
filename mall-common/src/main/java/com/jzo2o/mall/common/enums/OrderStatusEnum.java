package com.jzo2o.mall.common.enums;

/**
 * 订单状态枚举
 */
public enum OrderStatusEnum {

    /**
     * 订单状态
     */
    UNPAID("未付款"),
//    PAID("已付款"),
    UNDELIVERED("待发货"),
    DELIVERED("已发货"),
    COMPLETED("已完成"),
    STAY_PICKED_UP("待自提"),
    /**
     * 虚拟订单需要核验商品
     */
    TAKE("待核验"),
    CANCELLED("已关闭");

    private final String description;

    OrderStatusEnum(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public String description() {
        return this.description;
    }


}
