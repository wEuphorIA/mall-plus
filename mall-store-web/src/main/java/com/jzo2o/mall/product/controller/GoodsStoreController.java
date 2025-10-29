package com.jzo2o.mall.product.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jzo2o.common.utils.UserContext;
import com.jzo2o.mall.common.enums.ResultCode;
import com.jzo2o.mall.common.enums.ResultUtil;
import com.jzo2o.mall.common.exception.ServiceException;
import com.jzo2o.mall.common.model.AuthUser;
import com.jzo2o.mall.common.model.ResultMessage;
import com.jzo2o.mall.product.model.domain.Goods;
import com.jzo2o.mall.product.model.domain.GoodsSku;
import com.jzo2o.mall.product.model.dto.*;
import com.jzo2o.mall.common.enums.GoodsStatusEnum;
import com.jzo2o.mall.product.service.GoodsService;
import com.jzo2o.mall.product.service.GoodsSkuService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 店铺端,商品接口
 */
@RestController
@Slf4j
@Api(tags = "店铺端,商品接口")
@RequestMapping("/goods/goods")
public class GoodsStoreController {

    /**
     * 商品
     */
    @Autowired
    private GoodsService goodsService;
    /**
     * 商品sku
     */
    @Autowired
    private GoodsSkuService goodsSkuService;


    @ApiOperation(value = "分页获取商品列表")
    @GetMapping(value = "/list")
    public IPage<Goods> getByPage(GoodsSearchParamsDTO goodsSearchParams) {
        //获取当前登录商家账号
        AuthUser authUser =  UserContext.getCurrentUser();
        String storeId = authUser.getStoreId();
        goodsSearchParams.setStoreId(storeId);
        IPage<Goods> goodsIPage = goodsService.queryByParams(goodsSearchParams);
        return goodsIPage;
    }

    @ApiOperation(value = "分页获取商品Sku列表")
    @GetMapping(value = "/sku/list")
    public IPage<GoodsSku> getSkuByPage(GoodsSearchParamsDTO goodsSearchParams) {
        //获取当前登录商家账号
        AuthUser authUser =  UserContext.getCurrentUser();
        String storeId = authUser.getStoreId();
        goodsSearchParams.setStoreId(storeId);
        IPage<GoodsSku> goodsSkuByPage = goodsSkuService.getGoodsSkuByPage(goodsSearchParams);
        return goodsSkuByPage;
    }

    @ApiOperation(value = "分页获取库存告警商品列表")
    @GetMapping(value = "/list/stock")
    public IPage<GoodsSku> getWarningStockByPage(GoodsSearchParamsDTO goodsSearchParams) {
        //获取当前登录商家账号
        AuthUser authUser =  UserContext.getCurrentUser();
        String storeId = authUser.getStoreId();
        goodsSearchParams.setStoreId(storeId);
        goodsSearchParams.setAlertQuantity(true);
        goodsSearchParams.setMarketEnable(GoodsStatusEnum.UPPER.name());
        IPage<GoodsSku> goodsSkuPage = goodsSkuService.getGoodsSkuByPage(goodsSearchParams);
        return goodsSkuPage;
    }

    @ApiOperation(value = "批量修改商品预警库存")
    @PutMapping(value = "/batch/update/alert/stocks", consumes = "application/json")
    public void batchUpdateAlertQuantity(@RequestBody List<GoodsSkuStockDTO> updateStockList) {
        //获取当前登录商家账号
        AuthUser authUser =  UserContext.getCurrentUser();
        String storeId = authUser.getStoreId();
        // 获取商品skuId集合
        List<String> goodsSkuIds = updateStockList.stream().map(GoodsSkuStockDTO::getSkuId).collect(Collectors.toList());
        // 根据skuId集合查询商品信息
        LambdaQueryWrapper<GoodsSku> eq = new LambdaQueryWrapper<GoodsSku>().in(GoodsSku::getId, goodsSkuIds).eq(GoodsSku::getStoreId, storeId);
        List<GoodsSku> goodsSkuList = goodsSkuService.list(eq);
        // 过滤不符合当前店铺的商品
        List<String> filterGoodsSkuIds = goodsSkuList.stream().map(GoodsSku::getId).collect(Collectors.toList());
        List<GoodsSkuStockDTO> collect = updateStockList.stream().filter(i -> filterGoodsSkuIds.contains(i.getSkuId())).collect(Collectors.toList());
        goodsSkuService.batchUpdateAlertQuantity(collect);
    }

    @ApiOperation(value = "修改商品预警库存")
    @PutMapping(value = "/update/alert/stocks", consumes = "application/json")
    public void updateAlertQuantity(@RequestBody GoodsSkuStockDTO goodsSkuStockDTO) {
        goodsSkuService.updateAlertQuantity(goodsSkuStockDTO);
    }


    @ApiOperation(value = "通过id获取")
    @GetMapping(value = "/get/{id}")
    public GoodsDTO get(@PathVariable String id) {
        GoodsDTO goodsDTO = goodsService.getGoodsVO(id);
        return goodsDTO;
    }

