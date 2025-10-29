package com.jzo2o.mall.product.event;

import cn.hutool.core.convert.Convert;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.jzo2o.mall.common.cache.CachePrefix;
import com.jzo2o.mall.common.enums.ModuleEnums;
import com.jzo2o.mall.common.enums.PayStatusEnum;
import com.jzo2o.mall.common.event.OrderStatusChangeEvent;
import com.jzo2o.mall.common.model.message.OrderStatusMessage;
import com.jzo2o.mall.product.model.domain.GoodsSku;
import com.jzo2o.mall.product.service.GoodsService;
import com.jzo2o.mall.product.service.GoodsSkuService;
import com.jzo2o.redis.helper.Cache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 订单状态修改操作库存:
 * 订单创建成功进行库存扣减,如果扣减失败要取消订单
 * 取消订单进行库存回滚
 */
@Slf4j
@Service
public class StockUpdateExecute implements OrderStatusChangeEvent {

    /**
     * 缓存
     */
    @Autowired
    private Cache cache;

    /**
     * 出库失败消息
     */
    static String outOfStockMessage = "库存不足，出库失败";
    /**
     * Redis
     */
    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private DefaultRedisScript<Boolean> quantityScript;

    /**
     * 规格商品
     */
    @Autowired
    private GoodsSkuService goodsSkuService;
    @Autowired
    private GoodsService goodsService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void orderChange(OrderStatusMessage orderMessage) {

        switch (orderMessage.getNewStatus()) {
            //UNPAID订单创建成功
            case UNPAID: {
                //获取订单详情
                OrderStatusMessage.OrderDetail orderDetail = orderMessage.getOrderDetail();
                //库存key 和 扣减数量
                List<String> keys = new ArrayList<>();
                List<String> values = new ArrayList<>();
                for (OrderStatusMessage.OrderDetail.OrderItem orderItem : orderDetail.getOrderItems()) {
                    keys.add(GoodsSkuService.getStockCacheKey(orderItem.getSkuId()));
                    int i = -orderItem.getNum();
                    values.add(String.valueOf(i));
//                    setPromotionStock(keys, values, orderItem, true);
                }

                List<Integer> stocks = cache.multiGet(keys);
                //如果缓存中不存在存在等量的库存值，则重新写入缓存，防止缓存击穿导致无法下单
                checkStocks(stocks, orderDetail);
                //库存扣除结果
                RedisSerializer<String> stringSerializer = new StringRedisSerializer();
                Boolean skuResult   = (Boolean) redisTemplate.execute(quantityScript,
                        stringSerializer,stringSerializer,
                        keys, values.toArray());
//                Boolean skuResult = redisTemplate.execute(quantityScript, keys, values.toArray());
                //如果库存扣减都成功，则记录成交订单
                if (Boolean.TRUE.equals(skuResult)) {
                    log.info("库存扣减成功,参数为{};{}", keys, values);
                    //库存确认之后对结构处理-修改订单状态为待发货
//                    orderService.afterOrderConfirm(orderMessage.getOrderSn());
                    //从orderDetail中提取出List<GoodsSku>
                    List<GoodsSku> goodsSkus = getGoodsSkus(orderDetail);
                    //成功之后，同步库存
                    synchroDB(goodsSkus);
                    //发送库存扣减成功消息
                    goodsSkuService.sendUpdateStockMessage(orderMessage.getOrderDetail().getOrder().getSn(), false);
                } else {
                    log.info("库存扣件失败，变更缓存key{} 变更缓存value{}", keys, values);
                    //失败之后取消订单
//                    this.errorOrder(orderMessage.getOrderSn());
                    //发送库存扣减失败消息
                    goodsSkuService.sendUpdateStockMessage(orderMessage.getOrderDetail().getOrder().getSn(), true);
                }
                break;
            }
            case CANCELLED: {
                //获取订单详情
                OrderStatusMessage.OrderDetail orderDetail = orderMessage.getOrderDetail();
//                //判定是否已支付 并且 非库存不足导致库存回滚 则需要考虑订单库存返还业务
//                if (orderDetail.getOrder().getPayStatus().equals(PayStatusEnum.PAID.name())
//                        && !orderDetail.getOrder().getCancelReason().equals(outOfStockMessage)) {
                //库存不足导致库存回滚
                if ( !orderDetail.getOrder().getCancelReason().equals(outOfStockMessage)) {
                    //库存key 和 还原数量
                    List<String> keys = new ArrayList<>();
                    List<String> values = new ArrayList<>();

                    //返还商品库存，促销库存不与返还，不然前台展示层有展示逻辑错误
                    for (OrderStatusMessage.OrderDetail.OrderItem orderItem : orderDetail.getOrderItems()) {
                        keys.add(GoodsSkuService.getStockCacheKey(orderItem.getSkuId()));
                        int i = orderItem.getNum();
                        values.add(Integer.toString(i));
//                        setPromotionStock(keys, values, orderItem, false);
                    }
                    //批量脚本执行库存回退
//                    Boolean skuResult = stringRedisTemplate.execute(quantityScript, keys, values.toArray());
                    RedisSerializer<String> stringSerializer = new StringRedisSerializer();
                    Boolean skuResult   = (Boolean) redisTemplate.execute(quantityScript,stringSerializer,stringSerializer, keys, values.toArray());
                    //返还失败，则记录日志
                    if (Boolean.FALSE.equals(skuResult)) {
                        log.error("库存回退异常，keys：{},回复库存值为: {}", keys, values);
                    }
                    //从orderDetail中提取出List<GoodsSku>
                    List<GoodsSku> goodsSkus = getGoodsSkus(orderDetail);
                    //同步库存
                    synchroDB(goodsSkus);
                }
                break;
            }
//            case COMPLETED: {
//                //解析出订单明细
//                OrderStatusMessage.OrderDetail orderDetail = orderMessage.getOrderDetail();
//                List<OrderStatusMessage.OrderDetail.OrderItem> orderItems = orderDetail.getOrderItems();
//
//                orderItems.forEach(orderItem -> {
//                    //更新商品sku购买数量
//                    goodsSkuService.updateGoodsSkuBuyCount(orderItem.getSkuId(), orderItem.getNum());
//                    //更新商品spu购买数量
//                    goodsService.updateGoodsBuyCount(orderItem.getGoodsId(), orderItem.getNum());
//                    //更新商品索引中的购买数量
//
//
//                });
//            }
            default:
                break;
        }
    }

