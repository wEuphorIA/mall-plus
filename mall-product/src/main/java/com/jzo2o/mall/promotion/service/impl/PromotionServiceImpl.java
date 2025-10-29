package com.jzo2o.mall.promotion.service.impl;

import cn.hutool.json.JSONUtil;
import com.jzo2o.mall.promotion.model.enums.PromotionTypeEnum;
import com.jzo2o.mall.promotion.model.domain.Coupon;
import com.jzo2o.mall.promotion.model.domain.PromotionGoods;
import com.jzo2o.mall.promotion.model.domain.Seckill;
import com.jzo2o.mall.promotion.model.domain.SeckillApply;
import com.jzo2o.mall.promotion.model.dto.PromotionGoodsSearchParams;
import com.jzo2o.mall.promotion.model.dto.SeckillSearchParams;
import com.jzo2o.mall.promotion.model.enums.PromotionsStatusEnum;
import com.jzo2o.mall.promotion.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 促销业务层实现
 */
@Slf4j
@Service
public class PromotionServiceImpl implements PromotionService {
    /**
     * 秒杀
     */
    @Autowired
    private SeckillService seckillService;
    /**
     * 秒杀申请
     */
    @Autowired
    private SeckillApplyService seckillApplyService;
    /**
     * 满额活动
     */
//    @Autowired
//    private FullDiscountService fullDiscountService;
    /**
     * 拼团
     */
//    @Autowired
//    private PintuanService pintuanService;
    /**
     * 优惠券
     */
    @Autowired
    private CouponService couponService;
    /**
     * 促销商品
     */
    @Autowired
    private PromotionGoodsService promotionGoodsService;
    /**
     * 积分商品
     */
//    @Autowired
//    private PointsGoodsService pointsGoodsService;
//
//    @Autowired
//    private KanjiaActivityGoodsService kanjiaActivityGoodsService;


    /**
     * 获取当前进行的所有促销活动信息
     *
     * @return 当前促销活动集合
     */
    @Override
    public Map<String, List<PromotionGoods>> getCurrentPromotion() {
        PromotionGoodsSearchParams searchParams = new PromotionGoodsSearchParams();
        searchParams.setPromotionStatus(PromotionsStatusEnum.START.name());
        List<PromotionGoods> promotionGoods = promotionGoodsService.listFindAll(searchParams);
        return promotionGoods.stream().collect(Collectors.groupingBy(PromotionGoods::getPromotionType));
    }

    /**
     * 根据商品索引获取当前商品索引的所有促销活动信息
     *
     * @param storeId    店铺id
     * @param goodsSkuId 商品skuId
     * @return 当前促销活动集合
     */
    public Map<String, Object> getGoodsSkuPromotionMap(String storeId, String goodsSkuId) {
        String storeIds = storeId + "," + PromotionTools.PLATFORM_ID;
        List<PromotionGoods> promotionGoodsList = promotionGoodsService.findSkuValidPromotion(goodsSkuId, storeIds);
        return wrapperPromotionMapList(promotionGoodsList);
    }

    @Override
    public void removeByGoodsIds(String goodsIdsJsonStr) {
        List<String> goodsIds = JSONUtil.toList(goodsIdsJsonStr, String.class);
        promotionGoodsService.deletePromotionGoodsByGoods(goodsIds);
//        kanjiaActivityGoodsService.deleteByGoodsIds(goodsIds);
    }

    public Map<String, Object> wrapperPromotionMapList(List<PromotionGoods> promotionGoodsList) {
        //key是促销类型+促销id，value是促销活动对象(优惠券,秒杀)
        Map<String, Object> promotionMap = new HashMap<>();
        for (PromotionGoods promotionGoods : promotionGoodsList) {
            String esPromotionKey = promotionGoods.getPromotionType() + "-" + promotionGoods.getPromotionId();
            switch (PromotionTypeEnum.valueOf(promotionGoods.getPromotionType())) {
                case COUPON:
                    //优惠券
                    Coupon coupon = couponService.getById(promotionGoods.getPromotionId());
                    promotionMap.put(esPromotionKey, coupon);
                    break;
//                case PINTUAN:
//                    Pintuan pintuan = pintuanService.getById(promotionGoods.getPromotionId());
//                    promotionMap.put(esPromotionKey, pintuan);
//                    break;
//                case FULL_DISCOUNT:
//                    FullDiscount fullDiscount = fullDiscountService.getById(promotionGoods.getPromotionId());
//                    promotionMap.put(esPromotionKey, fullDiscount);
//                    break;
                case SECKILL:
                    this.getGoodsCurrentSeckill(esPromotionKey, promotionGoods, promotionMap);
                    break;
//                case POINTS_GOODS:
//                    PointsGoods pointsGoods = pointsGoodsService.getById(promotionGoods.getPromotionId());
//                    promotionMap.put(esPromotionKey, pointsGoods);
//                    break;
                default:
                    break;
            }
        }
        return promotionMap;
    }

    private void getGoodsCurrentSeckill(String esPromotionKey, PromotionGoods promotionGoods, Map<String, Object> promotionMap) {
        Seckill seckill = seckillService.getById(promotionGoods.getPromotionId());
        SeckillSearchParams searchParams = new SeckillSearchParams();
        searchParams.setSeckillId(promotionGoods.getPromotionId());
        searchParams.setSkuId(promotionGoods.getSkuId());
        List<SeckillApply> seckillApplyList = seckillApplyService.getSeckillApplyList(searchParams);
        if (seckillApplyList != null && !seckillApplyList.isEmpty()) {
//            String[] split = seckill.getHours().split(",");
//            int[] hoursSored = Arrays.stream(split).mapToInt(Integer::parseInt).toArray();
//            Arrays.sort(hoursSored);
            seckill.setStartTime(promotionGoods.getStartTime());
            seckill.setEndTime(promotionGoods.getEndTime());
            promotionMap.put(esPromotionKey, seckill);
        }
    }

}