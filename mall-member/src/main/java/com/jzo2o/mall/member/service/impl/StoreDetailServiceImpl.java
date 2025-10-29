package com.jzo2o.mall.member.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jzo2o.common.utils.DateUtils;
import com.jzo2o.common.utils.UserContext;
import com.jzo2o.mall.common.cache.CachePrefix;
import com.jzo2o.mall.common.model.AuthUser;
import com.jzo2o.mall.member.mapper.StoreDetailMapper;
import com.jzo2o.mall.member.model.domain.Store;
import com.jzo2o.mall.member.model.domain.StoreDetail;
import com.jzo2o.mall.member.model.dto.*;
import com.jzo2o.mall.member.service.StoreDetailService;
import com.jzo2o.mall.member.service.StoreService;
import com.jzo2o.redis.helper.Cache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 店铺详细业务层实现
 */
@Service
public class StoreDetailServiceImpl extends ServiceImpl<StoreDetailMapper, StoreDetail> implements StoreDetailService {

    /**
     * 店铺
     */
    @Autowired
    private StoreService storeService;

//    /**
//     * 分类
//     */
//    @Autowired
//    private CategoryService categoryService;
//
//    @Autowired
//    private GoodsService goodsService;
//
//    @Autowired
//    private RocketmqCustomProperties rocketmqCustomProperties;
//
//    @Autowired
//    private RocketMQTemplate rocketMQTemplate;

    @Autowired
    private Cache cache;

    @Override
    public StoreDetailDTO getStoreDetailDTO(String storeId) {
        StoreDetailDTO storeDetailVO = (StoreDetailDTO) cache.get(CachePrefix.STORE.getPrefix() + storeId);
        if (storeDetailVO == null) {
            storeDetailVO = this.baseMapper.getStoreDetail(storeId);
            cache.put(CachePrefix.STORE.getPrefix() + storeId, storeDetailVO, 7200L);
        }
        return storeDetailVO;
    }

    @Override
    public StoreDetailDTO getStoreDetailVOByMemberId(String memberId) {
        return this.baseMapper.getStoreDetailByMemberId(memberId);
    }

    @Override
    public StoreDetail getStoreDetail(String storeId) {
        LambdaQueryWrapper<StoreDetail> lambdaQueryWrapper = Wrappers.lambdaQuery();
        lambdaQueryWrapper.eq(StoreDetail::getStoreId, storeId);
        return this.getOne(lambdaQueryWrapper);
    }

    @Override
    public Boolean editStoreSetting(StoreSettingDTO storeSettingDTO) {
        AuthUser tokenUser = UserContext.getCurrentUser();
        //修改店铺
        Store store = storeService.getById(tokenUser.getStoreId());
        BeanUtil.copyProperties(storeSettingDTO, store);
        boolean result = storeService.updateById(store);
        if (result) {
//            this.updateStoreGoodsInfo(store);
            this.removeCache(store.getId());
        }
//        String destination = rocketmqCustomProperties.getStoreTopic() + ":" + StoreTagsEnum.EDIT_STORE_SETTING.name();
//        //发送订单变更mq消息
//        rocketMQTemplate.asyncSend(destination, store, RocketmqSendCallbackBuilder.commonCallback());
        return result;
    }

//    @Override
//    public void updateStoreGoodsInfo(Store store) {
//
//        goodsService.updateStoreDetail(store);
//
//        Map<String, Object> updateIndexFieldsMap = EsIndexUtil.getUpdateIndexFieldsMap(
//                MapUtil.builder(new HashMap<String, Object>()).put("storeId", store.getId()).build(),
//                MapUtil.builder(new HashMap<String, Object>()).put("storeName", store.getStoreName()).put("selfOperated", store.getSelfOperated()).build());
//        String destination = rocketmqCustomProperties.getGoodsTopic() + ":" + GoodsTagsEnum.UPDATE_GOODS_INDEX_FIELD.name();
//        //发送mq消息
//        rocketMQTemplate.asyncSend(destination, JSONUtil.toJsonStr(updateIndexFieldsMap), RocketmqSendCallbackBuilder.commonCallback());
//    }

//    @Override
//    public Boolean editMerchantEuid(String merchantEuid) {
//        AuthUser tokenUser = (AuthUser) Objects.requireNonNull(UserContext.getCurrentUser());
//        Store store = storeService.getById(tokenUser.getStoreId());
//        store.setMerchantEuid(merchantEuid);
//        this.removeCache(store.getId());
//        return storeService.updateById(store);
//    }

