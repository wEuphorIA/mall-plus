package com.jzo2o.mall.common.enums;

/**
 * 系统模块枚举类
 */
public enum ModuleEnums {

    ORDER("订单","order"),
    PROMOTION("促销","promotion"),
    PRODUCT("商品","product");
    private final String name;
    private final String code;

    ModuleEnums(String name,String code) {
        this.name = name;
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }
}
