package com.jzo2o.mall.cart.service.render.impl;

import com.jzo2o.mall.cart.model.dto.CartDTO;
import com.jzo2o.mall.cart.model.dto.CartSkuDTO;
import com.jzo2o.mall.cart.model.dto.TradeDTO;
import com.jzo2o.mall.cart.model.enums.RenderStepEnums;
import com.jzo2o.mall.cart.service.render.CartRenderStep;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 勾选商品过滤
 */
@Service
public class CheckedFilterRender implements CartRenderStep {

    @Override
    public RenderStepEnums step() {
        return RenderStepEnums.CHECKED_FILTER;
    }

    @Override
    public void render(TradeDTO tradeDTO) {
        //将购物车到sku未选择信息过滤
        List<CartSkuDTO> collect = tradeDTO.getSkuList().stream().filter(i -> Boolean.TRUE.equals(i.getChecked())).collect(Collectors.toList());
        tradeDTO.setSkuList(collect);

        //购物车信息过滤
        List<CartDTO> cartDTOList = new ArrayList<>();
        //循环购物车信息
        for (CartDTO cartDTO : tradeDTO.getCartList()) {
            //如果商品选中，则加入到对应购物车
            cartDTO.setSkuList(cartDTO.getCheckedSkuList());
            cartDTOList.add(cartDTO);
        }
        tradeDTO.setCartList(cartDTOList);
    }


}
