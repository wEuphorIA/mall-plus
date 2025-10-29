package com.jzo2o.mall.security.model.enums;


import com.jzo2o.mall.common.enums.ClientTypeEnum;
import com.jzo2o.mall.common.enums.ConnectEnum;

/**
 * 联合登陆-渠道枚举
 */
public enum SourceEnum {

    WECHAT_PC_OPEN_ID("微信PC应用 openid登录"),
    WECHAT_OFFIACCOUNT_OPEN_ID("微信公众号 openid登录"),
    WECHAT_MP_OPEN_ID("微信小程序 openid登录"),
    WECHAT_APP_OPEN_ID("微信APP openid登录"),

    QQ_APP_OPEN_ID("QQ APP openid登录"),
    QQ_PC_OPEN_ID("QQ PC应用 openid登录"),
    QQ_H5_OPEN_ID("QQ H5应用 openid登录"),

    APPLE_OPEN_ID("苹果 openid登录"),
    ;


    private final String description;

    SourceEnum(String description) {
        this.description = description;
    }

    public static SourceEnum getSourceEnum(ConnectEnum source, ClientTypeEnum type) {
        switch (source) {
            case WECHAT:
                switch (type) {
                    case APP:
                        return WECHAT_APP_OPEN_ID;
                    case WECHAT_MP:
                        return WECHAT_MP_OPEN_ID;
                    case PC:
                        return WECHAT_PC_OPEN_ID;
                    case H5:
                        return WECHAT_OFFIACCOUNT_OPEN_ID;
                }
                break;
            case QQ:
                switch (type) {
                    case APP:
                        return QQ_APP_OPEN_ID;
                    case PC:
                        return QQ_PC_OPEN_ID;
                    case H5:
                        return QQ_H5_OPEN_ID;
                }
                break;
            case APPLE:
                return APPLE_OPEN_ID;
        }
        return null;
    }
}
