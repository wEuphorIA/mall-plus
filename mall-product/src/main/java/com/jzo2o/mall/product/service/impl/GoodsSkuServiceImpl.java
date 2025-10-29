package com.jzo2o.mall.product.service.impl;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jzo2o.common.model.CurrentUser;
import com.jzo2o.common.utils.DateUtils;
import com.jzo2o.common.utils.UserContext;
import com.jzo2o.mall.common.cache.CachePrefix;
import com.jzo2o.mall.common.constants.MallMqConstants;
import com.jzo2o.mall.common.enums.GoodsSalesModeEnum;
import com.jzo2o.mall.common.enums.ResultCode;
import com.jzo2o.mall.common.exception.ServiceException;
import com.jzo2o.mall.common.model.message.OrderStatusMessage;
import com.jzo2o.mall.common.model.message.StockUpdateMessage;
import com.jzo2o.mall.common.utils.SnowFlake;
import com.jzo2o.mall.product.event.StockUpdateExecute;
import com.jzo2o.mall.product.mapper.GoodsSkuMapper;
import com.jzo2o.mall.product.model.GoodsSkuBuilder;
import com.jzo2o.mall.product.model.domain.Goods;
import com.jzo2o.mall.product.model.domain.GoodsGallery;
import com.jzo2o.mall.product.model.domain.GoodsSku;
import com.jzo2o.mall.product.model.dto.*;
import com.jzo2o.mall.common.enums.GoodsAuthEnum;
import com.jzo2o.mall.common.enums.GoodsStatusEnum;
import com.jzo2o.mall.promotion.model.enums.PromotionTypeEnum;
import com.jzo2o.mall.product.service.GoodsGalleryService;
import com.jzo2o.mall.product.service.GoodsService;
import com.jzo2o.mall.product.service.GoodsSkuService;
import com.jzo2o.mall.promotion.model.domain.Coupon;
import com.jzo2o.mall.promotion.model.domain.PromotionGoods;
import com.jzo2o.mall.promotion.model.dto.PromotionGoodsSearchParams;
import com.jzo2o.mall.promotion.model.dto.SeckillDTO;
import com.jzo2o.mall.promotion.model.dto.SeckillGoodsDTO;
import com.jzo2o.mall.promotion.model.dto.SeckillTimelineDTO;
import com.jzo2o.mall.promotion.model.enums.CouponGetEnum;
import com.jzo2o.mall.promotion.service.*;
import com.jzo2o.mall.search.model.domain.EsGoodsIndex;
import com.jzo2o.mall.search.service.EsGoodsIndexService;
import com.jzo2o.mysql.domain.BaseEntity;
import com.jzo2o.mysql.utils.PageUtils;
import com.jzo2o.rabbitmq.client.RabbitClient;
import com.jzo2o.redis.helper.Cache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 商品sku业务层实现
 */
@Service
public class GoodsSkuServiceImpl extends ServiceImpl<GoodsSkuMapper, GoodsSku> implements GoodsSkuService {

    /**
     * 缓存
     */
    @Autowired
    private Cache cache;
    /**
     * 分类
     */
    @Autowired
    private MemberCouponService memberCouponService;
    /**
     * 商品相册
     */
    @Autowired
    private GoodsGalleryService goodsGalleryService;

    @Autowired
    private StockUpdateExecute stockUpdateExecute;

    @Autowired
    private PromotionService promotionService;
//    /**
//     * rocketMq
//     */
//    @Autowired
//    private RocketMQTemplate rocketMQTemplate;
//    /**
//     * rocketMq配置
//     */
//    @Autowired
//    private RocketmqCustomProperties rocketmqCustomProperties;
    /**
     * 商品
     */
    @Autowired
    private GoodsService goodsService;

    @Autowired
    private RabbitClient rabbitClient;
    /**
     * 商品索引
     */
    @Autowired
    private EsGoodsIndexService goodsIndexService;

    @Autowired
    private PromotionGoodsService promotionGoodsService;

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    @Autowired
    private SeckillApplyService seckillApplyService;

    @Autowired
    private SeckillService seckillService;

//    @Autowired
//    private WholesaleService wholesaleService;

    @Autowired
    private CouponService couponService;

    @Autowired
    private DefaultRedisScript<Boolean> quantityScript;

