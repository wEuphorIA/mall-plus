package com.jzo2o.mall.cart.service.render.impl;

import com.jzo2o.mall.cart.model.dto.StoreRemarkDTO;
import com.jzo2o.mall.cart.model.dto.TradeDTO;
import com.jzo2o.mall.cart.model.enums.RenderStepEnums;
import com.jzo2o.mall.cart.service.render.CartRenderStep;
import com.jzo2o.mall.common.utils.SnowFlake;
import org.springframework.stereotype.Service;

/**
 * sn 生成
 *
 * @author Chopper
 * @since 2020-07-02 14:47
 */
@Service
public class CartSnRender implements CartRenderStep {

    @Override
    public RenderStepEnums step() {
        return RenderStepEnums.CART_SN;
    }

    @Override
    public void render(TradeDTO tradeDTO) {

        //生成各个sn
        tradeDTO.setSn(SnowFlake.createStr("T"));
        tradeDTO.getCartList().forEach(item -> {
            //写入备注
            if (tradeDTO.getStoreRemark() != null) {
                for (StoreRemarkDTO remark : tradeDTO.getStoreRemark()) {
                    if (item.getStoreId().equals(remark.getStoreId())) {
                        item.setRemark(remark.getRemark());
                    }
                }
            }
            item.setSn(SnowFlake.createStr("O"));
        });

    }
}
