package com.jzo2o.mall.cart.service.render.impl;

import com.jzo2o.mall.cart.model.dto.TradeDTO;
import com.jzo2o.mall.cart.model.enums.RenderStepEnums;
import com.jzo2o.mall.cart.service.render.CartRenderStep;
import com.jzo2o.mall.common.utils.CurrencyUtil;
import com.jzo2o.mall.cart.model.dto.PriceDetailDTO;
import com.jzo2o.redis.helper.Cache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 购物车基础价格实现
 */
@Service
public class BasePriceRender implements CartRenderStep {

    @Autowired
    private Cache cache;

    @Override
    public RenderStepEnums step() {
        return RenderStepEnums.BASEPRICE;
    }

    @Override
    public void render(TradeDTO tradeDTO) {

        //基础价格渲染
        renderBasePrice(tradeDTO);


    }

    /**
     * 基础价格渲染
     *
     * @param tradeDTO
     */
    private void renderBasePrice(TradeDTO tradeDTO) {
        tradeDTO.getCartList().forEach(
                cartDTO -> cartDTO.getCheckedSkuList().forEach(cartSkuDTO -> {
                    PriceDetailDTO priceDetailDTO = cartSkuDTO.getPriceDetailDTO();
                    priceDetailDTO.setGoodsPrice(cartSkuDTO.getSubTotal());
                    priceDetailDTO.setDiscountPrice(CurrencyUtil.sub(priceDetailDTO.getOriginalPrice(), cartSkuDTO.getSubTotal()));
                })
        );
    }


}