    @Autowired
    private RedisTemplate redisTemplate;
//
//    @Autowired
//    private List<SalesModelRender> salesModelRenders;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(Goods goods, GoodsOperationDTO goodsOperationDTO) {
        // 是否存在规格
        if (goodsOperationDTO.getSkuList() == null || goodsOperationDTO.getSkuList().isEmpty()) {
            throw new ServiceException(ResultCode.MUST_HAVE_GOODS_SKU);
        }
        // 检查是否需要生成索引
        List<GoodsSku> goodsSkus = GoodsSkuBuilder.buildBatch(goods, goodsOperationDTO.getSkuList());
//        renderGoodsSkuList(goodsSkus, goodsOperationDTO);
        for (GoodsSku goodsSku : goodsSkus) {
            //设置sku的图片信息
            this.renderImages(goodsSku, goodsOperationDTO.getGoodsGalleryList());
        }
        if (!goodsSkus.isEmpty()) {
            this.saveOrUpdateBatch(goodsSkus);
            this.updateGoodsStock(goodsSkus);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Goods goods, GoodsOperationDTO goodsOperationDTO) {
        // 是否存在规格
        if (goodsOperationDTO.getSkuList() == null || goodsOperationDTO.getSkuList().isEmpty()) {
            throw new ServiceException(ResultCode.MUST_HAVE_GOODS_SKU);
        }
        List<GoodsSku> skuList;
        //删除旧的sku信息
        if (Boolean.TRUE.equals(goodsOperationDTO.getRegeneratorSkuFlag())) {
            skuList = GoodsSkuBuilder.buildBatch(goods, goodsOperationDTO.getSkuList());
            renderGoodsSkuList(skuList, goodsOperationDTO);
            List<GoodsSkuDTO> goodsListByGoodsId = getGoodsListByGoodsId(goods.getId());
            List<String> oldSkuIds = new ArrayList<>();
            //删除旧索引
            for (GoodsSkuDTO goodsSkuVO : goodsListByGoodsId) {
                oldSkuIds.add(goodsSkuVO.getId());
                cache.remove(GoodsSkuService.getCacheKeys(goodsSkuVO.getId()));
            }

            //删除sku相册
            goodsGalleryService.removeByGoodsId(goods.getId());

//            //发送mq消息
//            String destination = rocketmqCustomProperties.getGoodsTopic() + ":" + GoodsTagsEnum.SKU_DELETE.name();
//            rocketMQTemplate.asyncSend(destination, JSONUtil.toJsonStr(oldSkuIds),
//                    RocketmqSendCallbackBuilder.commonCallback());
        } else {
            skuList = new ArrayList<>();
            for (Map<String, Object> map : goodsOperationDTO.getSkuList()) {
                GoodsSku sku = GoodsSkuBuilder.build(goods, map);
                renderGoodsSku(sku, goodsOperationDTO);
                skuList.add(sku);
                //如果商品状态值不对，则es索引移除
//                if (goods.getAuthFlag().equals(GoodsAuthEnum.PASS.name()) && goods.getMarketEnable().equals(GoodsStatusEnum.UPPER.name())) {
//                    goodsIndexService.deleteIndexById(sku.getId());
//                }
                this.clearCache(sku.getId());
            }
        }
        if (!skuList.isEmpty()) {
            LambdaQueryWrapper<GoodsSku> unnecessarySkuIdsQuery = new LambdaQueryWrapper<>();
            unnecessarySkuIdsQuery.eq(GoodsSku::getGoodsId, goods.getId());
            unnecessarySkuIdsQuery.notIn(GoodsSku::getId,
                    skuList.stream().map(BaseEntity::getId).collect(Collectors.toList()));
            this.remove(unnecessarySkuIdsQuery);
            this.saveOrUpdateBatch(skuList);
            this.updateGoodsStock(skuList);
        }
    }

    /**
     * 更新商品sku
     *
     * @param goodsSku sku信息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(GoodsSku goodsSku) {
        this.updateById(goodsSku);
        cache.remove(GoodsSkuService.getCacheKeys(goodsSku.getId()));
        cache.put(GoodsSkuService.getCacheKeys(goodsSku.getId()), goodsSku);
    }


    /**
     * 清除sku缓存
     *
     * @param skuId skuID
     */
    @Override
    public void clearCache(String skuId) {
        cache.remove(GoodsSkuService.getCacheKeys(skuId));
    }

    @Override
    public GoodsSku getGoodsSkuByIdFromCache(String id) {
        //获取缓存中的sku
        GoodsSku goodsSku = (GoodsSku) cache.get(GoodsSkuService.getCacheKeys(id));
        //如果缓存中没有信息，则查询数据库，然后写入缓存
        if (goodsSku == null) {
            goodsSku = this.getById(id);
            if (goodsSku == null) {
                return null;
            }
            cache.put(GoodsSkuService.getCacheKeys(id), goodsSku);
        }

        //获取商品库存
        Integer integer = (Integer) cache.get(GoodsSkuService.getStockCacheKey(id));

        //库存不为空,库存与缓存中不一致
        if (integer != null && !goodsSku.getQuantity().equals(integer)) {
            //写入最新的库存信息
            goodsSku.setQuantity(integer);
            cache.put(GoodsSkuService.getCacheKeys(goodsSku.getId()), goodsSku);
        }
        return goodsSku;
    }

    @Override
    public GoodsSku getCanPromotionGoodsSkuByIdFromCache(String skuId) {
        GoodsSku goodsSku = this.getGoodsSkuByIdFromCache(skuId);
        if (goodsSku != null && GoodsSalesModeEnum.WHOLESALE.name().equals(goodsSku.getSalesModel())) {
            throw new ServiceException(ResultCode.PROMOTION_GOODS_DO_NOT_JOIN_WHOLESALE, goodsSku.getGoodsName());
        }
        return goodsSku;
    }

