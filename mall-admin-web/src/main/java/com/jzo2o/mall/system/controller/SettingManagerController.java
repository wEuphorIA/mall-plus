package com.jzo2o.mall.system.controller;

import cn.hutool.json.JSONUtil;
import com.jzo2o.mall.common.enums.ResultCode;
import com.jzo2o.mall.common.enums.ResultUtil;
import com.jzo2o.mall.common.exception.ServiceException;
import com.jzo2o.mall.common.model.ResultMessage;
import com.jzo2o.mall.member.model.dto.LogisticsSetting;
import com.jzo2o.mall.product.model.dto.GoodsSetting;
import com.jzo2o.mall.system.enums.SettingEnum;
import com.jzo2o.mall.system.model.domain.Setting;
import com.jzo2o.mall.system.model.dto.*;
import com.jzo2o.mall.system.model.dto.connect.QQConnectSetting;
import com.jzo2o.mall.system.model.dto.connect.WechatConnectSetting;
import com.jzo2o.mall.system.model.dto.payment.AlipayPaymentSetting;
import com.jzo2o.mall.system.model.dto.payment.PaymentSupportSetting;
import com.jzo2o.mall.system.model.dto.payment.UnionPaymentSetting;
import com.jzo2o.mall.system.model.dto.payment.WechatPaymentSetting;
import com.jzo2o.mall.system.model.dto.payment.dto.PaymentSupportForm;
import com.jzo2o.mall.system.service.SettingService;
import com.jzo2o.redis.helper.Cache;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;

/**
 * 管理端,系统设置接口
 */
@RestController
@Api(tags = "管理端,系统设置接口")
@RequestMapping("/setting/setting")
public class SettingManagerController {
    @Autowired
    private SettingService settingService;
    /**
     * 缓存
     */
    @Autowired
    private Cache<String> cache;


    @ApiOperation(value = "更新配置")
    @PutMapping(value = "/put/{key}")
    @ApiImplicitParam(name = "key", value = "配置key", paramType = "path",
            allowableValues = "BASE_SETTING,EMAIL_SETTING,GOODS_SETTING,KUAIDI_SETTING,ORDER_SETTING,OSS_SETTING,POINT_SETTING," +
                    "WECHAT_PC_CONNECT,WECHAT_WAP_CONNECT,WECHAT_APP_CONNECT,WECHAT_MP_CONNECT," +
                    "QQ_WEB_CONNECT,QQ_APP_CONNECT," +
                    "QQ_WEB_CONNECT,QQ_APP_CONNECT,WEIBO_CONNECT,ALIPAY_CONNECT," +
                    "PAYMENT_SUPPORT,ALIPAY_PAYMENT,WECHAT_PAYMENT,SECKILL_SETTING,EXPERIENCE_SETTING,IM")
    public void saveConfig(@PathVariable String key, @RequestBody String configValue) {
        SettingEnum settingEnum = SettingEnum.valueOf(key);
        //获取系统配置
        Setting setting = settingService.getById(settingEnum.name());
        if (setting == null) {
            setting = new Setting();
            setting.setId(settingEnum.name());
        }
        //特殊配置过滤
        configValue = filter(settingEnum, configValue);

        setting.setSettingValue(configValue);
        settingService.saveUpdate(setting);
    }


    @ApiOperation(value = "查看配置")
    @GetMapping(value = "/get/{key}")
    @ApiImplicitParam(name = "key", value = "配置key", paramType = "path"
            , allowableValues = "BASE_SETTING,EMAIL_SETTING,GOODS_SETTING,KUAIDI_SETTING,ORDER_SETTING,OSS_SETTING,POINT_SETTING," +
            "WECHAT_PC_CONNECT,WECHAT_WAP_CONNECT,WECHAT_APP_CONNECT,WECHAT_MP_CONNECT," +
            "QQ_WEB_CONNECT,QQ_APP_CONNECT," +
            "QQ_WEB_CONNECT,QQ_APP_CONNECT,WEIBO_CONNECT,ALIPAY_CONNECT," +
            "PAYMENT_SUPPORT,ALIPAY_PAYMENT,WECHAT_PAYMENT,SECKILL_SETTING,EXPERIENCE_SETTING,IM"
    )
    public Object settingGet(@PathVariable String key) {
        Object setting = createSetting(key);
        return setting;
    }


    /**
     * 对配置进行过滤
     *
     * @param settingEnum
     * @param configValue
     */
    private String filter(SettingEnum settingEnum, String configValue) {
        if (settingEnum.equals(SettingEnum.POINT_SETTING)) {
            PointSetting pointSetting = JSONUtil.toBean(configValue, PointSetting.class);
            if (pointSetting.getPointSettingItems() != null && pointSetting.getPointSettingItems().size() > 0) {
                Collections.sort(pointSetting.getPointSettingItems());
                if (pointSetting.getPointSettingItems().size() > 4) {
                    pointSetting.setPointSettingItems(pointSetting.getPointSettingItems().subList(0, 4));
                }
            }
            configValue = JSONUtil.toJsonStr(pointSetting);
        }
        return configValue;
    }

