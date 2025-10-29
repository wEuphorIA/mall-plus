package com.jzo2o.mall.member.controller;


import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jzo2o.common.utils.UserContext;
import com.jzo2o.mall.common.enums.ResultCode;
import com.jzo2o.mall.common.enums.ResultUtil;
import com.jzo2o.mall.common.exception.ServiceException;
import com.jzo2o.mall.common.model.AuthUser;
import com.jzo2o.mall.common.model.ResultMessage;
import com.jzo2o.mall.member.model.domain.StoreLogistics;
import com.jzo2o.mall.member.model.dto.LogisticsSetting;
import com.jzo2o.mall.member.model.dto.StoreLogisticsCustomerDTO;
import com.jzo2o.mall.member.model.dto.StoreLogisticsDTO;
import com.jzo2o.mall.member.service.StoreLogisticsService;
import com.jzo2o.mall.system.enums.SettingEnum;
import com.jzo2o.mall.system.model.domain.Setting;
import com.jzo2o.mall.system.service.SettingService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

/**
 * 店铺端,物流公司接口
 */
@RestController
@Api(tags = "店铺端,物流公司接口")
@RequestMapping("/other/logistics")
public class LogisticsStoreController {

    /**
     * 物流公司
     */
    @Autowired
    private StoreLogisticsService storeLogisticsService;

    @Autowired
    private SettingService settingService;

    @ApiOperation(value = "获取商家物流公司列表，如果已选择则checked有值")
    @GetMapping
    public List<StoreLogisticsDTO> get() {
        AuthUser authUser  =UserContext.getCurrentUser();
        String storeId = authUser.getStoreId();
        //获取已开启的物流公司
        List<StoreLogisticsDTO> storeLogistics = storeLogisticsService.getOpenStoreLogistics(storeId);
        //获取未开启的物流公司
        List<StoreLogisticsDTO> closeStoreLogistics = storeLogisticsService.getCloseStoreLogistics(storeId);
        storeLogistics.addAll(closeStoreLogistics);
        return storeLogistics;
    }

    @ApiOperation(value = "获取商家已选择物流公司列表")
    @GetMapping("/getChecked")
    public List<StoreLogisticsDTO> getChecked() {
        AuthUser authUser  =UserContext.getCurrentUser();
        String storeId = authUser.getStoreId();
        List<StoreLogisticsDTO> storeSelectedLogistics = storeLogisticsService.getStoreSelectedLogistics(storeId);
        return storeSelectedLogistics;
    }

    @ApiOperation(value = "选择物流公司")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "logisticsId", value = "物流公司ID", required = true, paramType = "path"),
    })
    @PostMapping("/{logisticsId}")
    public StoreLogistics checked(@PathVariable String logisticsId, @RequestBody StoreLogisticsCustomerDTO storeLogisticsCustomerDTO) {
        AuthUser authUser  =UserContext.getCurrentUser();
        String storeId = authUser.getStoreId();
        StoreLogistics add = storeLogisticsService.add(logisticsId, storeId, storeLogisticsCustomerDTO);
        return add;
    }


    @ApiOperation(value = "取消选择物流公司")
    @ApiImplicitParam(name = "id", value = "物流公司ID", required = true, paramType = "path")
    @DeleteMapping(value = "/{id}")
    public boolean cancel(@PathVariable String id) {
        AuthUser authUser  =UserContext.getCurrentUser();
        String storeId = authUser.getStoreId();
        boolean remove = storeLogisticsService.remove(new LambdaQueryWrapper<StoreLogistics>().eq(StoreLogistics::getLogisticsId, id).eq(StoreLogistics::getStoreId, storeId));
        return remove;
    }

    @ApiOperation(value = "修改电子面单参数")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "logisticsId", value = "物流公司ID", required = true, paramType = "path"),
    })
    @PutMapping("/{logisticsId}/updateStoreLogistics")
    public StoreLogistics updateStoreLogistics(@PathVariable String logisticsId,StoreLogisticsCustomerDTO storeLogisticsCustomerDTO){
        AuthUser authUser  =UserContext.getCurrentUser();
        String storeId = authUser.getStoreId();
        StoreLogistics update = storeLogisticsService.update(logisticsId, storeId, storeLogisticsCustomerDTO);
        return update;
    }

    @ApiOperation(value = "获取商家已选择物流公司并且使用电子面单列表")
    @GetMapping("/getCheckedFaceSheet")
    public List<StoreLogisticsDTO> getCheckedFaceSheet() {
        AuthUser authUser  =UserContext.getCurrentUser();
        String storeId = authUser.getStoreId();
        List<StoreLogisticsDTO> storeSelectedLogisticsUseFaceSheet = storeLogisticsService.getStoreSelectedLogisticsUseFaceSheet(storeId);
        return storeSelectedLogisticsUseFaceSheet;
    }

    @ApiOperation(value = "获取店铺-物流公司详细信息")
    @ApiImplicitParam(name = "logisticsId", value = "物流公司ID", required = true, paramType = "path")
    @GetMapping("/{logisticsId}/getStoreLogistics")
    public StoreLogistics getStoreLogistics(@PathVariable String logisticsId){
        StoreLogistics storeLogisticsInfo = storeLogisticsService.getStoreLogisticsInfo(logisticsId);
        return storeLogisticsInfo;
    }

    @ApiOperation(value = "获取IM接口前缀")
    @GetMapping("/setting")
    public String getUrl() {
        String logisticsType;
        try {
            Setting logisticsSettingVal = settingService.get(SettingEnum.LOGISTICS_SETTING.name());
            LogisticsSetting logisticsSetting = JSONUtil.toBean(logisticsSettingVal.getSettingValue(), LogisticsSetting.class);
            logisticsType = logisticsSetting.getType();
        } catch (Exception e) {
            throw new ServiceException(ResultCode.ORDER_LOGISTICS_ERROR);
        }
        return logisticsType;
    }

}
