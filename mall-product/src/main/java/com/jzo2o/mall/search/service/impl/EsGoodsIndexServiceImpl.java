package com.jzo2o.mall.search.service.impl;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jzo2o.common.utils.CollUtils;
import com.jzo2o.es.core.ElasticSearchTemplate;
import com.jzo2o.mall.common.enums.ResultCode;
import com.jzo2o.mall.common.exception.ServiceException;
import com.jzo2o.mall.product.model.domain.Brand;
import com.jzo2o.mall.product.model.domain.Category;
import com.jzo2o.mall.product.model.domain.Goods;
import com.jzo2o.mall.product.model.domain.GoodsSku;
import com.jzo2o.mall.product.model.dto.GoodsParamsDTO;
import com.jzo2o.mall.product.model.dto.GoodsSearchParamsDTO;
import com.jzo2o.mall.common.enums.GoodsAuthEnum;
import com.jzo2o.mall.common.enums.GoodsStatusEnum;
import com.jzo2o.mall.product.service.BrandService;
import com.jzo2o.mall.product.service.CategoryService;
import com.jzo2o.mall.product.service.GoodsService;
import com.jzo2o.mall.product.service.GoodsSkuService;
import com.jzo2o.mall.promotion.service.PromotionService;
import com.jzo2o.mall.search.model.domain.EsGoodsIndex;
import com.jzo2o.mall.search.service.EsGoodsIndexService;
import com.jzo2o.mall.search.service.EsGoodsSearchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

/**
 * @author Mr.M
 * @version 1.0
 * @description TODO
 * @date 2024/4/23 16:37
 */
@Slf4j
@Service
public class EsGoodsIndexServiceImpl implements EsGoodsIndexService {

    private static final String INDEX_NAME = "mall_goods";

    private static final int BATCH_SIZE = 10;

    @Resource
    private GoodsSkuService goodsSkuService;
    @Resource
    private GoodsService goodsService;
    @Resource
    private PromotionService promotionService;

    @Resource
    private ElasticSearchTemplate elasticSearchTemplate;

    @Resource
    private CategoryService categoryService;


    @Resource
    private BrandService brandService;

    @Resource
    private EsGoodsSearchService esGoodsSearchService;

    @Override
    public void updateGoodsIndex(String goodsId, GoodsAuthEnum goodsAuthEnum, GoodsStatusEnum goodsStatusEnum) {

        log.info("更新商品索引,商品id：{}", goodsId);
        if (goodsAuthEnum.equals(GoodsAuthEnum.PASS) && goodsStatusEnum.equals(GoodsStatusEnum.UPPER)) {
            //查询商品
            Goods goods = goodsService.getById(goodsId);
            for (int i = 1; ; i++) {
                //如果商品通过审核&&并且已上架
                GoodsSearchParamsDTO searchParams = new GoodsSearchParamsDTO();
                searchParams.setGoodsId(goodsId);
                searchParams.setPageNumber(i);
                searchParams.setPageSize(BATCH_SIZE);
                searchParams.setGeQuantity(0);
                IPage<GoodsSku> goodsSkuByPage = this.goodsSkuService.getGoodsSkuByPage(searchParams);
                if (goodsSkuByPage == null || goodsSkuByPage.getRecords().isEmpty()) {
                    break;
                }
                this.generatorGoodsIndex(goods, goodsSkuByPage.getRecords());
            }

        } else {
            //查询该 商品spu下的所有sku索引
            List<EsGoodsIndex> esGoodsByGoodsId = esGoodsSearchService.getEsGoodsByGoodsId(goodsId);
            //从esGoodsByGoodsId中提取出skuid,用stream流完成
            List<String> skuIds = esGoodsByGoodsId.stream().map(EsGoodsIndex::getId).collect(ArrayList::new, ArrayList::add, ArrayList::addAll);

            //如果商品状态值不支持es搜索，那么将商品信息做下架处理
//            List<String> ids = new ArrayList<>();
//            for (GoodsSku goodsSku : goodsSkuByPage.getRecords()) {
////                    EsGoodsIndex esGoodsOld = findById(goodsSku.getId());
////                    if (esGoodsOld != null) {
////                        deleteIndexById(goodsSku.getId());
////                    }
//                ids.add(goodsSku.getId());
//            }
            if (CollUtils.isNotEmpty(skuIds)) {
                //批量删除索引
                deleteIndexByIds(skuIds);
            }
        }

    }

