package com.jzo2o.mall.search.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jzo2o.mall.search.model.domain.EsGoodsIndex;
import com.jzo2o.mall.search.model.dto.EsGoodsRelatedInfoDTO;
import com.jzo2o.mall.search.model.dto.EsGoodsSearchDTO;
import com.jzo2o.mysql.domain.PageVO;

import java.util.List;

/**
 * ES商品搜索业务层
 **/
public interface EsGoodsSearchService {

//    /**
//     * 商品搜索
//     *
//     * @param searchDTO 搜索参数
//     * @param pageVo    分页参数
//     * @return 搜索结果
//     */
//    SearchPage<EsGoodsIndex> searchGoods(EsGoodsSearchDTO searchDTO, PageVO pageVo);

    /**
     * 商品搜索
     *
     * @param searchDTO 搜索参数
     * @param pageVo    分页参数
     * @return 搜索结果
     */
    Page<EsGoodsIndex> searchGoodsByPage(EsGoodsSearchDTO searchDTO, PageVO pageVo);

    /**
     * 获取筛选器
     *
     * @param goodsSearch 搜索条件
     * @param pageVo      分页参数
     * @return ES商品关联
     */
    EsGoodsRelatedInfoDTO getSelector(EsGoodsSearchDTO goodsSearch, PageVO pageVo);

//    /**
//     * 根据SkuID列表获取ES商品
//     *
//     * @param skuIds SkuId列表
//     * @return ES商品列表
//     */
//    List<EsGoodsIndex> getEsGoodsBySkuIds(List<String> skuIds, PageVO pageVo);

    /**
     * 根据id获取商品索引
     *
     * @param id 商品skuId
     * @return 商品索引
     */
    EsGoodsIndex getEsGoodsById(String id);
    /**
     * 根据spu id获取所有sku索引
     *
     * @param goodsId 商品spuId
     * @return 商品索引
     */
    List<EsGoodsIndex> getEsGoodsByGoodsId(String goodsId);
}