    /**
     * 获取订单明细中的商品sku
     * @param orderDetail
     * @return
     */
    private static List<GoodsSku> getGoodsSkus(OrderStatusMessage.OrderDetail orderDetail) {
        List<GoodsSku> goodsSkus = orderDetail.getOrderItems().stream().map(orderItem -> {
            GoodsSku goodsSku = new GoodsSku();
            goodsSku.setId(orderItem.getSkuId());
            goodsSku.setGoodsId(orderItem.getGoodsId());
            return goodsSku;
        }).collect(Collectors.toList());
        return goodsSkus;
    }

    @Override
    public ModuleEnums getModule() {
        return ModuleEnums.PRODUCT;
    }


    /**
     * 校验库存是否有效
     *
     * @param stocks
     */
    private void checkStocks(List<Integer> stocks, OrderStatusMessage.OrderDetail order) {
        if (!stocks.isEmpty() && order.getOrderItems().size() == stocks.size() && stocks.stream().anyMatch(Objects::nonNull)) {
            return;
        }
        initSkuCache(order.getOrderItems());
//        initPromotionCache(order.getOrderItems());
    }

    /**
     * 缓存中sku库存值不存在时，将不存在的信息重新写入一边
     *
     * @param orderItems
     */
    private void initSkuCache(List<OrderStatusMessage.OrderDetail.OrderItem> orderItems) {
        orderItems.forEach(orderItem -> {
            //如果不存在
            if (!cache.hasKey(GoodsSkuService.getStockCacheKey(orderItem.getSkuId()))) {
                //内部会自动写入，这里不需要进行二次处理
                goodsSkuService.getStock(orderItem.getSkuId());
            }
        });
    }

