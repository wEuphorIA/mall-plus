package com.jzo2o.mall.common.file.service.plugin;

import cn.hutool.json.JSONUtil;
import com.jzo2o.mall.common.exception.ServiceException;
import com.jzo2o.mall.common.file.service.plugin.impl.AliFilePlugin;
import com.jzo2o.mall.common.file.service.plugin.impl.HuaweiFilePlugin;
import com.jzo2o.mall.common.file.service.plugin.impl.TencentFilePlugin;
import com.jzo2o.mall.system.enums.OssEnum;
import com.jzo2o.mall.system.enums.SettingEnum;
import com.jzo2o.mall.system.model.domain.Setting;
import com.jzo2o.mall.system.model.dto.OssSetting;
import com.jzo2o.mall.system.service.SettingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 文件服务抽象工厂 直接返回操作类
 */
@Component
public class FilePluginFactory {


    @Autowired
    private SettingService settingService;


    /**
     * 获取oss client
     *
     * @return
     */
    public FilePlugin filePlugin() {

        OssSetting ossSetting = null;
        try {
            Setting setting = settingService.get(SettingEnum.OSS_SETTING.name());

            ossSetting = JSONUtil.toBean(setting.getSettingValue(), OssSetting.class);


            switch (OssEnum.valueOf(ossSetting.getType())) {

                case ALI_OSS:
                    return new AliFilePlugin(ossSetting);
                case HUAWEI_OBS:
                    return new HuaweiFilePlugin(ossSetting);
                case TENCENT_COS:
                    return new TencentFilePlugin(ossSetting);
                default:
                    throw new ServiceException();
            }
        } catch (Exception e) {
            throw new ServiceException();
        }
    }


}