    /**
     * 获取待结算店铺列表
     * @param day  结算日
     * @param settlementDate 结算日期
     * @return 待结算店铺列表
     */
    @Override
    public List<StoreSettlementDay> getSettlementStore(int day, LocalDateTime settlementDate) {

        //按结算日和结算时间检索
        List<StoreSettlementDay> settlementStore = this.baseMapper.getSettlementStore(day, settlementDate);
        return settlementStore;
    }

    @Override
    public StoreDeliverGoodsAddressDTO getStoreDeliverGoodsAddressDto() {
        AuthUser authUser = UserContext.getCurrentUser();
        String storeId = authUser.getStoreId();
        return this.baseMapper.getStoreDeliverGoodsAddressDto(storeId);
    }

    @Override
    public StoreDeliverGoodsAddressDTO getStoreDeliverGoodsAddressDto(String id) {
        StoreDeliverGoodsAddressDTO storeDeliverGoodsAddressDto = this.baseMapper.getStoreDeliverGoodsAddressDto(id);
        if (storeDeliverGoodsAddressDto == null) {
            storeDeliverGoodsAddressDto = new StoreDeliverGoodsAddressDTO();
        }
        return storeDeliverGoodsAddressDto;
    }

    @Override
    public boolean editStoreDeliverGoodsAddressDTO(StoreDeliverGoodsAddressDTO storeDeliverGoodsAddressDto) {
        AuthUser authUser = UserContext.getCurrentUser();
        String storeId = authUser.getStoreId();
        LambdaUpdateWrapper<StoreDetail> lambdaUpdateWrapper = Wrappers.lambdaUpdate();
        lambdaUpdateWrapper.set(StoreDetail::getSalesConsignorName, storeDeliverGoodsAddressDto.getSalesConsignorName());
        lambdaUpdateWrapper.set(StoreDetail::getSalesConsignorMobile, storeDeliverGoodsAddressDto.getSalesConsignorMobile());
        lambdaUpdateWrapper.set(StoreDetail::getSalesConsignorAddressId, storeDeliverGoodsAddressDto.getSalesConsignorAddressId());
        lambdaUpdateWrapper.set(StoreDetail::getSalesConsignorAddressPath, storeDeliverGoodsAddressDto.getSalesConsignorAddressPath());
        lambdaUpdateWrapper.set(StoreDetail::getSalesConsignorDetail, storeDeliverGoodsAddressDto.getSalesConsignorDetail());
        lambdaUpdateWrapper.eq(StoreDetail::getStoreId, storeId);

        this.removeCache(storeId);
        return this.update(lambdaUpdateWrapper);
    }

    /**
     * 修改店铺的结算日
     *
     * @param storeId  店铺ID
     * @param dateTime 结算日
     */
    @Override
    public void updateSettlementDay(String storeId, LocalDateTime dateTime) {
        this.removeCache(storeId);
//        this.baseMapper.updateSettlementDay(storeId, dateTime);
        LambdaUpdateWrapper<StoreDetail> lambdaUpdateWrapper = Wrappers.lambdaUpdate();
        lambdaUpdateWrapper.set(StoreDetail::getSettlementDay, dateTime);
        lambdaUpdateWrapper.eq(StoreDetail::getStoreId, storeId);
        this.update(lambdaUpdateWrapper);
    }

    @Override
    public StoreBasicInfoDTO getStoreBasicInfoDTO(String storeId) {
        return this.baseMapper.getStoreBasicInfoDTO(storeId);
    }