    /**
     * 初始化促销商品缓存
     *
     * @param orderItems
     */
//    private void initPromotionCache(List<OrderItem> orderItems) {
//
//        //如果促销类型需要库存判定，则做对应处理
//        orderItems.forEach(orderItem -> {
//            if (orderItem.getPromotionType() != null) {
//                String[] skuPromotions = orderItem.getPromotionType().split(",");
//                for (int i = 0; i < skuPromotions.length; i++) {
//                    int currentIndex = i;
//                    //如果此促销有库存概念，则计入
//                    Arrays.stream(PromotionTypeEnum.haveStockPromotion).filter(promotionTypeEnum -> promotionTypeEnum.name().equals(skuPromotions[currentIndex]))
//                            .findFirst()
//                            .ifPresent(promotionTypeEnum -> {
//                                String promotionId = orderItem.getPromotionId().split(",")[currentIndex];
//                                String cacheKey = PromotionGoodsService.getPromotionGoodsStockCacheKey(promotionTypeEnum, promotionId, orderItem.getSkuId());
//
//                                switch (promotionTypeEnum) {
//                                    case KANJIA:
//                                        cache.put(cacheKey, kanjiaActivityGoodsService.getKanjiaGoodsBySkuId(orderItem.getSkuId()).getStock());
//                                        return;
//                                    case POINTS_GOODS:
//                                        cache.put(cacheKey, pointsGoodsService.getPointsGoodsDetailBySkuId(orderItem.getSkuId()).getActiveStock());
//                                        return;
//                                    case SECKILL:
//                                    case PINTUAN:
//                                        cache.put(cacheKey, promotionGoodsService.getPromotionGoodsStock(promotionTypeEnum, promotionId, orderItem.getSkuId()));
//                                        return;
//                                    default:
//                                        break;
//                                }
//                            });
//                }
//
//            }
//        });
//    }


    /**
     * 订单出库失败
     *
     * @param orderSn 失败入库订单信息
     */
//    private void errorOrder(String orderSn) {
//        orderService.systemCancel(orderSn, outOfStockMessage, true);
//    }


    /**
     * 写入需要更改促销库存的商品
     *
     * @param keys   缓存key值
     * @param values 缓存value值
     * @param sku    购物车信息
     */
//    private void setPromotionStock(List<String> keys, List<String> values, OrderItem sku, boolean deduction) {
//        if (sku.getPromotionType() != null) {
//            //如果此促销有库存概念，则计入
//            String[] skuPromotions = sku.getPromotionType().split(",");
//            for (int i = 0; i < skuPromotions.length; i++) {
//                int currentIndex = i;
//                Arrays.stream(PromotionTypeEnum.haveStockPromotion).filter(promotionTypeEnum -> promotionTypeEnum.name().equals(skuPromotions[currentIndex]))
//                        .findFirst()
//                        .ifPresent(promotionTypeEnum -> {
//                            keys.add(PromotionGoodsService.getPromotionGoodsStockCacheKey(promotionTypeEnum, sku.getPromotionId().split(",")[currentIndex], sku.getSkuId()));
//                            int num = deduction ? -sku.getNum() : sku.getNum();
//                            values.add(Integer.toString(num));
//                        });
//            }
//        }
//    }


