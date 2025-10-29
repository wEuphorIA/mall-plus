package com.jzo2o.mall.product.event;

import cn.hutool.core.convert.Convert;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jzo2o.mall.common.enums.GoodsAuthEnum;
import com.jzo2o.mall.common.enums.GoodsStatusEnum;
import com.jzo2o.mall.common.enums.ModuleEnums;
import com.jzo2o.mall.common.enums.PayStatusEnum;
import com.jzo2o.mall.common.event.OrderStatusChangeEvent;
import com.jzo2o.mall.common.event.ProductStatusChangeEvent;
import com.jzo2o.mall.common.model.message.OrderStatusMessage;
import com.jzo2o.mall.common.model.message.ProductStatusMessage;
import com.jzo2o.mall.product.model.domain.Goods;
import com.jzo2o.mall.product.model.domain.GoodsSku;
import com.jzo2o.mall.product.model.dto.GoodsSearchParamsDTO;
import com.jzo2o.mall.product.service.GoodsService;
import com.jzo2o.mall.product.service.GoodsSkuService;
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
 * 商品态修改更新商品索引
 */
@Slf4j
@Service
public class ProductIndexUpdateExecute implements ProductStatusChangeEvent {

    @Autowired
    private EsGoodsIndexService goodsIndexService;

    @Autowired
    private GoodsService goodsService;

    @Override
//    @Transactional(rollbackFor = Exception.class)
    public void onChange(ProductStatusMessage productStatusMessage) {
        String goodsId = productStatusMessage.getGoodsId();
        //商品上架状态
        GoodsStatusEnum goodsStatusEnum = productStatusMessage.getGoodsStatusEnum();
        //商品审核状态
        GoodsAuthEnum goodsAuthEnum = productStatusMessage.getGoodsAuthEnum();
        //更新es索引,如果上架则更新索引，否则删除索引
        goodsIndexService.updateGoodsIndex(goodsId,goodsAuthEnum,goodsStatusEnum);

    }

    @Override
    public ModuleEnums getModule() {
        return ModuleEnums.PRODUCT;
    }

}
