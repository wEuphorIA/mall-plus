package com.jzo2o.mall.product.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jzo2o.mall.common.enums.ResultCode;
import com.jzo2o.mall.common.enums.ResultUtil;
import com.jzo2o.mall.common.exception.ServiceException;
import com.jzo2o.mall.common.model.ResultMessage;
import com.jzo2o.mall.product.model.domain.Goods;
import com.jzo2o.mall.product.model.domain.GoodsSku;
import com.jzo2o.mall.product.model.dto.GoodsDTO;
import com.jzo2o.mall.product.model.dto.GoodsSearchParamsDTO;
import com.jzo2o.mall.product.service.GoodsService;
import com.jzo2o.mall.product.service.GoodsSkuService;
import com.jzo2o.mall.search.model.domain.EsGoodsIndex;
import com.jzo2o.mall.search.model.dto.EsGoodsRelatedInfoDTO;
import com.jzo2o.mall.search.model.dto.EsGoodsSearchDTO;
import com.jzo2o.mall.search.service.EsGoodsSearchService;
import com.jzo2o.mysql.domain.PageVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

/**
 * 买家端,商品接口
 */
@Slf4j
@Api(tags = "买家端,商品接口")
@RestController
@RequestMapping("/goods/goods")
public class GoodsBuyerController {

    /**
     * 商品
     */
    @Autowired
    private GoodsService goodsService;
    /**
     * 商品SKU
     */
    @Autowired
    private GoodsSkuService goodsSkuService;

    /**
     * ES商品搜索
     */
    @Autowired
    private EsGoodsSearchService goodsSearchService;

//    @Autowired
//    private HotWordsService hotWordsService;

    @ApiOperation(value = "通过id获取商品信息")
    @ApiImplicitParam(name = "goodsId", value = "商品ID", required = true, paramType = "path", dataType = "Long")
    @GetMapping(value = "/get/{goodsId}")
    public ResultMessage<GoodsDTO> get(@NotNull(message = "商品ID不能为空") @PathVariable("goodsId") String id) {
        return ResultUtil.data(goodsService.getGoodsVO(id));
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
        } catch (ServiceException se) {
            log.info(se.getMsg(), se);
            throw se;
        } catch (Exception e) {
            log.error(ResultCode.GOODS_ERROR.message(), e);
            throw new ServiceException(ResultCode.GOODS_ERROR);
        }

    }
    @ApiOperation(value = "通过id获取秒杀商品信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "goodsId", value = "商品ID", required = true, paramType = "path"),
            @ApiImplicitParam(name = "skuId", value = "skuId", required = true, paramType = "path")
    })
    @GetMapping(value = "/seckillsku/{goodsId}/{skuId}")
//    @PageViewPoint(type = PageViewEnum.SKU, id = "#id")
    public Map<String, Object> getSeckillsku(@NotNull(message = "商品ID不能为空") @PathVariable("goodsId") String goodsId,
                                                     @NotNull(message = "SKU ID不能为空") @PathVariable("skuId") String skuId) {
        try {
            // 读取选中的列表
            Map<String, Object> map = goodsSkuService.getSeckillGoodsSkuDetail(goodsId, skuId);
            return map;
        } catch (ServiceException se) {
            log.info(se.getMsg(), se);
            throw se;
        } catch (Exception e) {
            log.error(ResultCode.GOODS_ERROR.message(), e);
            throw new ServiceException(ResultCode.GOODS_ERROR);
        }

    }

    @ApiOperation(value = "获取商品分页列表")
    @GetMapping
    public IPage<Goods> getByPage(GoodsSearchParamsDTO goodsSearchParams) {
        IPage<Goods> goodsIPage = goodsService.queryByParams(goodsSearchParams);
        return goodsIPage;
    }

    @ApiOperation(value = "获取商品sku列表")
    @GetMapping("/sku")
    public List<GoodsSku> getSkuByPage(GoodsSearchParamsDTO goodsSearchParams) {
        List<GoodsSku> goodsSkuByList = goodsSkuService.getGoodsSkuByList(goodsSearchParams);
        return goodsSkuByList;
    }

    @ApiOperation(value = "从ES中获取商品信息")
    @GetMapping("/es")
    public Page<EsGoodsIndex> getGoodsByPageFromEs(EsGoodsSearchDTO goodsSearchParams, PageVO pageVO) {
        pageVO.setNotConvert(true);
        Page<EsGoodsIndex> esGoodsIndexPage = goodsSearchService.searchGoodsByPage(goodsSearchParams, pageVO);
        return esGoodsIndexPage;
    }

    @ApiOperation(value = "从ES中获取相关商品品牌名称，分类名称及属性")
    @GetMapping("/es/related")
    public EsGoodsRelatedInfoDTO getGoodsRelatedByPageFromEs(EsGoodsSearchDTO goodsSearchParams, PageVO pageVO) {
        pageVO.setNotConvert(true);
        EsGoodsRelatedInfoDTO selector = goodsSearchService.getSelector(goodsSearchParams, pageVO);
        return selector;
    }

//    @ApiOperation(value = "获取搜索热词")
//    @GetMapping("/hot-words")
//    public ResultMessage<List<String>> getGoodsHotWords(Integer count) {
//        List<String> hotWords = hotWordsService.getHotWords(count);
//        return ResultUtil.data(hotWords);
//    }

}
