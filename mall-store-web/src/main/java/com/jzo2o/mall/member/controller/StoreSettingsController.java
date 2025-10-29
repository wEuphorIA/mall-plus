package com.jzo2o.mall.member.controller;


import com.jzo2o.mall.common.enums.ResultUtil;
import com.jzo2o.mall.common.model.ResultMessage;
import com.jzo2o.mall.member.model.dto.StoreAfterSaleAddressDTO;
import com.jzo2o.mall.member.model.dto.StoreDTO;
import com.jzo2o.mall.member.model.dto.StoreDeliverGoodsAddressDTO;
import com.jzo2o.mall.member.model.dto.StoreSettingDTO;
import com.jzo2o.mall.member.service.StoreDetailService;
import com.jzo2o.mall.member.service.StoreService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * 店铺端,店铺设置接口
 */
@RestController
@Api(tags = "店铺端,店铺设置接口")
@RequestMapping("/settings/storeSettings")
public class StoreSettingsController {

    /**
     * 店铺
     */
    @Autowired
    private StoreService storeService;
    /**
     * 店铺详情
     */
    @Autowired
    private StoreDetailService storeDetailService;

    @ApiOperation(value = "获取商家设置")
    @GetMapping
    public StoreDTO get() {
        //获取当前登录商家内容
        StoreDTO storeDetail = storeService.getStoreDetail();
        return storeDetail;
    }

    @ApiOperation(value = "修改商家设置")
    @PutMapping
    public boolean edit(@Valid StoreSettingDTO storeSettingDTO) {
        //修改商家设置
        boolean result = storeDetailService.editStoreSetting(storeSettingDTO);
        return result;
    }

//    @ApiOperation(value = "修改商家设置")
//    @PutMapping("/merchantEuid")
//    public ResultMessage<Object> edit(String merchantEuid) {
//        //修改UDESK设置
//        Boolean result = storeDetailService.editMerchantEuid(merchantEuid);
//        return ResultUtil.data(result);
//    }

    @ApiOperation(value = "修改店铺库存预警数量")
    @ApiImplicitParam(name = "stockWarning", value = "库存预警数量", required = true, dataType = "Integer", paramType = "query")
    @PutMapping("/updateStockWarning")
    public boolean updateStockWarning(Integer stockWarning) {
        //修改商家设置
        boolean result = storeDetailService.updateStockWarning(stockWarning);
        return result;
    }

    @ApiOperation(value = "获取商家退货收件地址")
    @GetMapping("/storeAfterSaleAddress")
    public StoreAfterSaleAddressDTO getStoreAfterSaleAddress() {
        //获取当前登录商家内容
        StoreAfterSaleAddressDTO storeAfterSaleAddressDTO = storeDetailService.getStoreAfterSaleAddressDTO();
        return storeAfterSaleAddressDTO;
    }

    @ApiOperation(value = "修改商家退货收件地址")
    @PutMapping("/storeAfterSaleAddress")
    public boolean editStoreAfterSaleAddress(@Valid StoreAfterSaleAddressDTO storeAfterSaleAddressDTO) {
        //修改商家退货收件地址
        boolean result = storeDetailService.editStoreAfterSaleAddressDTO(storeAfterSaleAddressDTO);
        return result;
    }


    @ApiOperation(value = "获取商家发货地址")
    @GetMapping("/storeDeliverGoodsAddress")
    public StoreDeliverGoodsAddressDTO getStoreDeliverGoodsAddress(){
        StoreDeliverGoodsAddressDTO storeDeliverGoodsAddressDto = storeDetailService.getStoreDeliverGoodsAddressDto();
        return storeDeliverGoodsAddressDto;
    }

    @ApiOperation(value = "修改商家发货地址")
    @PutMapping("/storeDeliverGoodsAddress")
    public boolean editStoreDeliverGoodsAddress(@Valid StoreDeliverGoodsAddressDTO storeDeliverGoodsAddressDTO) {
        //修改商家退货收件地址
        boolean result = storeDetailService.editStoreDeliverGoodsAddressDTO(storeDeliverGoodsAddressDTO);
        return result;
    }
}
