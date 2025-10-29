package com.jzo2o.mall.common.enums;

/**
 * token角色类型
 */
public enum UserEnums {
    /**
     * 角色
     */
    MEMBER("会员",new String[]{"/buyer","/common"}),
    STORE("商家",new String[]{"/store","/common"}),
    MANAGER("管理员",new String[]{"/manager","/common"}),
    SYSTEM("系统",new String[]{"/manager","/common"}),
    SEAT("坐席",new String[]{"/","/common"});
    private final String role;
    private final String[] urlPrefix;

    UserEnums(String role, String[] urlPrefix) {
        this.role = role;
        this.urlPrefix = urlPrefix;
    }

    public String getRole() {
        return role;
    }

    public String[] getUrlPrefix() {
        return urlPrefix;
    }
}
