//package com.jzo2o.mall.cart.service.render.impl;
//
//import cn.hutool.json.JSONObject;
//import cn.hutool.json.JSONUtil;
//import com.jzo2o.mall.cart.model.dto.CartDTO;
//import com.jzo2o.mall.cart.model.dto.CartSkuDTO;
//import com.jzo2o.mall.cart.model.dto.PriceDetailDTO;
//import com.jzo2o.mall.cart.model.dto.TradeDTO;
//import com.jzo2o.mall.cart.model.enums.RenderStepEnums;
//import com.jzo2o.mall.cart.service.render.CartRenderStep;
//import com.jzo2o.mall.common.utils.CurrencyUtil;
//import com.jzo2o.mall.member.service.MemberService;
//import com.jzo2o.mall.order.model.dto.DiscountPriceItem;
//import com.jzo2o.mall.promotion.model.enums.PromotionTypeEnum;
//import com.jzo2o.mall.promotion.model.dto.PromotionSkuDTO;
//import com.jzo2o.mall.promotion.service.PromotionGoodsService;
//import com.jzo2o.redis.helper.Cache;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import java.util.List;
//import java.util.Map;
//
///**
// * 购物促销信息渲染实现
// */
//@Service
//public class SkuPromotionRender implements CartRenderStep {
//
//
////    @Autowired
////    private KanjiaActivityService kanjiaActivityService;
////
////    @Autowired
////    private KanjiaActivityGoodsService kanjiaActivityGoodsService;
////
////    @Autowired
////    private PointsGoodsService pointsGoodsService;
//
//    /**
//     * 促销商品
//     */
//    @Autowired
//    private PromotionGoodsService promotionGoodsService;
//
//    @Autowired
//    private MemberService memberService;
//
//    @Autowired
//    private Cache cache;
//
//    @Override
//    public RenderStepEnums step() {
//        return RenderStepEnums.SKU_PROMOTION;
//    }
//
//    @Override
//    public void render(TradeDTO tradeDTO) {
//
//        //基础价格渲染
//        renderBasePrice(tradeDTO);
//        //渲染单品促销
//        renderSkuPromotion(tradeDTO);
//        //检查促销库存
//        checkPromotionQuantity(tradeDTO);
//
//
//    }
//
//    /**
//     * 基础价格渲染
//     *
//     * @param tradeDTO
//     */
//    private void renderBasePrice(TradeDTO tradeDTO) {
//        tradeDTO.getCartList().forEach(
//                cartDTO -> cartDTO.getCheckedSkuList().forEach(cartSkuDTO -> {
//                    PriceDetailDTO priceDetailDTO = cartSkuDTO.getPriceDetailDTO();
//                    priceDetailDTO.setGoodsPrice(cartSkuDTO.getSubTotal());
//                    priceDetailDTO.setDiscountPrice(CurrencyUtil.sub(priceDetailDTO.getOriginalPrice(), cartSkuDTO.getSubTotal()));
//                })
//        );
//    }
//
//
//    /**
//     * 渲染单品优惠 积分/拼团/秒杀/砍价
//     *
//     * @param tradeDTO 购物车视图
//     */
//    private void renderSkuPromotion(TradeDTO tradeDTO) {
//
//
//        switch (tradeDTO.getCartTypeEnum()) {
//
//            //这里是双重循环，但是实际积分购买或者是砍价购买时，购物车只有一个商品，所以没有循环操作数据库或者其他的问题
////            case POINTS:
////                Member userInfo = memberService.getUserInfo();
////                long totalPayPoints = 0;
////                //处理积分商品购买
////                for (CartVO cartVO : tradeDTO.getCartList()) {
////                    for (CartSkuVO cartSkuVO : cartVO.getCheckedSkuList()) {
////                        cartSkuVO.getPriceDetailDTO().setPayPoint(cartSkuVO.getPoint());
////                        PromotionSkuVO promotionSkuVO = new PromotionSkuVO(PromotionTypeEnum.POINTS_GOODS.name(), cartSkuVO.getPointsId());
////                        cartSkuVO.getPriceDetailDTO().getJoinPromotion().add(promotionSkuVO);
////                        totalPayPoints += cartSkuVO.getPoint();
////
////                        //记录优惠由来
////                        cartSkuVO.getPriceDetailDTO().setDiscountPriceItem(
////                                DiscountPriceItem.builder()
////                                        .discountPrice(CurrencyUtil.sub(cartSkuVO.getGoodsSku().getPrice(), cartSkuVO.getPurchasePrice()))
////                                        .promotionId(promotionSkuVO.getActivityId())
////                                        .promotionTypeEnum(PromotionTypeEnum.POINTS_GOODS)
////                                        .skuId(cartSkuVO.getGoodsSku().getId())
////                                        .goodsId(cartSkuVO.getGoodsSku().getGoodsId())
////                                        .build()
////                        );
////                    }
////                }
////                if (userInfo.getPoint() < totalPayPoints) {
////                    throw new ServiceException(ResultCode.POINT_NOT_ENOUGH);
////                }
////                return;
////            case KANJIA:
////                for (CartVO cartVO : tradeDTO.getCartList()) {
////                    for (CartSkuVO cartSkuVO : cartVO.getCheckedSkuList()) {
////                        KanjiaActivitySearchParams kanjiaActivitySearchParams = new KanjiaActivitySearchParams();
////                        kanjiaActivitySearchParams.setGoodsSkuId(cartSkuVO.getGoodsSku().getId());
////                        kanjiaActivitySearchParams.setMemberId(Objects.requireNonNull(UserContext.getCurrentUser()).getId());
////                        kanjiaActivitySearchParams.setStatus(KanJiaStatusEnum.SUCCESS.name());
////                        KanjiaActivityVO kanjiaActivityVO = kanjiaActivityService.getKanjiaActivityVO(kanjiaActivitySearchParams);
////                        //可以砍价金额购买，则处理信息
////                        if (Boolean.TRUE.equals(kanjiaActivityVO.getPass())) {
////                            cartSkuVO.setKanjiaId(kanjiaActivityVO.getId());
////                            cartSkuVO.setPurchasePrice(kanjiaActivityVO.getPurchasePrice());
////                            cartSkuVO.setSubTotal(kanjiaActivityVO.getPurchasePrice());
////                            cartSkuVO.getPriceDetailDTO().setGoodsPrice(kanjiaActivityVO.getPurchasePrice());
////                        }
////                        PromotionSkuVO promotionSkuVO = new PromotionSkuVO(PromotionTypeEnum.KANJIA.name(), cartSkuVO.getKanjiaId());
////                        cartSkuVO.getPriceDetailDTO().getJoinPromotion().add(promotionSkuVO);
////
////                        //记录优惠由来
////                        cartSkuVO.getPriceDetailDTO().setDiscountPriceItem(
////                                DiscountPriceItem.builder()
////                                        .discountPrice(CurrencyUtil.sub(cartSkuVO.getGoodsSku().getPrice(), cartSkuVO.getPurchasePrice()))
////                                        .promotionId(promotionSkuVO.getActivityId())
////                                        .promotionTypeEnum(PromotionTypeEnum.KANJIA)
////                                        .skuId(cartSkuVO.getGoodsSku().getId())
////                                        .goodsId(cartSkuVO.getGoodsSku().getGoodsId())
////                                        .build()
////                        );
////                    }
////                }
////                return;
////            case PINTUAN:
////                for (CartVO cartVO : tradeDTO.getCartList()) {
////                    for (CartSkuVO cartSkuVO : cartVO.getCheckedSkuList()) {
////                        PromotionSkuVO promotionSkuVO = new PromotionSkuVO(PromotionTypeEnum.PINTUAN.name(), cartSkuVO.getPintuanId());
////                        cartSkuVO.getPriceDetailDTO().getJoinPromotion().add(promotionSkuVO);
////
////                        //记录优惠由来
////                        cartSkuVO.getPriceDetailDTO().setDiscountPriceItem(
////                                DiscountPriceItem.builder()
////                                        .discountPrice(CurrencyUtil.sub(cartSkuVO.getGoodsSku().getPrice(), cartSkuVO.getPurchasePrice()))
////                                        .promotionId(promotionSkuVO.getActivityId())
////                                        .promotionTypeEnum(PromotionTypeEnum.PINTUAN)
////                                        .skuId(cartSkuVO.getGoodsSku().getId())
////                                        .goodsId(cartSkuVO.getGoodsSku().getGoodsId())
////                                        .build()
////                        );
////                    }
////                }
////                return;
//            case CART:
//            case BUY_NOW:
////            case VIRTUAL:
//                //循环购物车
//                for (CartDTO cartDTO : tradeDTO.getCartList()) {
//                    //循环sku
//                    for (CartSkuDTO cartSkuDTO : cartDTO.getCheckedSkuList()) {
//                        //赋予商品促销信息
//                        for (Map.Entry<String, Object> entry : cartSkuDTO.getPromotionMap().entrySet()) {
//                            if (ignorePromotion(entry.getKey())) {
//                                continue;
//                            }
//
//                            JSONObject promotionsObj = JSONUtil.parseObj(entry.getValue());
//                            //构建促销活动对象(构造参数：活动类型,，活动id)
//                            PromotionSkuDTO promotionSku = new PromotionSkuDTO(entry.getKey().split("-")[0], promotionsObj.get("id", String.class));
////                            cartSkuDTO.setSubTotal(CurrencyUtil.mul(cartSkuDTO.getPurchasePrice(), cartSkuDTO.getNum()));
////                            cartSkuDTO.getPriceDetailDTO().setGoodsPrice(cartSkuDTO.getSubTotal());
//
//                            cartSkuDTO.getPriceDetailDTO().getJoinPromotion().add(promotionSku);
//
//                            //如果是秒杀活动
////                            if (promotionSku.getPromotionType().equals(PromotionTypeEnum.SECKILL.name())) {
////                                //需记录秒杀活动详情
////                                cartSkuDTO.getPriceDetailDTO().setDiscountPriceItem(
////                                        DiscountPriceItem.builder()
////                                                .discountPrice(CurrencyUtil.sub(cartSkuDTO.getGoodsSku().getPrice(), cartSkuDTO.getPurchasePrice()))
////                                                .promotionId(promotionSku.getActivityId())
////                                                .promotionTypeEnum(PromotionTypeEnum.SECKILL)
////                                                .skuId(cartSkuDTO.getGoodsSku().getId())
////                                                .goodsId(cartSkuDTO.getGoodsSku().getGoodsId())
////                                                .build());
////                            }
//
//
//                        }
//                    }
//                }
//                return;
////            case SECKILL:
////                //取出秒杀商品
////                for (CartDTO cartDTO : tradeDTO.getCartList()) {
////                    //循环sku
////                    for (CartSkuDTO cartSkuDTO : cartDTO.getCheckedSkuList()) {
////                        //赋予商品促销信息
////                        for (Map.Entry<String, Object> entry : cartSkuDTO.getPromotionMap().entrySet()) {
////                            if (ignorePromotion(entry.getKey())) {
////                                continue;
////                            }
////
////                            JSONObject promotionsObj = JSONUtil.parseObj(entry.getValue());
////                            //构建促销活动对象(构造参数：活动类型,，活动id)
////                            PromotionSkuDTO promotionSku = new PromotionSkuDTO(entry.getKey().split("-")[0], promotionsObj.get("id", String.class));
////                            cartSkuDTO.setSubTotal(CurrencyUtil.mul(cartSkuDTO.getPurchasePrice(), cartSkuDTO.getNum()));
////                            cartSkuDTO.getPriceDetailDTO().setGoodsPrice(cartSkuDTO.getSubTotal());
////
////                            cartSkuDTO.getPriceDetailDTO().getJoinPromotion().add(promotionSku);
////
////                            //如果是秒杀活动
////                            if (promotionSku.getPromotionType().equals(PromotionTypeEnum.SECKILL.name())) {
////                                //需记录秒杀活动详情
////                                cartSkuDTO.getPriceDetailDTO().setDiscountPriceItem(
////                                        DiscountPriceItem.builder()
////                                                .discountPrice(CurrencyUtil.sub(cartSkuDTO.getGoodsSku().getPrice(), cartSkuDTO.getPurchasePrice()))
////                                                .promotionId(promotionSku.getActivityId())
////                                                .promotionTypeEnum(PromotionTypeEnum.SECKILL)
////                                                .skuId(cartSkuDTO.getGoodsSku().getId())
////                                                .goodsId(cartSkuDTO.getGoodsSku().getGoodsId())
////                                                .build());
////                            }
////
////
////                        }
////                    }
////                }
//            default:
//        }
//    }
//
//    /**
//     * 检查促销库存
//     *
//     * @param tradeDTO 购物车视图
//     */
//    private void checkPromotionQuantity(TradeDTO tradeDTO) {
//        for (CartSkuDTO cartSkuDTO : tradeDTO.getCheckedSkuList()) {
//            List<PromotionSkuDTO> joinPromotion = cartSkuDTO.getPriceDetailDTO().getJoinPromotion();
//            if (!joinPromotion.isEmpty()) {
//                for (PromotionSkuDTO promotionSkuVO : joinPromotion) {
//                    this.checkPromotionGoodsQuantity(cartSkuDTO, promotionSkuVO);
//                }
//            }
//        }
//    }
//
//    private void checkPromotionGoodsQuantity(CartSkuDTO cartSkuDTO, PromotionSkuDTO promotionSkuDTO) {
//        String promotionGoodsStockCacheKey = PromotionGoodsService.getPromotionGoodsStockCacheKey(PromotionTypeEnum.valueOf(promotionSkuDTO.getPromotionType()), promotionSkuDTO.getActivityId(), cartSkuDTO.getGoodsSku().getId());
//        Object quantity = cache.get(promotionGoodsStockCacheKey);
//
//        if (quantity == null) {
//            //如果促销有库存信息
//            PromotionTypeEnum promotionTypeEnum = PromotionTypeEnum.valueOf(promotionSkuDTO.getPromotionType());
//            switch (promotionTypeEnum) {
////                case KANJIA:
////                    quantity = kanjiaActivityGoodsService.getKanjiaGoodsBySkuId(cartSkuVO.getGoodsSku().getId()).getStock();
////                    break;
////                case POINTS_GOODS:
////                    quantity = pointsGoodsService.getPointsGoodsDetailBySkuId(cartSkuVO.getGoodsSku().getId()).getActiveStock();
////                    break;
//                case SECKILL:
//                case PINTUAN:
//                    quantity = promotionGoodsService.getPromotionGoodsStock(PromotionTypeEnum.valueOf(promotionSkuDTO.getPromotionType()), promotionSkuDTO.getActivityId(), cartSkuDTO.getGoodsSku().getId());
//                    break;
//                default:
//                    return;
//            }
//        }
//
//
//        if (quantity != null && cartSkuDTO.getNum() > (Integer) quantity) {//设置购物车未选中
//            cartSkuDTO.setChecked(false);
//            //设置失效消息
//            cartSkuDTO.setErrorMessage("促销商品库存不足,现有库存数量[" + quantity + "]");
//        }
//    }
//
//    /**
//     * 购物车促销类型
//     */
//    private boolean ignorePromotion(String promotionKey) {
//
//        // 忽略积分活动活动 忽略砍价活动 忽略优惠券活动 忽略拼团活动
//        return promotionKey.contains(PromotionTypeEnum.POINTS_GOODS.name())
//                || promotionKey.contains(PromotionTypeEnum.KANJIA.name())
//                || promotionKey.contains(PromotionTypeEnum.COUPON.name())
//                || promotionKey.contains(PromotionTypeEnum.PINTUAN.name());
//    }
//
//}
