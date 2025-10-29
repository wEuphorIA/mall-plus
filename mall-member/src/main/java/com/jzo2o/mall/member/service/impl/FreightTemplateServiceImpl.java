package com.jzo2o.mall.member.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.PageUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jzo2o.common.model.CurrentUserInfo;
import com.jzo2o.common.utils.UserContext;
import com.jzo2o.mall.common.cache.CachePrefix;
import com.jzo2o.mall.common.enums.ResultCode;
import com.jzo2o.mall.common.exception.ServiceException;
import com.jzo2o.mall.common.model.AuthUser;
import com.jzo2o.mall.member.mapper.FreightTemplateMapper;
import com.jzo2o.mall.member.model.domain.FreightTemplate;
import com.jzo2o.mall.member.model.domain.FreightTemplateChild;
import com.jzo2o.mall.member.model.dto.FreightTemplateDTO;
import com.jzo2o.mall.member.service.FreightTemplateChildService;
import com.jzo2o.mall.member.service.FreightTemplateService;
import com.jzo2o.mysql.domain.PageVO;
import com.jzo2o.mysql.utils.PageUtils;
import com.jzo2o.redis.helper.Cache;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 店铺运费模板业务层实现
 */
@Service
public class FreightTemplateServiceImpl extends ServiceImpl<FreightTemplateMapper, FreightTemplate> implements FreightTemplateService {
    /**
     * 配送子模板
     */
    @Autowired
    private FreightTemplateChildService freightTemplateChildService;
    /**
     * 缓存
     */
    @Autowired
    private Cache cache;


    @Override
    public List<FreightTemplateDTO> getFreightTemplateList(String storeId) {
        //先从缓存中获取运费模板，如果有则直接返回，如果没有则查询数据后再返回
        List<FreightTemplateDTO> list = (List<FreightTemplateDTO>) cache.get(CachePrefix.SHIP_TEMPLATE.getPrefix() + storeId);
        if (list != null) {
            return list;
        }
        list = new ArrayList<>();
        //查询运费模板
        LambdaQueryWrapper<FreightTemplate> lambdaQueryWrapper = Wrappers.lambdaQuery();
        lambdaQueryWrapper.eq(FreightTemplate::getStoreId, storeId);
        List<FreightTemplate> freightTemplates = this.baseMapper.selectList(lambdaQueryWrapper);
        if (!freightTemplates.isEmpty()) {
            //如果模板不为空则查询子模板信息
            for (FreightTemplate freightTemplate : freightTemplates) {
                FreightTemplateDTO freightTemplateVO = new FreightTemplateDTO();
                BeanUtil.copyProperties(freightTemplate, freightTemplateVO);
                List<FreightTemplateChild> freightTemplateChildren = freightTemplateChildService.getFreightTemplateChild(freightTemplate.getId());
                if (!freightTemplateChildren.isEmpty()) {
                    freightTemplateVO.setFreightTemplateChildList(freightTemplateChildren);
                }
                list.add(freightTemplateVO);
            }
        }
        cache.put(CachePrefix.SHIP_TEMPLATE.getPrefix() + storeId, list);
        return list;

    }

    @Override
    public IPage<FreightTemplate> getFreightTemplate(PageVO pageVo) {
        LambdaQueryWrapper<FreightTemplate> lambdaQueryWrapper = Wrappers.lambdaQuery();
        AuthUser tokenUser =  UserContext.getCurrentUser();
        lambdaQueryWrapper.eq(FreightTemplate::getStoreId, tokenUser.getStoreId());
        return this.baseMapper.selectPage(PageUtils.initPage(pageVo), lambdaQueryWrapper);
    }

    @Override
    public FreightTemplateDTO getFreightTemplate(String id) {
        FreightTemplateDTO freightTemplateVO = new FreightTemplateDTO();
        //获取运费模板
        FreightTemplate freightTemplate = this.getById(id);
        if (freightTemplate != null) {
            //复制属性
            BeanUtils.copyProperties(freightTemplate, freightTemplateVO);
            //填写运费模板子内容
            List<FreightTemplateChild> freightTemplateChildList = freightTemplateChildService.getFreightTemplateChild(id);
            freightTemplateVO.setFreightTemplateChildList(freightTemplateChildList);
        }
        return freightTemplateVO;
    }

    @Override
    public FreightTemplateDTO addFreightTemplate(FreightTemplateDTO freightTemplateVO) {
        //获取当前登录商家账号
        AuthUser tokenUser = UserContext.getCurrentUser();
        FreightTemplate freightTemplate = new FreightTemplate();
        //设置店铺ID
        freightTemplateVO.setStoreId(tokenUser.getStoreId());
        //复制属性
        BeanUtils.copyProperties(freightTemplateVO, freightTemplate);
        //添加运费模板
        this.save(freightTemplate);
        //给子模板赋父模板的id
        List<FreightTemplateChild> list = new ArrayList<>();
        //如果子运费模板不为空则进行新增
        if (freightTemplateVO.getFreightTemplateChildList() != null) {
            for (FreightTemplateChild freightTemplateChild : freightTemplateVO.getFreightTemplateChildList()) {
                freightTemplateChild.setFreightTemplateId(freightTemplate.getId());
                list.add(freightTemplateChild);
            }
            //添加运费模板子内容
            freightTemplateChildService.addFreightTemplateChild(list);
        }

        //更新缓存
        cache.remove(CachePrefix.SHIP_TEMPLATE.getPrefix() + tokenUser.getStoreId());
        return freightTemplateVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FreightTemplateDTO editFreightTemplate(FreightTemplateDTO freightTemplateVO) {
        //获取当前登录商家账号
        AuthUser authUser = UserContext.getCurrentUser();
        if (freightTemplateVO.getId().equals(authUser.getStoreId())) {
            throw new ServiceException(ResultCode.USER_AUTHORITY_ERROR);
        }
        FreightTemplate freightTemplate = new FreightTemplate();
        //复制属性
        BeanUtils.copyProperties(freightTemplateVO, freightTemplate);
        //修改运费模板
        this.updateById(freightTemplate);
        //删除模板子内容
        freightTemplateChildService.removeFreightTemplate(freightTemplateVO.getId());
        //给子模板赋父模板的id
        List<FreightTemplateChild> list = new ArrayList<>();
        for (FreightTemplateChild freightTemplateChild : freightTemplateVO.getFreightTemplateChildList()) {
            freightTemplateChild.setFreightTemplateId(freightTemplate.getId());
            list.add(freightTemplateChild);
        }
        //添加模板子内容
        freightTemplateChildService.addFreightTemplateChild(list);
        //更新缓存
        cache.remove(CachePrefix.SHIP_TEMPLATE.getPrefix() + authUser.getStoreId());
        return null;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeFreightTemplate(String id) {
        //获取当前登录商家账号
        AuthUser tokenUser = UserContext.getCurrentUser();
        LambdaQueryWrapper<FreightTemplate> lambdaQueryWrapper = Wrappers.lambdaQuery();
        lambdaQueryWrapper.eq(FreightTemplate::getStoreId, tokenUser.getStoreId());
        lambdaQueryWrapper.eq(FreightTemplate::getId, id);
        //如果删除成功则删除运费模板子项
        if (this.remove(lambdaQueryWrapper)) {
            cache.remove(CachePrefix.SHIP_TEMPLATE.getPrefix() + tokenUser.getStoreId());
            return freightTemplateChildService.removeFreightTemplate(id);
        }
        return false;
    }
}