    @Override
    public Map<String, Object> getGoodsSkuDetail(String goodsId, String skuId) {
        Map<String, Object> map = new HashMap<>(16);
        //获取商品VO
        GoodsDTO goodsVO = goodsService.getGoodsVO(goodsId);
        //如果skuid为空，则使用商品VO中sku信息获取
        if (CharSequenceUtil.isEmpty(skuId) || "undefined".equals(skuId)) {
            skuId = goodsVO.getSkuList().get(0).getId();
        }
        //从缓存拿商品Sku
        GoodsSku goodsSku = this.getGoodsSkuByIdFromCache(skuId);
        //如果使用商品ID无法查询SKU则返回错误
        if (goodsVO == null || goodsSku == null) {
//            //发送mq消息
//            String destination = rocketmqCustomProperties.getGoodsTopic() + ":" + GoodsTagsEnum.GOODS_DELETE.name();
//            rocketMQTemplate.asyncSend(destination, JSONUtil.toJsonStr(Collections.singletonList(goodsId)),
//                    RocketmqSendCallbackBuilder.commonCallback());
            throw new ServiceException(ResultCode.GOODS_NOT_EXIST);
        }

        //商品下架||商品未审核通过||商品删除，则提示：商品已下架
        if (GoodsStatusEnum.DOWN.name().equals(goodsVO.getMarketEnable()) || !GoodsAuthEnum.PASS.name().equals(goodsVO.getAuthFlag()) || Boolean.TRUE.equals(goodsVO.getDeleteFlag())) {
//            String destination = rocketmqCustomProperties.getGoodsTopic() + ":" + GoodsTagsEnum.GOODS_DELETE.name();
//            rocketMQTemplate.asyncSend(destination, JSONUtil.toJsonStr(Collections.singletonList(goodsId)),
//                    RocketmqSendCallbackBuilder.commonCallback());
            throw new ServiceException(ResultCode.GOODS_NOT_EXIST);
        }

        //获取当前商品的索引信息
        EsGoodsIndex goodsIndex = goodsIndexService.findById(skuId);
//        if (goodsIndex == null) {
//            //goodsIndex = goodsIndexService.getResetEsGoodsIndex(goodsSku, goodsVO.getGoodsParamsDTOList());
//            throw new ServiceException(ResultCode.GOODS_NOT_EXIST);
//        }

        //商品规格
        GoodsSkuDTO goodsSkuDetail = this.getGoodsSkuVO(goodsSku);
        //获取该商品在店铺及平台的促销信息
        Map<String, Object> promotionMap = promotionService.getGoodsSkuPromotionMap(goodsIndex.getStoreId(), goodsIndex.getId());
//        goodsIndex.setPromotionMapJson(JSONUtil.toJsonStr(goodsCurrentPromotionMap));

//        Map<String, Object> promotionMap = goodsIndex.getPromotionMap();
        CurrentUser currentUser = UserContext.getCurrentUser();
        //设置当前商品的促销价格
        if (promotionMap != null && !promotionMap.isEmpty()) {
            promotionMap = promotionMap.entrySet().stream().parallel().filter(i -> {
                JSONObject jsonObject = JSONUtil.parseObj(i.getValue());
                if (i.getKey().contains(PromotionTypeEnum.COUPON.name()) && currentUser != null) {
                    //couponLimitNum领取限制
                    Integer couponLimitNum = jsonObject.getInt("couponLimitNum");
                    Coupon coupon = couponService.getById(jsonObject.getStr("id"));
                    if (coupon == null || (coupon.getPublishNum() != 0 && coupon.getReceivedNum() >= coupon.getPublishNum())) {
                        return false;
                    }
                    if (couponLimitNum > 0) {
                        Long count = memberCouponService.getMemberCouponNum(currentUser.getIdString(), jsonObject.getStr(
                                "id"));
                        if (count >= couponLimitNum) {
                            return false;
                        }
                    }
                }
                // 过滤活动赠送优惠券和无效时间的活动
                return (jsonObject.get("getType") == null || jsonObject.get("getType", String.class).equals(CouponGetEnum.FREE.name())) && (jsonObject.get("startTime") != null && jsonObject.get("startTime", Date.class).getTime() <= System.currentTimeMillis()) && (jsonObject.get("endTime") == null || jsonObject.get("endTime", Date.class).getTime() >= System.currentTimeMillis());
            }).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

            //秒杀活动
            Optional<Map.Entry<String, Object>> containsPromotion =
                    promotionMap.entrySet().stream().filter(i -> i.getKey().contains(PromotionTypeEnum.SECKILL.name()) ).findFirst();
            if (containsPromotion.isPresent()) {
                JSONObject jsonObject = JSONUtil.parseObj(containsPromotion.get().getValue());
                PromotionGoodsSearchParams searchParams = new PromotionGoodsSearchParams();
                searchParams.setSkuId(skuId);
                searchParams.setPromotionId(jsonObject.get("id").toString());
                //获取活动商品
                PromotionGoods promotionsGoods = promotionGoodsService.getPromotionsGoods(searchParams);
                if (promotionsGoods != null && promotionsGoods.getPrice() != null) {
                    //设置商品促销价格
                    goodsSkuDetail.setPromotionFlag(true);
                    goodsSkuDetail.setPromotionPrice(promotionsGoods.getPrice());
                }
            } else {
                goodsSkuDetail.setPromotionFlag(false);
                goodsSkuDetail.setPromotionPrice(null);
            }

        }

//        goodsSkuDetail.setPromotionFlag(false);
//        goodsSkuDetail.setPromotionPrice(null);

        if (goodsSkuDetail.getGoodsGalleryList() == null || goodsSkuDetail.getGoodsGalleryList().isEmpty()) {
            goodsSkuDetail.setGoodsGalleryList(goodsVO.getGoodsGalleryList());
        } else if(goodsVO.getGoodsGalleryList()!=null) {
            goodsSkuDetail.getGoodsGalleryList().addAll(goodsVO.getGoodsGalleryList());
        }
        map.put("data", goodsSkuDetail);

        //获取分类
//        map.put("wholesaleList", GoodsSalesModeEnum.WHOLESALE.name().equals(goodsVO.getSalesModel()) ?
//                wholesaleService.findByGoodsId(goodsSkuDetail.getGoodsId()) : Collections.emptyList());
        map.put("wholesaleList", Collections.emptyList());
        map.put("categoryName", CharSequenceUtil.isNotEmpty(goodsIndex.getCategoryNamePath()) ?
                goodsIndex.getCategoryNamePath().split(",") : null);

        //获取规格信息
        map.put("specs", this.groupBySkuAndSpec(goodsVO.getSkuList()));
        map.put("promotionMap", promotionMap);
//        map.put("promotionMap", new HashMap<String,String>());

        //获取参数信息
        if (goodsVO.getGoodsParamsDTOList() != null && !goodsVO.getGoodsParamsDTOList().isEmpty()) {
            map.put("goodsParamsDTOList", goodsVO.getGoodsParamsDTOList());
        }

//        //记录用户足迹
//        if (currentUser != null) {
//            FootPrint footPrint = new FootPrint(currentUser.getId(), goodsIndex.getStoreId(), goodsId, skuId);
//            String destination = rocketmqCustomProperties.getGoodsTopic() + ":" + GoodsTagsEnum.VIEW_GOODS.name();
//            rocketMQTemplate.asyncSend(destination, footPrint, RocketmqSendCallbackBuilder.commonCallback());
//        }
        return map;
    }