    @ApiOperation(value = "新增商品")
    @PostMapping(value = "/create", consumes = "application/json", produces = "application/json")
    public void save(@Valid @RequestBody GoodsOperationDTO goodsOperationDTO) {
        goodsService.addGoods(goodsOperationDTO);
    }

    @ApiOperation(value = "修改商品")
    @PutMapping(value = "/update/{goodsId}", consumes = "application/json", produces = "application/json")
    public void update(@Valid @RequestBody GoodsOperationDTO goodsOperationDTO, @PathVariable String goodsId) {
        goodsService.editGoods(goodsOperationDTO, goodsId);
    }

    @ApiOperation(value = "下架商品", notes = "下架商品时使用")
    @ApiImplicitParam(name = "goodsId", value = "商品ID", required = true, paramType = "query", allowMultiple = true)
    @PutMapping(value = "/under")
    public void underGoods(@RequestParam List<String> goodsId) {
        goodsService.updateGoodsMarketAble(goodsId, GoodsStatusEnum.DOWN, "商家下架");
    }

    @ApiOperation(value = "上架商品", notes = "上架商品时使用")
    @PutMapping(value = "/up")
    @ApiImplicitParam(name = "goodsId", value = "商品ID", required = true, paramType = "query", allowMultiple = true)
    public void unpGoods(@RequestParam List<String> goodsId) {
        goodsService.updateGoodsMarketAble(goodsId, GoodsStatusEnum.UPPER, "");
    }

    @ApiOperation(value = "删除商品")
    @PutMapping(value = "/delete")
    @ApiImplicitParam(name = "goodsId", value = "商品ID", required = true, paramType = "query", allowMultiple = true)
    public void deleteGoods(@RequestParam List<String> goodsId) {
        goodsService.deleteGoods(goodsId);
    }

    @ApiOperation(value = "设置商品运费模板")
    @PutMapping(value = "/freight")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "goodsId", value = "商品ID", required = true, paramType = "query", allowMultiple = true),
            @ApiImplicitParam(name = "templateId", value = "运费模板ID", required = true, paramType = "query")
    })
    public ResultMessage<Object> freight(@RequestParam List<String> goodsId, @RequestParam String templateId) {
        goodsService.freight(goodsId, templateId);
        return ResultUtil.success();
    }

    @ApiOperation(value = "根据goodsId分页获取商品规格列表")
    @GetMapping(value = "/sku/{goodsId}/list")
    public List<GoodsSkuDTO> getSkuByList(@PathVariable String goodsId) {
        //获取当前登录商家账号
        AuthUser authUser =  UserContext.getCurrentUser();
        String storeId = authUser.getStoreId();
        List<GoodsSku> list = goodsSkuService.list(new LambdaQueryWrapper<GoodsSku>().eq(GoodsSku::getGoodsId, goodsId).eq(GoodsSku::getStoreId, storeId));
        List<GoodsSkuDTO> goodsSkuVOList = goodsSkuService.getGoodsSkuVOList(list);
        return goodsSkuVOList;
    }

    @ApiOperation(value = "修改商品库存")
    @PutMapping(value = "/update/stocks", consumes = "application/json")
    public void updateStocks(@RequestBody List<GoodsSkuStockDTO> updateStockList) {
        //获取当前登录商家账号
        AuthUser authUser =  UserContext.getCurrentUser();
        String storeId = authUser.getStoreId();
        // 获取商品skuId集合
        List<String> goodsSkuIds = updateStockList.stream().map(GoodsSkuStockDTO::getSkuId).collect(Collectors.toList());
        // 根据skuId集合查询商品信息
        List<GoodsSku> goodsSkuList = goodsSkuService.list(new LambdaQueryWrapper<GoodsSku>().in(GoodsSku::getId, goodsSkuIds).eq(GoodsSku::getStoreId, storeId));
        // 过滤不符合当前店铺的商品
        List<String> filterGoodsSkuIds = goodsSkuList.stream().map(GoodsSku::getId).collect(Collectors.toList());
        List<GoodsSkuStockDTO> collect = updateStockList.stream().filter(i -> filterGoodsSkuIds.contains(i.getSkuId())).collect(Collectors.toList());
        goodsSkuService.updateStocksBatch(collect);
    }
    @ApiOperation(value = "通过id获取商品信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "goodsId", value = "商品ID", required = true, paramType = "path"),
            @ApiImplicitParam(name = "skuId", value = "skuId", required = true, paramType = "path")
    })
    @GetMapping(value = "/sku/{goodsId}/{skuId}")
//    @PageViewPoint(type = PageViewEnum.SKU, id = "#id")
    public Map<String, Object> getSku(@NotNull(message = "商品ID不能为空") @PathVariable("goodsId") String goodsId,
                                                     @NotNull(message = "SKU ID不能为空") @PathVariable("skuId") String skuId) {
        try {
            // 读取选中的列表
            Map<String, Object> map = goodsSkuService.getGoodsSkuDetail(goodsId, skuId);
            return map;
        } catch (Exception se) {
            log.info(se.getMessage(), se);
            throw new ServiceException(ResultCode.GOODS_ERROR);
        }

    }

}
