package com.jzo2o.mall.product.event;

import cn.hutool.core.convert.Convert;
import com.jzo2o.mall.common.enums.ModuleEnums;
import com.jzo2o.mall.common.event.OrderStatusChangeEvent;
import com.jzo2o.mall.common.model.message.OrderStatusMessage;
import com.jzo2o.mall.product.model.domain.GoodsSku;
import com.jzo2o.mall.product.service.GoodsService;
import com.jzo2o.mall.product.service.GoodsSkuService;
import com.jzo2o.mall.search.model.domain.EsGoodsIndex;
import com.jzo2o.mall.search.service.EsGoodsIndexService;
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
import java.util.Objects;

/**
 * 订单状态修改操作库存:
 * 订单创建成功进行库存扣减,如果扣减失败要取消订单
 * 取消订单进行库存回滚
 */
@Slf4j
@Service
public class BuyCountUpdateExecute implements OrderStatusChangeEvent {

    /**
     * 缓存
     */
    @Autowired
    private Cache cache;

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
    @Autowired
    private EsGoodsIndexService goodsIndexService;
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void orderChange(OrderStatusMessage orderMessage) {

        switch (orderMessage.getNewStatus()) {

            case COMPLETED: {
                //解析出订单明细
                OrderStatusMessage.OrderDetail orderDetail = orderMessage.getOrderDetail();
                List<OrderStatusMessage.OrderDetail.OrderItem> orderItems = orderDetail.getOrderItems();

                orderItems.forEach(orderItem -> {
                    //更新商品sku购买数量
                    goodsSkuService.updateGoodsSkuBuyCount(orderItem.getSkuId(), orderItem.getNum());
                    //更新商品spu购买数量
                    goodsService.updateGoodsBuyCount(orderItem.getGoodsId(), orderItem.getNum());
                     //更新商品索引中的商品数量
                    //使用goodsIndexService查询商品索引
                    EsGoodsIndex esGoodsIndex = goodsIndexService.findById(orderItem.getSkuId());
                    esGoodsIndex.setBuyCount(esGoodsIndex.getBuyCount() + orderItem.getNum());
                    goodsIndexService.updateIndex(esGoodsIndex);

                });
            }
            default:
                break;
        }
    }

    @Override
    public ModuleEnums getModule() {
        return ModuleEnums.PRODUCT;
    }


}