    /**
     * 获取秒杀商品sku详情
     *
     * @param goodsId 商品ID
     * @param skuId   skuID
     * @return 商品sku详情
     */
    public Map<String, Object> getSeckillGoodsSkuDetail(String goodsId, String skuId){

        Map<String, Object> map = new HashMap<>(16);
        //获取商品VO
        GoodsDTO goodsVO = goodsService.getGoodsVO(goodsId);
        //如果skuid为空，则使用商品VO中sku信息获取
        if (CharSequenceUtil.isEmpty(skuId) || "undefined".equals(skuId)) {
            throw new ServiceException(ResultCode.GOODS_NOT_EXIST);
//            skuId = goodsVO.getSkuList().get(0).getId();
        }
        //从缓存拿商品Sku
        GoodsSku goodsSku = this.getGoodsSkuByIdFromCache(skuId);
        //如果使用商品ID无法查询SKU则返回错误
        if (goodsVO == null || goodsSku == null) {
            throw new ServiceException(ResultCode.GOODS_NOT_EXIST);
        }

        //商品下架||商品未审核通过||商品删除，则提示：商品已下架
        if (GoodsStatusEnum.DOWN.name().equals(goodsVO.getMarketEnable()) || !GoodsAuthEnum.PASS.name().equals(goodsVO.getAuthFlag()) || Boolean.TRUE.equals(goodsVO.getDeleteFlag())) {
            throw new ServiceException(ResultCode.GOODS_NOT_EXIST);
        }

        //获取当前商品的索引信息
        EsGoodsIndex goodsIndex = goodsIndexService.findById(skuId);
        if (goodsIndex == null) {
            throw new ServiceException(ResultCode.GOODS_NOT_EXIST);
        }

        //商品规格
        GoodsSkuDTO goodsSkuDetail = this.getGoodsSkuVO(goodsSku);
        //根据当前日期获取秒杀活动信息
        String now = DateUtils.format(LocalDateTime.now(), "yyyy-MM-dd");
        //从缓存中获取当天的秒杀活动列表
        List<SeckillTimelineDTO> seckillTimeline = seckillApplyService.getSeckillTimeline(now);
        //根据skuid从seckillTimeline取出秒杀活动信息
        SeckillTimelineDTO seckillTimelineDTO = seckillTimeline.stream().filter(i -> i.getSeckillGoodsList().stream().anyMatch(j -> j.getSkuId().equals(skuId))).findFirst().orElse(null);
        if(seckillTimelineDTO == null){
            throw new ServiceException(ResultCode.GOODS_NOT_EXIST);
        }
        //从seckillTimelineDTO中取出秒杀商品
        SeckillGoodsDTO seckillGoodsDTO = seckillTimelineDTO.getSeckillGoodsList().stream().filter(i -> i.getSkuId().equals(skuId)).findFirst().orElse(null);
        //设置商品促销价格
        goodsSkuDetail.setPromotionFlag(true);
        goodsSkuDetail.setPromotionPrice(seckillGoodsDTO.getPrice());
        goodsSkuDetail.setQuantity(seckillGoodsDTO.getQuantity());

        if (goodsSkuDetail.getGoodsGalleryList() == null || goodsSkuDetail.getGoodsGalleryList().isEmpty()) {
            goodsSkuDetail.setGoodsGalleryList(goodsVO.getGoodsGalleryList());
        } else if(goodsVO.getGoodsGalleryList()!=null) {
            goodsSkuDetail.getGoodsGalleryList().addAll(goodsVO.getGoodsGalleryList());
        }
        map.put("data", goodsSkuDetail);

        //获取分类
//        map.put("wholesaleList", GoodsSalesModeEnum.WHOLESALE.name().equals(goodsVO.getSalesModel()) ?
//                wholesaleService.findByGoodsId(goodsSkuDetail.getGoodsId()) : Collections.emptyList());
        map.put("wholesaleList", Collections.emptyList());
        map.put("categoryName", CharSequenceUtil.isNotEmpty(goodsIndex.getCategoryNamePath()) ?
                goodsIndex.getCategoryNamePath().split(",") : null);

        //获取规格信息
        map.put("specs", this.groupBySkuAndSpec(goodsVO.getSkuList()));

        //秒杀活动id
        String seckillId = seckillGoodsDTO.getSeckillId();
        //查询秒杀活动信息
        SeckillDTO seckillDetail = seckillService.getSeckillDetail(seckillId);
        //key是促销类型+促销id，value是促销活动对象(优惠券,秒杀)
        Map<String, Object> promotionMap = new HashMap<>();
        if (seckillDetail != null) {
            seckillDetail.setStartTime(seckillGoodsDTO.getPromotionGoods().getStartTime());
            seckillDetail.setEndTime(seckillGoodsDTO.getPromotionGoods().getEndTime());
            promotionMap.put(PromotionTypeEnum.SECKILL.name() + "-" + seckillDetail.getId(), seckillDetail);
        }
        map.put("promotionMap", promotionMap);

        //获取参数信息
        if (goodsVO.getGoodsParamsDTOList() != null && !goodsVO.getGoodsParamsDTOList().isEmpty()) {
            map.put("goodsParamsDTOList", goodsVO.getGoodsParamsDTOList());
        }

//        //记录用户足迹
//        if (currentUser != null) {
//            FootPrint footPrint = new FootPrint(currentUser.getId(), goodsIndex.getStoreId(), goodsId, skuId);
//            String destination = rocketmqCustomProperties.getGoodsTopic() + ":" + GoodsTagsEnum.VIEW_GOODS.name();
//            rocketMQTemplate.asyncSend(destination, footPrint, RocketmqSendCallbackBuilder.commonCallback());
//        }
        return map;
    }