    @ApiOperation(value = "支持支付方式表单")
    @GetMapping("/paymentSupport")
    public PaymentSupportForm paymentForm() {
        return new PaymentSupportForm();
    }

    /**
     * 获取表单
     * 这里主要包含一个配置对象为空，导致转换异常问题的处理，解决配置项增加减少，带来的系统异常，无法直接配置
     *
     * @param key
     * @return
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    private Object createSetting(String key) {
        SettingEnum settingEnum = SettingEnum.valueOf(key);
        cache.remove(key);
        Setting setting = settingService.get(key);
        switch (settingEnum) {
            case BASE_SETTING:
                return setting == null ?
                        new BaseSetting() :
                        JSONUtil.toBean(setting.getSettingValue(), BaseSetting.class);
            case WITHDRAWAL_SETTING:
                return setting == null ?
                        new WithdrawalSetting() :
                        JSONUtil.toBean(setting.getSettingValue(), WithdrawalSetting.class);
            case DISTRIBUTION_SETTING:
                return setting == null ?
                        new DistributionSetting() :
                        JSONUtil.toBean(setting.getSettingValue(), DistributionSetting.class);
            case EMAIL_SETTING:
                return setting == null ?
                        new EmailSetting() :
                        JSONUtil.toBean(setting.getSettingValue(), EmailSetting.class);
            case GOODS_SETTING:
                return setting == null ?
                        new GoodsSetting() :
                        JSONUtil.toBean(setting.getSettingValue(), GoodsSetting.class);
            case LOGISTICS_SETTING:
                return setting == null ?
                        new LogisticsSetting() :
                        JSONUtil.toBean(setting.getSettingValue(), LogisticsSetting.class);
            case ORDER_SETTING:
                return setting == null ?
                        new OrderSetting() :
                        JSONUtil.toBean(setting.getSettingValue(), OrderSetting.class);
            case OSS_SETTING:
                return setting == null ?
                        new OssSetting() :
                        JSONUtil.toBean(setting.getSettingValue(), OssSetting.class);
            case SMS_SETTING:
                return setting == null ?
                        new SmsSetting() :
                        JSONUtil.toBean(setting.getSettingValue(), SmsSetting.class);
            case POINT_SETTING:
                return setting == null ?
                        new PointSetting() :
                        JSONUtil.toBean(setting.getSettingValue(), PointSetting.class);
            case QQ_CONNECT:
                return setting == null ?
                        new QQConnectSetting() :
                        JSONUtil.toBean(setting.getSettingValue(), QQConnectSetting.class);
            case PAYMENT_SUPPORT:
                return setting == null ?
                        new PaymentSupportSetting(new PaymentSupportForm()) :
                        JSONUtil.toBean(setting.getSettingValue(), PaymentSupportSetting.class);
            case ALIPAY_PAYMENT:
                return setting == null ?
                        new AlipayPaymentSetting() :
                        JSONUtil.toBean(setting.getSettingValue(), AlipayPaymentSetting.class);
            case UNIONPAY_PAYMENT:
                return setting == null ?
                        new UnionPaymentSetting() :
                        JSONUtil.toBean(setting.getSettingValue(), UnionPaymentSetting.class);
            case WECHAT_CONNECT:
                return setting == null ?
                        new WechatConnectSetting() :
                        JSONUtil.toBean(setting.getSettingValue(), WechatConnectSetting.class);
            case WECHAT_PAYMENT:
                return setting == null ?
                        new WechatPaymentSetting() :
                        JSONUtil.toBean(setting.getSettingValue(), WechatPaymentSetting.class);
            case SECKILL_SETTING:
                return setting == null ?
                        new SeckillSetting() :
                        JSONUtil.toBean(setting.getSettingValue(), SeckillSetting.class);
            case EXPERIENCE_SETTING:
                return setting == null ?
                        new ExperienceSetting():
                        JSONUtil.toBean(setting.getSettingValue(), ExperienceSetting.class);
            case IM_SETTING:
                return setting == null ?
                        new ImSetting() :
                        JSONUtil.toBean(setting.getSettingValue(), ImSetting.class);
            case HOT_WORDS:
                return setting == null ?
                        new HotWordsSetting() :
                        JSONUtil.toBean(setting.getSettingValue(), HotWordsSetting.class);
            default:
                throw new ServiceException(ResultCode.SETTING_NOT_TO_SET);
        }
    }
}
