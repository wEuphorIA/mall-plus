package com.jzo2o.mall.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jzo2o.mall.system.mapper.SettingMapper;
import com.jzo2o.mall.system.model.domain.Setting;
import com.jzo2o.mall.system.service.SettingService;
import org.springframework.stereotype.Service;

/**
 * 配置业务层实现
 */
@Service
public class SettingServiceImpl extends ServiceImpl<SettingMapper, Setting> implements SettingService {

    @Override
    public Setting get(String key) {
        return this.getById(key);
    }

    @Override
    public boolean saveUpdate(Setting setting) {
        return this.saveOrUpdate(setting);
    }
}