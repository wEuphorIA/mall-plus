package com.jzo2o.mall.common.site;

import com.jzo2o.mall.common.enums.ResultUtil;
import com.jzo2o.mall.common.model.ResultMessage;
import com.jzo2o.mall.system.enums.SettingEnum;
import com.jzo2o.mall.system.model.domain.Setting;
import com.jzo2o.mall.system.service.SettingService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 站点基础配置获取
 *
 */
@Slf4j
@RestController
@RequestMapping("/site")
@Api(tags = "站点基础接口")
public class SiteController {

    @Autowired
    private SettingService settingService;

    @ApiOperation(value = "获取站点基础信息")
    @GetMapping
    public Setting baseSetting() {
        Setting setting = settingService.get(SettingEnum.BASE_SETTING.name());
        return setting;
    }
}
