package com.jzo2o.mall.cart.service.render;


import com.jzo2o.mall.cart.model.dto.TradeDTO;
import com.jzo2o.mall.cart.model.enums.RenderStepEnums;

/**
 * 购物车渲染
 */
public interface CartRenderStep {


    /**
     * 渲染价格步骤
     *
     * @return 渲染枚举
     */
    RenderStepEnums step();

    /**
     * 渲染一笔交易
     *
     * @param tradeDTO 交易DTO
     */
    void render(TradeDTO tradeDTO);


}
