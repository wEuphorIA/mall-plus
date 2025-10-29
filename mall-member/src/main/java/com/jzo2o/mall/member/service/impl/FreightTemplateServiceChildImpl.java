package com.jzo2o.mall.member.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jzo2o.mall.member.mapper.FreightTemplateChildMapper;
import com.jzo2o.mall.member.model.domain.FreightTemplateChild;
import com.jzo2o.mall.member.service.FreightTemplateChildService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 配送子模板业务层实现
 */
@Service
public class FreightTemplateServiceChildImpl extends ServiceImpl<FreightTemplateChildMapper, FreightTemplateChild> implements FreightTemplateChildService {

    @Override
    public List<FreightTemplateChild> getFreightTemplateChild(String freightTemplateId) {
        LambdaQueryWrapper<FreightTemplateChild> lambdaQueryWrapper = Wrappers.lambdaQuery();
        lambdaQueryWrapper.eq(FreightTemplateChild::getFreightTemplateId, freightTemplateId);
        return this.baseMapper.selectList(lambdaQueryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addFreightTemplateChild(List<FreightTemplateChild> freightTemplateChildren) {
        return this.saveBatch(freightTemplateChildren);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeFreightTemplate(String freightTemplateId) {
        LambdaQueryWrapper<FreightTemplateChild> lambdaQueryWrapper = Wrappers.lambdaQuery();
        lambdaQueryWrapper.eq(FreightTemplateChild::getFreightTemplateId, freightTemplateId);
        return this.remove(lambdaQueryWrapper);
    }


}
