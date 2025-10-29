package com.jzo2o.mall.system.service.impl;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.PageUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jzo2o.common.utils.UserContext;
import com.jzo2o.mall.common.enums.*;
import com.jzo2o.mall.common.exception.ServiceException;
import com.jzo2o.mall.common.model.AuthUser;
import com.jzo2o.mall.system.mapper.PageDataMapper;
import com.jzo2o.mall.system.model.domain.PageData;
import com.jzo2o.mall.system.model.dto.PageDataDTO;
import com.jzo2o.mall.system.model.dto.PageDataListDTO;
import com.jzo2o.mall.system.service.PageDataService;
import com.jzo2o.mysql.domain.PageVO;
import com.jzo2o.mysql.utils.PageUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * 楼层装修管理业务层实现
 */
@Service
public class PageDataServiceImpl extends ServiceImpl<PageDataMapper, PageData> implements PageDataService {


//    @Autowired
//    private SystemSettingProperties systemSettingProperties;
//
//    @Override
//    @Transactional(rollbackFor = Exception.class)
//    public void addStorePageData(String storeId) {
//        //设置店铺的PC页面
//        PageData pageData = new PageData();
//        pageData.setNum(storeId);
//        pageData.setPageClientType(ClientTypeEnum.PC.value());
//        pageData.setPageShow(SwitchEnum.OPEN.name());
//        pageData.setPageType(PageEnum.STORE.value());
//        this.save(pageData);
//
//        //设置店铺的Mobile页面
//        PageData mobilePageData = new PageData();
//        mobilePageData.setNum(storeId);
//        mobilePageData.setPageClientType(ClientTypeEnum.H5.value());
//        mobilePageData.setPageShow(SwitchEnum.OPEN.name());
//        mobilePageData.setPageType(PageEnum.STORE.value());
//        this.save(mobilePageData);
//    }
//
    @Override
    @Transactional(rollbackFor = Exception.class)
    public PageData addPageData(PageData pageData) {
        AuthUser currentUser = UserContext.getCurrentUser();
        //演示站点判定
//        if (Boolean.TRUE.equals(systemSettingProperties.getDemoSite()) && (pageData.getPageShow().equals(SwitchEnum.OPEN.name()) && pageData.getPageType().equals(PageEnum.INDEX.name()))) {
//                pageData.setPageShow(SwitchEnum.CLOSE.name());
//        }

        //如果页面为发布，则关闭其他页面，开启此页面
        if (pageData.getPageShow().equals(SwitchEnum.OPEN.name())) {
            LambdaUpdateWrapper<PageData> lambdaUpdateWrapper = Wrappers.lambdaUpdate();
            lambdaUpdateWrapper.eq(CharSequenceUtil.equals(currentUser.getRole().name(), UserEnums.STORE.name()), PageData::getNum
                    , currentUser.getStoreId());
            lambdaUpdateWrapper.eq(PageData::getPageType, pageData.getPageType());
            lambdaUpdateWrapper.eq(PageData::getPageClientType, pageData.getPageClientType());
            lambdaUpdateWrapper.set(PageData::getPageShow, SwitchEnum.CLOSE.name());
            this.update(lambdaUpdateWrapper);
        } else {
            pageData.setPageShow(SwitchEnum.CLOSE.name());
        }
        this.save(pageData);
        return pageData;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PageData updatePageData(PageData pageData) {
        AuthUser currentUser = UserContext.getCurrentUser();
        //如果页面为发布，则关闭其他页面，开启此页面
        if (pageData.getPageShow() != null && pageData.getPageShow().equals(SwitchEnum.OPEN.name())) {
            LambdaUpdateWrapper<PageData> lambdaUpdateWrapper = Wrappers.lambdaUpdate();
            lambdaUpdateWrapper.eq(CharSequenceUtil.isNotEmpty(pageData.getPageType()), PageData::getPageType, pageData.getPageType());
            lambdaUpdateWrapper.eq(CharSequenceUtil.isNotEmpty(pageData.getPageClientType()), PageData::getPageClientType,
                    pageData.getPageClientType());

            //如果是管理员，则判定页面num为null
            if (currentUser.getRole().name().equals(UserEnums.MANAGER.name())) {
                lambdaUpdateWrapper.isNull(PageData::getNum);
            } else {
                lambdaUpdateWrapper.eq(PageData::getNum, pageData.getNum());
            }

            lambdaUpdateWrapper.set(PageData::getPageShow, SwitchEnum.CLOSE.name());
            this.update(lambdaUpdateWrapper);
        } else {
            pageData.setPageShow(SwitchEnum.CLOSE.name());
        }

        LambdaUpdateWrapper<PageData> lambdaUpdateWrapper = Wrappers.lambdaUpdate();
        lambdaUpdateWrapper.set(PageData::getPageData, pageData.getPageData());
        lambdaUpdateWrapper.eq(PageData::getId, pageData.getId());
        lambdaUpdateWrapper.eq(CharSequenceUtil.equals(currentUser.getRole().name(), UserEnums.STORE.name()),
                PageData::getPageType, PageEnum.STORE.name());
        lambdaUpdateWrapper.eq(CharSequenceUtil.equals(currentUser.getRole().name(), UserEnums.STORE.name()), PageData::getNum,
                currentUser.getStoreId());
        this.updateById(pageData);
        return pageData;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PageData releasePageData(String id) {
        PageData pageData = this.getCurrentPageData(id);
        if (pageData == null) {
            throw new ServiceException(ResultCode.PAGE_NOT_EXIST);
        }

        //如果已经发布，不能重复发布
        if (pageData.getPageShow().equals(SwitchEnum.OPEN.name())) {
            throw new ServiceException(ResultCode.PAGE_RELEASE_ERROR);
        }

        LambdaUpdateWrapper<PageData> lambdaUpdateWrapper = Wrappers.lambdaUpdate();
        lambdaUpdateWrapper.set(PageData::getPageShow, SwitchEnum.CLOSE.name());
        lambdaUpdateWrapper.eq(PageData::getPageType, pageData.getPageType());
        lambdaUpdateWrapper.eq(PageData::getPageClientType, pageData.getPageClientType());
        //如果是店铺需要设置店铺ID
        if (pageData.getPageType().equals(PageEnum.STORE.value())) {
            lambdaUpdateWrapper.eq(PageData::getNum, pageData.getNum());
        }
        //设置禁用所有店铺首页
        this.update(lambdaUpdateWrapper);

        //将当前页面启用
        LambdaUpdateWrapper<PageData> wrapper = Wrappers.lambdaUpdate();
        wrapper.set(PageData::getPageShow, SwitchEnum.OPEN.name());
        wrapper.eq(PageData::getId, pageData.getId());
        this.update(wrapper);
        return pageData;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removePageData(String id) {
        PageData pageData = this.getCurrentPageData(id);
        if (pageData == null) {
            throw new ServiceException(ResultCode.PAGE_NOT_EXIST);
        }

        //专题则直接进行删除
        if (pageData.getPageType().equals(PageEnum.SPECIAL.name())) {
            return this.removeById(id);
        }
        //店铺、平台首页需要判断是否开启，开启则无法删除
        if (pageData.getPageShow().equals(SwitchEnum.OPEN.name())) {
            throw new ServiceException(ResultCode.PAGE_OPEN_DELETE_ERROR);
        }
        //判断是否有其他页面，如果没有其他的页面则无法进行删除
        LambdaQueryWrapper<PageData> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PageData::getPageType, pageData.getPageType());
        queryWrapper.eq(pageData.getPageClientType() != null, PageData::getPageClientType, pageData.getPageClientType());
        //如果为店铺页面需要设置店铺ID
        if (pageData.getPageType().equals(PageEnum.STORE.name())) {
            queryWrapper.eq(pageData.getNum() != null, PageData::getNum, pageData.getNum());
        }
        //判断是否为唯一的页面
        if (this.baseMapper.getPageDataNum(queryWrapper) == 1) {
            throw new ServiceException(ResultCode.PAGE_DELETE_ERROR);
        }
        return this.removeById(id);
    }

    @Override
    public PageDataDTO getPageData(PageDataDTO pageDataDTO) {

        //如果获取的是专题、店铺页面数据需要传入ID
        if (!pageDataDTO.getPageType().equals(PageEnum.INDEX.name()) && pageDataDTO.getNum() == null) {
            throw new ServiceException(ResultCode.PAGE_NOT_EXIST);
        }
        QueryWrapper<PageDataDTO> queryWrapper = Wrappers.query();
        queryWrapper.eq("page_type", pageDataDTO.getPageType());
        queryWrapper.eq(pageDataDTO.getNum() != null, "num", pageDataDTO.getNum());
        queryWrapper.eq("page_show", SwitchEnum.OPEN.name());

        queryWrapper.eq("page_client_type", pageDataDTO.getPageClientType());

        return this.baseMapper.getPageData(queryWrapper);
    }

    @Override
    public IPage<PageDataListDTO> getPageDataList(PageVO pageVO, PageDataDTO pageDataDTO) {
        QueryWrapper<PageDataListDTO> queryWrapper = Wrappers.query();
        queryWrapper.eq(pageDataDTO.getPageType() != null, "page_type", pageDataDTO.getPageType());
        queryWrapper.eq(pageDataDTO.getNum() != null, "num", pageDataDTO.getNum());
        queryWrapper.eq(pageDataDTO.getPageClientType() != null, "page_client_type", pageDataDTO.getPageClientType());

        return this.baseMapper.getPageDataList(PageUtils.initPage(pageVO), queryWrapper);

    }

    @Override
    public PageData getSpecial(String id) {
        return this.getById(id);
    }

    private PageData getCurrentPageData(String id) {
        AuthUser currentUser = UserContext.getCurrentUser();
        return this.getOne(new LambdaQueryWrapper<PageData>()
                .eq(CharSequenceUtil.equals(currentUser.getRole().name(), UserEnums.STORE.name()), PageData::getPageType, PageEnum.STORE.name())
                .eq(CharSequenceUtil.equals(currentUser.getRole().name(), UserEnums.STORE.name()), PageData::getNum, currentUser.getStoreId())
                .eq(PageData::getId, id), false);

    }
}