    /**
     * 生成商品索引
     *
     * @param goods        商品信息
     * @param goodsSkuList 商品sku信息
     */
    private void generatorGoodsIndex(Goods goods, List<GoodsSku> goodsSkuList) {
        int skuSource = 100;
        List<EsGoodsIndex> esGoodsIndices = new ArrayList<>();
        for (GoodsSku goodsSku : goodsSkuList) {
            EsGoodsIndex goodsIndex = this.settingUpGoodsIndexData(goods, goodsSku);
            skuSource--;
            if (skuSource <= 0) {
                skuSource = 1;
            }
            goodsIndex.setSkuSource(skuSource);
            log.info("goodsSku：{}", goodsSku);
            log.info("生成商品索引 {}", goodsIndex);
            esGoodsIndices.add(goodsIndex);
        }
        //批量更新索引
        updateBulkIndex(esGoodsIndices);
//        //删除索引
//        this.goodsIndexService.deleteIndex(MapUtil.builder(new HashMap<String, Object>()).put("goodsId", goods.getId()).build());
//        //添加索引
//        this.goodsIndexService.addIndex(esGoodsIndices);
    }

    /**
     * 生成商品索引
     *
     * @param goods    商品信息
     * @param goodsSku 商品sku信息
     */
    private EsGoodsIndex settingUpGoodsIndexData(Goods goods, GoodsSku goodsSku) {
        EsGoodsIndex goodsIndex = null;
        if (goods.getParams() != null && !goods.getParams().isEmpty()) {
            List<GoodsParamsDTO> goodsParamDTOS = JSONUtil.toList(goods.getParams(), GoodsParamsDTO.class);
            goodsIndex = new EsGoodsIndex(goodsSku, goodsParamDTOS);
        } else {
            goodsIndex = new EsGoodsIndex(goodsSku);
        }
        goodsIndex.setAuthFlag(goods.getAuthFlag());
        goodsIndex.setMarketEnable(goods.getMarketEnable());
        this.settingUpGoodsIndexOtherParam(goodsIndex);
        return goodsIndex;
    }

    /**
     * 设置商品索引的其他参数（非商品自带）
     *
     * @param goodsIndex 商品索引信息
     */
    private void settingUpGoodsIndexOtherParam(EsGoodsIndex goodsIndex) {
        List<Category> categories = categoryService.listByIdsOrderByLevel(Arrays.asList(goodsIndex.getCategoryPath()));
        if (!categories.isEmpty()) {
            String[] categoryPathArray = goodsIndex.getCategoryPath();
            //将categoryPath数组转成逗号分隔的字符串
            String categoryPathArrayJoin = ArrayUtil.join(categoryPathArray, ",");
            goodsIndex.setCategoryNamePath(ArrayUtil.join(categories.stream().map(Category::getName).toArray(), ",")+"|"+categoryPathArrayJoin);
        }
        Brand brand = brandService.getById(goodsIndex.getBrandId());
        if (brand != null) {
            goodsIndex.setBrandName(brand.getName());
            goodsIndex.setBrandUrl(brand.getLogo());
        }
//        if (goodsIndex.getStoreCategoryPath() != null && CharSequenceUtil.isNotEmpty(goodsIndex.getStoreCategoryPath())) {
//            List<StoreGoodsLabel> storeGoodsLabels = storeGoodsLabelService.listByStoreIds(Arrays.asList(goodsIndex.getStoreCategoryPath().split(",")));
//            if (!storeGoodsLabels.isEmpty()) {
//                goodsIndex.setStoreCategoryNamePath(ArrayUtil.join(storeGoodsLabels.stream().map(StoreGoodsLabel::getLabelName).toArray(), ","));
//            }
//        }

//        if (goodsIndex.getOriginPromotionMap() == null || goodsIndex.getOriginPromotionMap().isEmpty()) {
//            Map<String, Object> goodsCurrentPromotionMap = promotionService.getGoodsSkuPromotionMap(goodsIndex.getStoreId(), goodsIndex.getId());
//            goodsIndex.setPromotionMapJson(JSONUtil.toJsonStr(goodsCurrentPromotionMap));
//        }
    }

