package com.jzo2o.mall.system.model.dto.connect;

import com.jzo2o.mall.system.model.dto.connect.dto.WechatConnectSettingItem;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 微信设置
 */
@Data
public class WechatConnectSetting {


    /**
     * 微信联合登陆配置
     */
    List<WechatConnectSettingItem> wechatConnectSettingItems = new ArrayList<>();

}