    /**
     * 更新商品sku状态
     *
     * @param goods 商品信息(Id,MarketEnable/AuthFlag)
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateGoodsSkuStatus(Goods goods) {
        //更新商品下的所有sku的状态
        LambdaUpdateWrapper<GoodsSku> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(CharSequenceUtil.isNotEmpty(goods.getId()), GoodsSku::getGoodsId, goods.getId());
        updateWrapper.eq(CharSequenceUtil.isNotEmpty(goods.getStoreId()), GoodsSku::getStoreId, goods.getStoreId());
        updateWrapper.set(GoodsSku::getMarketEnable, goods.getMarketEnable());
        updateWrapper.set(GoodsSku::getAuthFlag, goods.getAuthFlag());
        updateWrapper.set(GoodsSku::getDeleteFlag, goods.getDeleteFlag());
        boolean update = this.update(updateWrapper);
        if (Boolean.TRUE.equals(update)) {
            List<GoodsSku> goodsSkus = this.getGoodsSkuListByGoodsId(goods.getId());
            for (GoodsSku sku : goodsSkus) {
                //删除sku缓存信息
                cache.remove(GoodsSkuService.getCacheKeys(sku.getId()));
                if (GoodsStatusEnum.UPPER.name().equals(goods.getMarketEnable()) && GoodsAuthEnum.PASS.name().equals(goods.getAuthFlag())) {
                    //添加sku缓存信息
                    cache.put(GoodsSkuService.getCacheKeys(sku.getId()), sku);
                }
            }

        }
    }

    /**
     * 更新商品sku状态根据店铺id
     *
     * @param storeId      店铺id
     * @param marketEnable 市场启用状态
     * @param authFlag     审核状态
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateGoodsSkuStatusByStoreId(String storeId, String marketEnable, String authFlag) {
        LambdaUpdateWrapper<GoodsSku> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(GoodsSku::getStoreId, storeId);
        updateWrapper.set(CharSequenceUtil.isNotEmpty(marketEnable), GoodsSku::getMarketEnable, marketEnable);
        updateWrapper.set(CharSequenceUtil.isNotEmpty(authFlag), GoodsSku::getAuthFlag, authFlag);
        boolean update = this.update(updateWrapper);
//        if (Boolean.TRUE.equals(update)) {
//            if (GoodsStatusEnum.UPPER.name().equals(marketEnable)) {
//                applicationEventPublisher.publishEvent(new TransactionCommitSendMQEvent("生成店铺商品",
//                        rocketmqCustomProperties.getGoodsTopic(), GoodsTagsEnum.GENERATOR_STORE_GOODS_INDEX.name(),
//                        storeId));
//            } else if (GoodsStatusEnum.DOWN.name().equals(marketEnable)) {
//                cache.vagueDel(CachePrefix.GOODS_SKU.getPrefix());
//                applicationEventPublisher.publishEvent(new TransactionCommitSendMQEvent("删除店铺商品",
//                        rocketmqCustomProperties.getGoodsTopic(), GoodsTagsEnum.STORE_GOODS_DELETE.name(), storeId));
//            }
//        }
    }

    @Override
    public List<GoodsSku> getGoodsSkuByIdFromCache(List<String> ids) {
        List<String> keys = new ArrayList<>();
        for (String id : ids) {
            keys.add(GoodsSkuService.getCacheKeys(id));
        }
        List<GoodsSku> list = cache.multiGet(keys);
        if (list == null || list.isEmpty()) {
            list = new ArrayList<>();
            List<GoodsSku> goodsSkus = listByIds(ids);
            for (GoodsSku skus : goodsSkus) {
                cache.put(GoodsSkuService.getCacheKeys(skus.getId()), skus);
                list.add(skus);
            }
        }
        return list;
    }

    @Override
    public List<GoodsSkuDTO> getGoodsListByGoodsId(String goodsId) {
        List<GoodsSku> list = this.list(new LambdaQueryWrapper<GoodsSku>().eq(GoodsSku::getGoodsId, goodsId));
        return this.getGoodsSkuVOList(list);
    }

    /**
     * 获取goodsId下所有的goodsSku
     *
     * @param goodsId 商品id
     * @return goodsSku列表
     */
    @Override
    public List<GoodsSku> getGoodsSkuListByGoodsId(String goodsId) {
        return this.list(new LambdaQueryWrapper<GoodsSku>().eq(GoodsSku::getGoodsId, goodsId));
    }

    @Override
    public List<GoodsSkuDTO> getGoodsSkuVOList(List<GoodsSku> list) {
        List<GoodsSkuDTO> goodsSkuDTOS = new ArrayList<>();
        for (GoodsSku goodsSku : list) {
            GoodsSkuDTO goodsSkuDTO = this.getGoodsSkuVO(goodsSku);
            goodsSkuDTOS.add(goodsSkuDTO);
        }
        return goodsSkuDTOS;
    }

    @Override
    public GoodsSkuDTO getGoodsSkuVO(GoodsSku goodsSku) {
        //初始化商品
        GoodsSkuDTO goodsSkuVO = new GoodsSkuDTO(goodsSku);
        //获取sku信息
        JSONObject jsonObject = JSONUtil.parseObj(goodsSku.getSpecs());
        //用于接受sku信息
        List<SpecValueDTO> specValueVOS = new ArrayList<>();
        //用于接受sku相册
        List<String> goodsGalleryList = new ArrayList<>();
        //循环提交的sku表单
        for (Map.Entry<String, Object> entry : jsonObject.entrySet()) {
            SpecValueDTO specValueDTO = new SpecValueDTO();
            if ("images".equals(entry.getKey())) {
                specValueDTO.setSpecName(entry.getKey());
                List<String> specImages = JSONUtil.toList(JSONUtil.parseArray(entry.getValue()),
                        String.class);
                specValueDTO.setSpecImage(specImages);
                goodsGalleryList = new ArrayList<>(specImages);
            } else {
                specValueDTO.setSpecName(entry.getKey());
                specValueDTO.setSpecValue(entry.getValue().toString());
            }
            specValueVOS.add(specValueDTO);
        }
        goodsSkuVO.setGoodsGalleryList(goodsGalleryList);
        goodsSkuVO.setSpecList(specValueVOS);
        return goodsSkuVO;
    }

    @Override
    public IPage<GoodsSku> getGoodsSkuByPage(GoodsSearchParamsDTO searchParams) {
        return this.page(PageUtils.initPage(searchParams), searchParams.queryWrapper());
    }

