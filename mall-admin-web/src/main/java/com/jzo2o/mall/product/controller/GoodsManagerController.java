package com.jzo2o.mall.product.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jzo2o.mall.common.enums.ResultCode;
import com.jzo2o.mall.common.exception.ServiceException;
import com.jzo2o.mall.product.model.domain.Goods;
import com.jzo2o.mall.product.model.domain.GoodsSku;
import com.jzo2o.mall.product.model.dto.GoodsDTO;
import com.jzo2o.mall.product.model.dto.GoodsSearchParamsDTO;
import com.jzo2o.mall.common.enums.GoodsAuthEnum;
import com.jzo2o.mall.common.enums.GoodsStatusEnum;
import com.jzo2o.mall.product.service.GoodsService;
import com.jzo2o.mall.product.service.GoodsSkuService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotEmpty;
import java.util.Arrays;
import java.util.List;

/**
 * 管理端,商品管理接口
 */
@RestController
@Api(tags = "管理端,商品管理接口")
@RequestMapping("/goods/goods")
public class GoodsManagerController {
    /**
     * 商品
     */
    @Autowired
    private GoodsService goodsService;
    /**
     * 规格商品
     */
    @Autowired
    private GoodsSkuService goodsSkuService;

    @ApiOperation(value = "分页获取")
    @GetMapping(value = "/list")
    public IPage<Goods> getByPage(GoodsSearchParamsDTO goodsSearchParams) {
        IPage<Goods> goodsIPage = goodsService.queryByParams(goodsSearchParams);
        return goodsIPage;
    }

    @ApiOperation(value = "分页获取商品列表")
    @GetMapping(value = "/sku/list")
    public IPage<GoodsSku> getSkuByPage(GoodsSearchParamsDTO goodsSearchParams) {
        IPage<GoodsSku> goodsSkuByPage = goodsSkuService.getGoodsSkuByPage(goodsSearchParams);
        return goodsSkuByPage;
    }

    @ApiOperation(value = "分页获取待审核商品")
    @GetMapping(value = "/auth/list")
    public IPage<Goods> getAuthPage(GoodsSearchParamsDTO goodsSearchParams) {
        goodsSearchParams.setAuthFlag(GoodsAuthEnum.TOBEAUDITED.name());
        IPage<Goods> goodsIPage = goodsService.queryByParams(goodsSearchParams);
        return goodsIPage;
    }

    @ApiOperation(value = "管理员下架商品", notes = "管理员下架商品时使用")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "goodsId", value = "商品ID", required = true, paramType = "query", allowMultiple = true),
            @ApiImplicitParam(name = "reason", value = "下架理由", required = true, paramType = "query")
    })
    @PutMapping(value = "/{goodsId}/under")
    public void underGoods(@PathVariable String goodsId, @NotEmpty(message = "下架原因不能为空") @RequestParam String reason) {
        List<String> goodsIds = Arrays.asList(goodsId.split(","));
        Boolean result = goodsService.managerUpdateGoodsMarketAble(goodsIds, GoodsStatusEnum.DOWN, reason);
        if (result) {
            return ;
        }
        throw new ServiceException(ResultCode.GOODS_UNDER_ERROR);
    }

    @ApiOperation(value = "管理员审核商品", notes = "管理员审核商品")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "goodsIds", value = "商品ID", required = true, paramType = "path", allowMultiple = true, dataType = "int"),
            @ApiImplicitParam(name = "authFlag", value = "审核结果", required = true, paramType = "query", dataType = "string")
    })
    @PutMapping(value = "{goodsIds}/auth")
    public void auth(@PathVariable List<String> goodsIds, @RequestParam String authFlag) {
        //校验商品是否存在
        boolean result = goodsService.auditGoods(goodsIds, GoodsAuthEnum.valueOf(authFlag));
        if (result) {
            return ;
        }
        throw new ServiceException(ResultCode.GOODS_AUTH_ERROR);
    }


    @ApiOperation(value = "管理员上架商品", notes = "管理员上架商品时使用")
    @PutMapping(value = "/{goodsId}/up")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "goodsId", value = "商品ID", required = true, allowMultiple = true)
    })
    public void unpGoods(@PathVariable List<String> goodsId) {
        Boolean result = goodsService.updateGoodsMarketAble(goodsId, GoodsStatusEnum.UPPER, "");
        if (result) {
            return ;
        }
        throw new ServiceException(ResultCode.GOODS_UPPER_ERROR);
    }


    @ApiOperation(value = "通过id获取商品详情")
    @GetMapping(value = "/get/{id}")
    public GoodsDTO get(@PathVariable String id) {
        GoodsDTO goods = goodsService.getGoodsVO(id);
        return goods;
    }

}
