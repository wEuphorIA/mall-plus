package com.jzo2o.mall.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jzo2o.mall.member.model.domain.FreightTemplateChild;

import java.util.List;

/**
 * 配送子模板业务层
 */
public interface FreightTemplateChildService extends IService<FreightTemplateChild> {

    /**
     * 获取当前商家的运费模板子内容列表
     *
     * @param freightTemplateId 运费模板ID
     * @return 运费模板子内容列表
     */
    List<FreightTemplateChild> getFreightTemplateChild(String freightTemplateId);

    /**
     * 添加商家运费模板
     *
     * @param freightTemplateChildren 子模板信息
     * @return 运费模板
     */
    boolean addFreightTemplateChild(List<FreightTemplateChild> freightTemplateChildren);


    /**
     * 删除商家运费模板
     *
     * @param freightTemplateId 运费模板ID
     * @return 操作状态
     */
    boolean removeFreightTemplate(String freightTemplateId);

}