    @Override
    public IPage<GoodsSkuDTO> getGoodsSkuDTOByPage(Page<GoodsSkuDTO> page, Wrapper<GoodsSkuDTO> queryWrapper) {
        return this.baseMapper.queryByParams(page, queryWrapper);
    }

    /**
     * 列表查询商品sku信息
     *
     * @param searchParams 查询参数
     * @return 商品sku信息
     */
    @Override
    public List<GoodsSku> getGoodsSkuByList(GoodsSearchParamsDTO searchParams) {
        return this.list(searchParams.queryWrapper());
    }

    /**
     * 发送库存更新消息
     *
     * @param orderSn 订单号
     * @param isRollback 是否回滚库存
     */
    @Override
    public void sendUpdateStockMessage(String orderSn, boolean isRollback){
        //发送库存变更消息
        StockUpdateMessage stockUpdateMessage = new StockUpdateMessage(orderSn, isRollback);
        //发送消息
        rabbitClient.sendMsg(MallMqConstants.Exchanges.EXCHANGE_ORDER, MallMqConstants.RoutingKeys.PRODUCT_STOCK_UPDATE_ROUTINGKEY, stockUpdateMessage);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStocksBatch(List<GoodsSkuStockDTO> goodsSkuStockDTOS) {

        //库存key 和 扣减数量
        List<String> keys = new ArrayList<>();
        List<String> values = new ArrayList<>();

        for (GoodsSkuStockDTO goodsSkuStockDTO : goodsSkuStockDTOS) {
            keys.add(GoodsSkuService.getStockCacheKey(goodsSkuStockDTO.getSkuId()));
            values.add(String.valueOf(goodsSkuStockDTO.getQuantity()));
        }

        //库存扣除结果
        RedisSerializer<String> stringSerializer = new StringRedisSerializer();
        Boolean skuResult   = (Boolean) redisTemplate.execute(quantityScript,
                stringSerializer,stringSerializer,
                keys, values.toArray());

        if (Boolean.TRUE.equals(skuResult)) {
            //从goodsSkuStockDTOS中提取出List<GoodsSku>
            List<GoodsSku> goodsSkus = goodsSkuStockDTOS.stream().map(sku -> {
                GoodsSku goodsSku = new GoodsSku();
                goodsSku.setId(sku.getSkuId());
                goodsSku.setGoodsId(sku.getGoodsId());
                return goodsSku;
            }).collect(Collectors.toList());
            //成功之后，同步库存
            stockUpdateExecute.synchroDB(goodsSkus);

        }

    }

//    @Override
//    @Transactional(rollbackFor = Exception.class)
//    public void updateStocksBatch(List<GoodsSkuStockDTO> goodsSkuStockDTOS) {
//        //从goodsSkuStockDTOS中提取skuid
//        List<String> skuIds = goodsSkuStockDTOS.stream().map(GoodsSkuStockDTO::getSkuId).collect(Collectors.toList());
//        //根据skuid从数据库查询库存
//        List<GoodsSkuStockDTO> goodsSkuStockList = this.baseMapper.queryStocks(GoodsSearchParamsDTO.builder().ids(skuIds).build().queryWrapper());
//        //根据商品id进行分组
//        Map<String, List<GoodsSkuStockDTO>> groupByGoodsIds = goodsSkuStockList.stream().collect(Collectors.groupingBy(GoodsSkuStockDTO::getGoodsId));
//
//        //统计每个商品的库存
//        for (Map.Entry<String, List<GoodsSkuStockDTO>> entry : groupByGoodsIds.entrySet()) {
//            //库存
//            Integer quantity = 0;
//            for (GoodsSkuStockDTO goodsSku : entry.getValue()) {
//                //向goodsSku对象设置库存
//                goodsSkuStockDTOS.stream().filter(i -> i.getSkuId().equals(goodsSku.getSkuId())).findFirst().ifPresent(i -> goodsSku.setQuantity(i.getQuantity()));
//                //按goodsId商品id计算总库存数
//                if (entry.getKey().equals(goodsSku.getGoodsId())) {
//                    quantity += goodsSku.getQuantity();
//                }
//                //更新sku的库存
//                this.updateStock(goodsSku.getSkuId(), goodsSku.getQuantity());
//            }
//            //更新该商品的总库存
//            goodsService.updateStock(entry.getKey(), quantity);
//        }
//    }

    @Override
    public void updateAlertQuantity(GoodsSkuStockDTO goodsSkuStockDTO) {
        GoodsSku goodsSku = this.getById(goodsSkuStockDTO.getSkuId());
        goodsSku.setAlertQuantity(goodsSkuStockDTO.getAlertQuantity());
        //清除缓存，防止修改预警后直接修改商品导致预警数值错误
        cache.remove(CachePrefix.GOODS.getPrefix() + goodsSku.getGoodsId());
        this.update(goodsSku);
    }

    @Override
    public void batchUpdateAlertQuantity(List<GoodsSkuStockDTO> goodsSkuStockDTOS) {
        List<GoodsSku> goodsSkuList = new ArrayList<>();
        List<String> skuIds = goodsSkuStockDTOS.stream().map(GoodsSkuStockDTO::getSkuId).collect(Collectors.toList());
        QueryWrapper<GoodsSku> objectQueryWrapper = GoodsSearchParamsDTO.builder().ids(skuIds).build().queryWrapper();
        List<GoodsSkuStockDTO> goodsSkuStockList = this.baseMapper.queryStocks(objectQueryWrapper);
        List<String> goodsIdList = goodsSkuStockList.stream().map(GoodsSkuStockDTO::getGoodsId).collect(Collectors.toList());
        HashSet<String> uniqueSet = new HashSet<>(goodsIdList);
        // 将去重后的元素转回列表
        List<String> uniqueGoodsIdList = new ArrayList<>(uniqueSet);
        for (String goodsId : uniqueGoodsIdList) {
            cache.remove(CachePrefix.GOODS.getPrefix() + goodsId);
        }
        //修改预警库存
        for (GoodsSkuStockDTO goodsSkuStockDTO : goodsSkuStockDTOS) {
            GoodsSku goodsSku = this.getById(goodsSkuStockDTO.getSkuId());
            goodsSku.setAlertQuantity(goodsSkuStockDTO.getAlertQuantity());
            goodsSkuList.add(goodsSku);
        }
        this.updateBatchById(goodsSkuList);
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStock(String skuId, Integer quantity) {
        GoodsSku goodsSku = getGoodsSkuByIdFromCache(skuId);
        if (goodsSku != null) {
            //判断商品sku是否已经下架(修改商品库存为0时  会自动下架商品),再次更新商品库存时 需更新商品索引
            boolean isFlag = goodsSku.getQuantity() <= 0;

            goodsSku.setQuantity(quantity);
            boolean update = this.update(new LambdaUpdateWrapper<GoodsSku>().eq(GoodsSku::getId, skuId).set(GoodsSku::getQuantity, quantity));
//            if (update) {
//                cache.remove(CachePrefix.GOODS.getPrefix() + goodsSku.getGoodsId());
//            }
            //同步库存缓存
            cache.put(GoodsSkuService.getStockCacheKey(skuId), quantity);
            //删除sku信息缓存
            cache.remove(GoodsSkuService.getCacheKeys(skuId));
//            cache.put(GoodsSkuService.getCacheKeys(skuId), goodsSku);


//            this.promotionGoodsService.updatePromotionGoodsStock(goodsSku.getId(), quantity);
//            //商品库存为0是删除商品索引
//            if (quantity <= 0) {
//                goodsIndexService.deleteIndexById(goodsSku.getId());
//            }
            //商品SKU库存为0并且商品sku状态为上架时更新商品库存
//            if (isFlag && CharSequenceUtil.equals(goodsSku.getMarketEnable(), GoodsStatusEnum.UPPER.name())) {
//                List<String> goodsIds = new ArrayList<>();
//                goodsIds.add(goodsSku.getGoodsId());
//                applicationEventPublisher.publishEvent(new TransactionCommitSendMQEvent("更新商品", rocketmqCustomProperties.getGoodsTopic(), GoodsTagsEnum.UPDATE_GOODS_INDEX.name(), goodsIds));
//            }
        }
    }
//    @Override
//    @Transactional(rollbackFor = Exception.class)
//    public void updateStock(String skuId, Integer quantity) {
//        GoodsSku goodsSku = getGoodsSkuByIdFromCache(skuId);
//        if (goodsSku != null) {
//            //判断商品sku是否已经下架(修改商品库存为0时  会自动下架商品),再次更新商品库存时 需更新商品索引
//            boolean isFlag = goodsSku.getQuantity() <= 0;
//
//            goodsSku.setQuantity(quantity);
//            boolean update = this.update(new LambdaUpdateWrapper<GoodsSku>().eq(GoodsSku::getId, skuId).set(GoodsSku::getQuantity, quantity));
//            if (update) {
//                cache.remove(CachePrefix.GOODS.getPrefix() + goodsSku.getGoodsId());
//            }
//            //同步sku信息缓存
//            cache.put(GoodsSkuService.getCacheKeys(skuId), goodsSku);
//            //同步库存缓存
//            cache.put(GoodsSkuService.getStockCacheKey(skuId), quantity);
//
////            this.promotionGoodsService.updatePromotionGoodsStock(goodsSku.getId(), quantity);
////            //商品库存为0是删除商品索引
////            if (quantity <= 0) {
////                goodsIndexService.deleteIndexById(goodsSku.getId());
////            }
//            //商品SKU库存为0并且商品sku状态为上架时更新商品库存
////            if (isFlag && CharSequenceUtil.equals(goodsSku.getMarketEnable(), GoodsStatusEnum.UPPER.name())) {
////                List<String> goodsIds = new ArrayList<>();
////                goodsIds.add(goodsSku.getGoodsId());
////                applicationEventPublisher.publishEvent(new TransactionCommitSendMQEvent("更新商品", rocketmqCustomProperties.getGoodsTopic(), GoodsTagsEnum.UPDATE_GOODS_INDEX.name(), goodsIds));
////            }
//        }
//    }

    @Override
    public Integer getStock(String skuId) {
        String cacheKeys = GoodsSkuService.getStockCacheKey(skuId);
        Integer stock = (Integer) cache.get(cacheKeys);
        if (stock != null) {
            return stock;
        } else {
            GoodsSku goodsSku = getGoodsSkuByIdFromCache(skuId);
            cache.put(cacheKeys, goodsSku.getQuantity());
            return goodsSku.getQuantity();
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateGoodsStock(List<GoodsSku> goodsSkus) {
        Map<String, List<GoodsSku>> groupByGoodsIds = goodsSkus.stream().collect(Collectors.groupingBy(GoodsSku::getGoodsId));

        //统计每个商品的库存
        for (String goodsId : groupByGoodsIds.keySet()) {
            //库存
            Integer quantity = 0;
            for (GoodsSku goodsSku : goodsSkus) {
                if (goodsId.equals(goodsSku.getGoodsId())) {
                    quantity += goodsSku.getQuantity();
                    this.updateStock(goodsSku.getId(), goodsSku.getQuantity());
                }
            }
            //保存商品库存结果
            goodsService.updateStock(goodsId, quantity);
        }


    }

    /**
     * 根据商品id获取全部skuId的集合
     *
     * @param goodsId goodsId
     * @return 全部skuId的集合
     */
    @Override
    public List<String> getSkuIdsByGoodsId(String goodsId) {
        return this.baseMapper.getGoodsSkuIdByGoodsId(goodsId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteAndInsertGoodsSkus(List<GoodsSku> goodsSkus) {
        int count = 0;
        for (GoodsSku skus : goodsSkus) {
            if (CharSequenceUtil.isEmpty(skus.getId())) {
                skus.setId(SnowFlake.getIdStr());
            }
            count = this.baseMapper.replaceGoodsSku(skus);
        }
        return count > 0;
    }

    @Override
    public Integer countSkuNum(String storeId) {
        LambdaQueryWrapper<GoodsSku> queryWrapper = new LambdaQueryWrapper<>();

        queryWrapper.eq(GoodsSku::getStoreId, storeId).eq(GoodsSku::getDeleteFlag, Boolean.FALSE).eq(GoodsSku::getAuthFlag, GoodsAuthEnum.PASS.name()).eq(GoodsSku::getMarketEnable, GoodsStatusEnum.UPPER.name());
        long count = this.count(queryWrapper);
        return (int) count;
    }


    /**
     * 批量渲染商品sku
     *
     * @param goodsSkuList      sku集合
     * @param goodsOperationDTO 商品操作DTO
     */
    @Override
    public void renderGoodsSkuList(List<GoodsSku> goodsSkuList, GoodsOperationDTO goodsOperationDTO) {
//        // 商品销售模式渲染器
//        salesModelRenders.stream().filter(i -> i.getSalesMode().equals(goodsOperationDTO.getSalesModel())).findFirst().ifPresent(i -> i.renderBatch(goodsSkuList, goodsOperationDTO));
        for (GoodsSku goodsSku : goodsSkuList) {
            //将原sku中的评论量、浏览量、购买量、好评率放到新的sku对象中
            extendOldSkuValue(goodsSku);
            //设置sku的图片信息
//            this.renderImages(goodsSku, goodsOperationDTO.getGoodsGalleryList());
        }
    }

    @Override
    public void updateGoodsSkuBuyCount(String skuId, int buyCount) {
        LambdaUpdateWrapper<GoodsSku> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(GoodsSku::getId, skuId);
//        updateWrapper.set(GoodsSku::getBuyCount, buyCount);
        updateWrapper.setSql("buy_count = buy_count + " + buyCount);
        this.update(updateWrapper);
    }

    @Override
    public void updateGoodsSkuGrade(String goodsId, double grade, int commentNum) {
        LambdaUpdateWrapper<GoodsSku> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(GoodsSku::getGoodsId, goodsId);
        updateWrapper.set(GoodsSku::getGrade, grade);
        updateWrapper.set(GoodsSku::getCommentNum, commentNum);
        this.update(updateWrapper);
        this.getSkuIdsByGoodsId(goodsId).forEach(this::clearCache);
    }

    /**
     * 渲染商品sku
     *
     * @param goodsSku          sku
     * @param goodsOperationDTO 商品操作DTO
     */
    void renderGoodsSku(GoodsSku goodsSku, GoodsOperationDTO goodsOperationDTO) {
        extendOldSkuValue(goodsSku);
//        // 商品销售模式渲染器
//        salesModelRenders.stream().filter(i -> i.getSalesMode().equals(goodsOperationDTO.getSalesModel())).findFirst().ifPresent(i -> i.renderSingle(goodsSku, goodsOperationDTO));
        this.renderImages(goodsSku, goodsOperationDTO.getGoodsGalleryList());
    }

    /**
     * 将原sku的一些不会直接传递的值放到新的sku中
     *
     * @param goodsSku 商品sku
     */
    private void extendOldSkuValue(GoodsSku goodsSku) {
        if (CharSequenceUtil.isNotEmpty(goodsSku.getGoodsId())) {
            GoodsSku oldSku = this.getGoodsSkuByIdFromCache(goodsSku.getId());
            if (oldSku != null) {
                goodsSku.setCommentNum(oldSku.getCommentNum());
                goodsSku.setViewCount(oldSku.getViewCount());
                goodsSku.setBuyCount(oldSku.getBuyCount());
                goodsSku.setGrade(oldSku.getGrade());
            }
        }
    }

    /**
     * 渲染sku图片
     *
     * @param goodsSku sku
     */
    void renderImages(GoodsSku goodsSku, List<String> goodsImages) {
        //获取商品规格信息
        JSONObject jsonObject = JSONUtil.parseObj(goodsSku.getSpecs());
        //取出规格中的图片
        List<String> images = jsonObject.getBeanList("images", String.class);
        GoodsGallery goodsGallery;
        if (images != null && !images.isEmpty()) {
            //如果针对不同的规格上传了图片则取出规格对应的图片作为该sku的图片
            goodsGallery = goodsGalleryService.getGoodsGallery(images.get(0));
        } else if(goodsImages!=null && goodsImages.size()>0) {
            //如果不没有规格图片则取出第一个商品图片
            goodsGallery = goodsGalleryService.getGoodsGallery(goodsImages.get(0));
        }else{
            return ;
        }

        goodsSku.setBig(goodsGallery.getOriginal());
        goodsSku.setOriginal(goodsGallery.getOriginal());
        goodsSku.setThumbnail(goodsGallery.getThumbnail());
        goodsSku.setSmall(goodsGallery.getSmall());
    }

    /**
     * 根据商品分组商品sku及其规格信息
     *
     * @param goodsSkuVOList 商品VO列表
     * @return 分组后的商品sku及其规格信息
     */
    private List<GoodsSkuSpecDTO> groupBySkuAndSpec(List<GoodsSkuDTO> goodsSkuVOList) {

        List<GoodsSkuSpecDTO> skuSpecVOList = new ArrayList<>();
        for (GoodsSkuDTO goodsSkuVO : goodsSkuVOList) {
            GoodsSkuSpecDTO specVO = new GoodsSkuSpecDTO();
            specVO.setSkuId(goodsSkuVO.getId());
            specVO.setSpecValues(goodsSkuVO.getSpecList());
            //设置库存
            specVO.setQuantity(goodsSkuVO.getQuantity());
            skuSpecVOList.add(specVO);
        }
        return skuSpecVOList;
    }

}