    /**
     * 同步库存和促销库存
     * <p>
     * 需修改：DB：商品库存、Sku商品库存、活动商品库存
     * 1.获取需要修改的Sku列表、活动商品列表
     * 2.写入sku商品库存，批量修改
     * 3.写入促销商品的卖出数量、剩余数量,批量修改
     * 4.调用方法修改商品库存
     * @param goodsSkus sku列表(包括skuid、spuid)
     */
    public void synchroDB(List<GoodsSku> goodsSkus) {
        //促销商品
//        List<PromotionGoods> promotionGoods = new ArrayList<>();
        //促销库存key 集合
        List<String> promotionKey = new ArrayList<>();

        //从goodsSkus获取skuid组成一个List,使用Stream流的map方法实现
        List<String> skuKeys = goodsSkus.stream().map(sku->{
            String stockCacheKey = GoodsSkuService.getStockCacheKey(sku.getId());
            return stockCacheKey;
        }).collect(Collectors.toList());

        //批量获取商品库存
        List skuStocks = cache.multiGet(skuKeys);
        //循环写入商品库存
        for (int i = 0; i < skuStocks.size(); i++) {
            goodsSkus.get(i).setQuantity(Convert.toInt(skuStocks.get(i).toString()));
        }
        //商品库存，包含sku库存集合，更新至数据库
        Map<String, List<GoodsSku>> groupByGoodsIds = goodsSkus.stream().collect(Collectors.groupingBy(GoodsSku::getGoodsId));

        //统计每个商品的库存
        for (String goodsId : groupByGoodsIds.keySet()) {
            //库存
            Integer quantity = 0;
            for (GoodsSku goodsSku : goodsSkus) {
                if (goodsId.equals(goodsSku.getGoodsId())) {
                    quantity += goodsSku.getQuantity();
                    boolean update = goodsSkuService.update(new LambdaUpdateWrapper<GoodsSku>().eq(GoodsSku::getId, goodsSku.getId()).set(GoodsSku::getQuantity, goodsSku.getQuantity()));
                }
            }
            //保存商品库存结果
            goodsService.updateStock(goodsId, quantity);
        }
        log.info("订单确认，库存同步：商品信息--{}；促销信息---{}", goodsSkus, null);
    }

//    private void synchroDB(OrderStatusMessage.OrderDetail order) {
//
//        //sku商品
//        List<GoodsSku> goodsSkus = new ArrayList<>();
//        //促销商品
////        List<PromotionGoods> promotionGoods = new ArrayList<>();
//        //sku库存key 集合
//        List<String> skuKeys = new ArrayList<>();
//        //促销库存key 集合
//        List<String> promotionKey = new ArrayList<>();
//
//        //循环订单
//        for (OrderStatusMessage.OrderDetail.OrderItem orderItem : order.getOrderItems()) {
//            skuKeys.add(GoodsSkuService.getStockCacheKey(orderItem.getSkuId()));
//
//            GoodsSku goodsSku = new GoodsSku();
//            goodsSku.setId(orderItem.getSkuId());
//            goodsSku.setGoodsId(orderItem.getGoodsId());
//            //如果有促销信息
////            if (null != orderItem.getPromotionType() && null != orderItem.getPromotionId()) {
////                //如果促销有库存信息
////                String[] skuPromotions = orderItem.getPromotionType().split(",");
////                for (int i = 0; i < skuPromotions.length; i++) {
////                    int currentIndex = i;
////                    Arrays.stream(PromotionTypeEnum.haveStockPromotion).filter(promotionTypeEnum -> promotionTypeEnum.name().equals(skuPromotions[currentIndex]))
////                            .findFirst()
////                            .ifPresent(promotionTypeEnum -> {
////                                //修改砍价商品库存
////                                String promotionId = orderItem.getPromotionId().split(",")[currentIndex];
////
////                                //修改砍价商品库存
////                                if (promotionTypeEnum.equals(PromotionTypeEnum.KANJIA)) {
////                                    KanjiaActivity kanjiaActivity = kanjiaActivityService.getById(promotionId);
////                                    KanjiaActivityGoodsDTO kanjiaActivityGoodsDTO = kanjiaActivityGoodsService.getKanjiaGoodsDetail(kanjiaActivity.getKanjiaActivityGoodsId());
////
////                                    Integer stock = Integer.parseInt(cache.get(PromotionGoodsService.getPromotionGoodsStockCacheKey(promotionTypeEnum, promotionId, orderItem.getSkuId())).toString());
////                                    kanjiaActivityGoodsDTO.setStock(stock);
////
////                                    kanjiaActivityGoodsService.updateById(kanjiaActivityGoodsDTO);
////                                    //修改积分商品库存
////                                } else if (promotionTypeEnum.equals(PromotionTypeEnum.POINTS_GOODS)) {
////                                    PointsGoodsVO pointsGoodsVO = pointsGoodsService.getPointsGoodsDetail(promotionId);
////                                    Integer stock = Integer.parseInt(cache.get(PromotionGoodsService.getPromotionGoodsStockCacheKey(promotionTypeEnum, promotionId, orderItem.getSkuId())).toString());
////                                    pointsGoodsVO.setActiveStock(stock);
////                                    pointsGoodsService.updateById(pointsGoodsVO);
////                                } else {
////                                    PromotionGoodsSearchParams searchParams = new PromotionGoodsSearchParams();
////                                    searchParams.setPromotionType(promotionTypeEnum.name());
////                                    searchParams.setPromotionId(promotionId);
////                                    searchParams.setSkuId(orderItem.getSkuId());
////                                    PromotionGoods pGoods = promotionGoodsService.getPromotionsGoods(searchParams);
////                                    //记录需要更新的促销库存信息
////                                    promotionKey.add(
////                                            PromotionGoodsService.getPromotionGoodsStockCacheKey(
////                                                    promotionTypeEnum,
////                                                    promotionId, orderItem.getSkuId())
////                                    );
////                                    if (pGoods != null) {
////                                        promotionGoods.add(pGoods);
////                                    }
////                                }
////                            });
////                }
////
////            }
//            goodsSkus.add(goodsSku);
//        }
//
//        //批量获取商品库存
//        List skuStocks = cache.multiGet(skuKeys);
//        //循环写入商品库存
//        for (int i = 0; i < skuStocks.size(); i++) {
//            goodsSkus.get(i).setQuantity(Convert.toInt(skuStocks.get(i).toString()));
//        }
//
//        //促销库存处理
////        if (!promotionKey.isEmpty()) {
////            List promotionStocks = cache.multiGet(promotionKey);
////            for (int i = 0; i < promotionKey.size(); i++) {
////                promotionGoods.get(i).setQuantity(Convert.toInt(promotionStocks.get(i).toString()));
////                Integer num = promotionGoods.get(i).getNum();
////                promotionGoods.get(i).setNum((num != null ? num : 0) + order.getOrder().getGoodsNum());
////            }
////            promotionGoodsService.updatePromotionGoodsStock(promotionGoods);
////        }
//        //商品库存，包含sku库存集合，批量更新商品库存相关
//        goodsSkuService.updateGoodsStock(goodsSkus);
//
//        log.info("订单确认，库存同步：商品信息--{}；促销信息---{}", goodsSkus, null);
//
//    }

//    /**
//     * 恢复商品库存
//     *
//     * @param order 订单
//     */
//    private void rollbackOrderStock(OrderStatusMessage.OrderDetail order) {
//
//        //sku商品
//        List<GoodsSku> goodsSkus = new ArrayList<>();
//        //sku库存key 集合
//        List<String> skuKeys = new ArrayList<>();
//        //促销商品
////        List<PromotionGoods> promotionGoods = new ArrayList<>();
//        //促销库存key 集合
//        List<String> promotionKey = new ArrayList<>();
//
//        //循环订单
//        for (OrderStatusMessage.OrderDetail.OrderItem orderItem : order.getOrderItems()) {
//            skuKeys.add(GoodsSkuService.getStockCacheKey(orderItem.getSkuId()));
//
//            GoodsSku goodsSku = new GoodsSku();
//            goodsSku.setId(orderItem.getSkuId());
//            goodsSku.setGoodsId(orderItem.getGoodsId());
//            //如果有促销信息
////            if (null != orderItem.getPromotionType() && null != orderItem.getPromotionId()) {
////
////                //如果促销有库存信息
////                String[] skuPromotions = orderItem.getPromotionType().split(",");
////                for (int i = 0; i < skuPromotions.length; i++) {
////                    int currentIndex = i;
////                    Arrays.stream(PromotionTypeEnum.haveStockPromotion).filter(promotionTypeEnum -> promotionTypeEnum.name().equals(skuPromotions[currentIndex]))
////                            .findFirst()
////                            .ifPresent(promotionTypeEnum -> {
////                                //修改砍价商品库存
////                                String promotionId = orderItem.getPromotionId().split(",")[currentIndex];
////                                //修改砍价商品库存
////                                if (promotionTypeEnum.equals(PromotionTypeEnum.KANJIA)) {
////                                    KanjiaActivity kanjiaActivity = kanjiaActivityService.getById(promotionId);
////                                    KanjiaActivityGoodsDTO kanjiaActivityGoodsDTO = kanjiaActivityGoodsService.getKanjiaGoodsDetail(kanjiaActivity.getKanjiaActivityGoodsId());
////
////                                    Integer stock = Integer.parseInt(cache.get(PromotionGoodsService.getPromotionGoodsStockCacheKey(promotionTypeEnum, promotionId, orderItem.getSkuId())).toString());
////                                    kanjiaActivityGoodsDTO.setStock(stock);
////
////                                    kanjiaActivityGoodsService.updateById(kanjiaActivityGoodsDTO);
////                                    //修改积分商品库存
////                                } else if (promotionTypeEnum.equals(PromotionTypeEnum.POINTS_GOODS)) {
////                                    PointsGoodsVO pointsGoodsVO = pointsGoodsService.getPointsGoodsDetail(promotionId);
////                                    Integer stock = Integer.parseInt(cache.get(PromotionGoodsService.getPromotionGoodsStockCacheKey(promotionTypeEnum, promotionId, orderItem.getSkuId())).toString());
////                                    pointsGoodsVO.setActiveStock(stock);
////                                    pointsGoodsService.updateById(pointsGoodsVO);
////                                } else {
////                                    PromotionGoodsSearchParams searchParams = new PromotionGoodsSearchParams();
////                                    searchParams.setPromotionType(promotionTypeEnum.name());
////                                    searchParams.setPromotionId(promotionId);
////                                    searchParams.setSkuId(orderItem.getSkuId());
////                                    PromotionGoods pGoods = promotionGoodsService.getPromotionsGoods(searchParams);
////                                    //记录需要更新的促销库存信息
////                                    promotionKey.add(
////                                            PromotionGoodsService.getPromotionGoodsStockCacheKey(
////                                                    promotionTypeEnum,
////                                                    promotionId, orderItem.getSkuId())
////                                    );
////                                    if (pGoods != null) {
////                                        promotionGoods.add(pGoods);
////                                    }
////                                }
////                            });
////                }
////
////
////            }
//            goodsSkus.add(goodsSku);
//        }
//
//        //循环订单
//        for (OrderStatusMessage.OrderDetail.OrderItem orderItem : order.getOrderItems()) {
//            skuKeys.add(GoodsSkuService.getStockCacheKey(orderItem.getSkuId()));
//            GoodsSku goodsSku = new GoodsSku();
//            goodsSku.setId(orderItem.getSkuId());
//            goodsSku.setGoodsId(orderItem.getGoodsId());
//            goodsSkus.add(goodsSku);
//        }
//        //批量获取商品库存
//        List skuStocks = cache.multiGet(skuKeys);
//        //循环写入商品SKU库存
//        for (int i = 0; i < skuStocks.size(); i++) {
//            goodsSkus.get(i).setQuantity(Convert.toInt(skuStocks.get(i).toString()));
//        }
//        //促销库存处理
////        if (!promotionKey.isEmpty()) {
////            List promotionStocks = cache.multiGet(promotionKey);
////            for (int i = 0; i < promotionKey.size(); i++) {
////                promotionGoods.get(i).setQuantity(Convert.toInt(promotionStocks.get(i).toString()));
////                Integer num = promotionGoods.get(i).getNum();
////                promotionGoods.get(i).setNum((num != null ? num : 0) + order.getOrder().getGoodsNum());
////            }
////            promotionGoodsService.updatePromotionGoodsStock(promotionGoods);
////        }
//        log.info("订单取消，库存还原：{}", goodsSkus);
//        //批量修改商品库存
//        goodsSkuService.updateGoodsStock(goodsSkus);
//
//    }
}