    /**
     * 获取重置的商品索引
     *
     * @param goodsSku       商品sku信息
     * @param goodsParamDTOS 商品参数
     * @return 商品索引
     */
    @Override
    public EsGoodsIndex getResetEsGoodsIndex(GoodsSku goodsSku, List<GoodsParamsDTO> goodsParamDTOS) {
        EsGoodsIndex index = new EsGoodsIndex(goodsSku, goodsParamDTOS);
        //获取活动信息
        Map<String, Object> goodsCurrentPromotionMap = promotionService.getGoodsSkuPromotionMap(index.getStoreId(), index.getId());
        //写入促销信息
//        index.setPromotionMapJson(JSONUtil.toJsonStr(goodsCurrentPromotionMap));
        //更新索引
        updateIndex(index);
//        //发送mq消息
//        String destination = rocketmqCustomProperties.getGoodsTopic() + ":" + GoodsTagsEnum.RESET_GOODS_INDEX.name();
//        rocketMQTemplate.asyncSend(destination, JSONUtil.toJsonStr(Collections.singletonList(index)), RocketmqSendCallbackBuilder.commonCallback());

        return index;
    }
    @Override
    public void addIndex(EsGoodsIndex goods) {
        Boolean mall_goods = elasticSearchTemplate.opsForDoc().insert(INDEX_NAME, goods);
        if (mall_goods) {
            log.info("商品添加成功");
        } else {
            log.info("商品添加失败");
            throw new ServiceException(ResultCode.ELASTICSEARCH_INDEX_INIT_ERROR);
        }

    }

    @Override
    public void updateIndex(EsGoodsIndex goods) {

        Boolean aBoolean = elasticSearchTemplate.opsForDoc().updateById(INDEX_NAME, goods);
        if (aBoolean) {
            log.info("商品更新成功");
        } else {
            log.info("商品更新失败");
            throw new ServiceException(ResultCode.ELASTICSEARCH_INDEX_INIT_ERROR);
        }
    }

    @Override
    public void updateBulkIndex(List<EsGoodsIndex> goodsIndices) {
        Boolean aBoolean = elasticSearchTemplate.opsForDoc().batchUpsert(INDEX_NAME, goodsIndices);
        if (aBoolean) {
            log.info("商品批量更新成功");
        } else {
            log.info("商品批量更新失败");
            throw new ServiceException(ResultCode.ELASTICSEARCH_INDEX_INIT_ERROR);
        }
    }

    @Override
    public void deleteIndexById(String id) {
        log.info("商品删除:{}", id);
        Boolean aBoolean = elasticSearchTemplate.opsForDoc().deleteById(INDEX_NAME, id);
//        if (aBoolean) {
//            log.info("商品删除成功");
//        } else {
//            log.info("商品删除失败");
//            throw new ServiceException(ResultCode.ELASTICSEARCH_INDEX_INIT_ERROR);
//        }
    }

    @Override
    public void deleteIndexByIds(List<String> ids) {
        log.info("商品批量删除：{}", ids);
        Boolean aBoolean = elasticSearchTemplate.opsForDoc().batchDelete(INDEX_NAME, ids);
        //        if (aBoolean) {
//            log.info("商品删除成功");
//        } else {
//            log.info("商品删除失败");
//            throw new ServiceException(ResultCode.ELASTICSEARCH_INDEX_INIT_ERROR);
//        }
    }

    @Override
    public EsGoodsIndex findById(String id) {
        EsGoodsIndex esGoodsIndex = elasticSearchTemplate.opsForDoc().findById(INDEX_NAME, id, EsGoodsIndex.class);
        return esGoodsIndex;
    }
}
