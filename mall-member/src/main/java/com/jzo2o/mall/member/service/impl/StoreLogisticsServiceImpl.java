package com.jzo2o.mall.member.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jzo2o.common.utils.UserContext;
import com.jzo2o.mall.common.model.AuthUser;
import com.jzo2o.mall.member.mapper.StoreLogisticsMapper;
import com.jzo2o.mall.member.model.domain.StoreLogistics;
import com.jzo2o.mall.member.model.dto.StoreLogisticsCustomerDTO;
import com.jzo2o.mall.member.model.dto.StoreLogisticsDTO;
import com.jzo2o.mall.member.service.StoreLogisticsService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * 物流公司业务层实现
 */
@Service
public class StoreLogisticsServiceImpl extends ServiceImpl<StoreLogisticsMapper, StoreLogistics> implements StoreLogisticsService {

    @Override
    public List<StoreLogisticsDTO> getStoreLogistics(String storeId) {
        return this.baseMapper.getStoreLogistics(storeId);
    }

    @Override
    public List<StoreLogisticsDTO> getStoreSelectedLogistics(String storeId) {
        return this.baseMapper.getSelectedStoreLogistics(storeId);

    }

    @Override
    public List<String> getStoreSelectedLogisticsName(String storeId) {
        return this.baseMapper.getSelectedStoreLogisticsName(storeId);
    }

    @Override
    public List<StoreLogisticsDTO> getStoreSelectedLogisticsUseFaceSheet(String storeId) {
        return this.baseMapper.getSelectedStoreLogisticsUseFaceSheet(storeId);
    }

    @Override
    public StoreLogistics update(String logisticsId, String storeId, StoreLogisticsCustomerDTO storeLogisticsCustomerDTO) {
        LambdaQueryWrapper<StoreLogistics> lambdaQueryWrapper = Wrappers.lambdaQuery();
        lambdaQueryWrapper.eq(StoreLogistics::getLogisticsId, logisticsId);
        lambdaQueryWrapper.eq(StoreLogistics::getStoreId, storeId);
        this.remove(lambdaQueryWrapper);
        StoreLogistics ResultstoreLogistics = new StoreLogistics(storeLogisticsCustomerDTO);
        ResultstoreLogistics.setStoreId(storeId);
        ResultstoreLogistics.setLogisticsId(logisticsId);
        this.save(ResultstoreLogistics);
        return ResultstoreLogistics;
    }

    @Override
    public StoreLogistics getStoreLogisticsInfo(String logisticsId) {
        AuthUser authUser = UserContext.getCurrentUser();
        String storeId = authUser.getStoreId();
        return this.getOne(new LambdaQueryWrapper<StoreLogistics>().eq(StoreLogistics::getStoreId, storeId).eq(StoreLogistics::getLogisticsId, logisticsId));
    }

    @Override
    public List<StoreLogisticsDTO> getOpenStoreLogistics(String storeId) {
        List<StoreLogisticsDTO> openStoreLogistics = this.baseMapper.getOpenStoreLogistics(storeId);
        for (StoreLogisticsDTO storeLogisticsVO : openStoreLogistics) {
            storeLogisticsVO.setSelected("1");
        }
        return openStoreLogistics;
    }

    @Override
    public List<StoreLogisticsDTO> getCloseStoreLogistics(String storeId) {
        return this.baseMapper.getCloseStroreLogistics(storeId);
    }

    @Override
    public StoreLogistics add(String logisticsId, String storeId, StoreLogisticsCustomerDTO storeLogisticsCustomerDTO) {
        //判断是否已经选择过，如果没有选择则进行添加
        LambdaQueryWrapper<StoreLogistics> lambdaQueryWrapper = Wrappers.lambdaQuery();
        lambdaQueryWrapper.eq(StoreLogistics::getLogisticsId, logisticsId);
        lambdaQueryWrapper.eq(StoreLogistics::getStoreId, storeId);
        StoreLogistics storeLogistics = null;
        if (this.getOne(lambdaQueryWrapper) == null) {
            storeLogistics = new StoreLogistics(storeLogisticsCustomerDTO);
            storeLogistics.setStoreId(storeId);
            storeLogistics.setLogisticsId(logisticsId);
            this.save(storeLogistics);
            return storeLogistics;
        }
        return null;
    }


}