    @Override
    public StoreAfterSaleAddressDTO getStoreAfterSaleAddressDTO() {
        AuthUser authUser = UserContext.getCurrentUser();
        String storeId = authUser.getStoreId();
        return this.baseMapper.getStoreAfterSaleAddressDTO(storeId);
    }


    @Override
    public StoreAfterSaleAddressDTO getStoreAfterSaleAddressDTO(String id) {
        StoreAfterSaleAddressDTO storeAfterSaleAddressDTO = this.baseMapper.getStoreAfterSaleAddressDTO(id);
        if (storeAfterSaleAddressDTO == null) {
            storeAfterSaleAddressDTO = new StoreAfterSaleAddressDTO();
        }
        return storeAfterSaleAddressDTO;
    }

    @Override
    public boolean editStoreAfterSaleAddressDTO(StoreAfterSaleAddressDTO storeAfterSaleAddressDTO) {
        AuthUser authUser = UserContext.getCurrentUser();
        String storeId = authUser.getStoreId();
        LambdaUpdateWrapper<StoreDetail> lambdaUpdateWrapper = Wrappers.lambdaUpdate();
        lambdaUpdateWrapper.set(StoreDetail::getSalesConsigneeName, storeAfterSaleAddressDTO.getSalesConsigneeName());
        lambdaUpdateWrapper.set(StoreDetail::getSalesConsigneeAddressId, storeAfterSaleAddressDTO.getSalesConsigneeAddressId());
        lambdaUpdateWrapper.set(StoreDetail::getSalesConsigneeAddressPath, storeAfterSaleAddressDTO.getSalesConsigneeAddressPath());
        lambdaUpdateWrapper.set(StoreDetail::getSalesConsigneeDetail, storeAfterSaleAddressDTO.getSalesConsigneeDetail());
        lambdaUpdateWrapper.set(StoreDetail::getSalesConsigneeMobile, storeAfterSaleAddressDTO.getSalesConsigneeMobile());
        lambdaUpdateWrapper.eq(StoreDetail::getStoreId, storeId);

        this.removeCache(storeId);
        return this.update(lambdaUpdateWrapper);
    }


    @Override
    public boolean updateStockWarning(Integer stockWarning) {
        AuthUser authUser = UserContext.getCurrentUser();
        String storeId = authUser.getStoreId();
        LambdaUpdateWrapper<StoreDetail> lambdaUpdateWrapper = Wrappers.lambdaUpdate();
        lambdaUpdateWrapper.set(StoreDetail::getStockWarning, stockWarning);
        lambdaUpdateWrapper.eq(StoreDetail::getStoreId, storeId);
        this.removeCache(storeId);
        return this.update(lambdaUpdateWrapper);
    }

//    @Override
//    public List<StoreManagementCategoryDTO> goodsManagementCategory(String storeId) {
//
//        //获取顶部分类列表
//        List<Category> categoryList = categoryService.firstCategory();
//        //获取店铺信息
//        StoreDetail storeDetail = this.getOne(new LambdaQueryWrapper<StoreDetail>().eq(StoreDetail::getStoreId, storeId));
//        //获取店铺分类
//        String[] storeCategoryList = storeDetail.getGoodsManagementCategory().split(",");
//        List<StoreManagementCategoryDTO> list = new ArrayList<>();
//        for (Category category : categoryList) {
//            StoreManagementCategoryDTO storeManagementCategoryVO = new StoreManagementCategoryDTO(category);
//            for (String storeCategory : storeCategoryList) {
//                if (storeCategory.equals(category.getId())) {
//                    storeManagementCategoryVO.setSelected(true);
//                }
//            }
//            list.add(storeManagementCategoryVO);
//        }
//        return list;
//    }

    @Override
    public StoreLicenceDTO getStoreLicence(String storeId) {
        StoreLicenceDTO storeLicenceDTO = this.baseMapper.getLicencePhoto(storeId);
        return storeLicenceDTO;
    }


    /**
     * 删除缓存
     *
     * @param storeId 店铺id
     */
    private void removeCache(String storeId) {
        cache.remove(CachePrefix.STORE.getPrefix() + storeId);
    }

}