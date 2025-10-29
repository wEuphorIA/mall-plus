package com.jzo2o.mall.system.model.dto.connect.dto;

import lombok.Data;

/**
 * 微信设置
 */
@Data
public class WechatConnectSettingItem {


    /**
     * @See ClientType
     */
    private String clientType;

    private String appId;

    private String appSecret;
}
