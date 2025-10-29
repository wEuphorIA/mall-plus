package com.jzo2o.mall.cart.service.render.impl;

import com.jzo2o.mall.cart.model.dto.CartDTO;
import com.jzo2o.mall.cart.model.dto.CartSkuDTO;
import com.jzo2o.mall.cart.model.dto.TradeDTO;
import com.jzo2o.mall.cart.model.enums.RenderStepEnums;
import com.jzo2o.mall.cart.service.render.CartRenderStep;
import com.jzo2o.mall.cart.model.dto.PriceDetailDTO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 购物车渲染，将购物车中的各个商品，拆分到每个商家，形成购物车VO
 */
@Service
public class CartPriceRender implements CartRenderStep {

    @Override
    public RenderStepEnums step() {
        return RenderStepEnums.CART_PRICE;
    }

    @Override
    public void render(TradeDTO tradeDTO) {
        //初始化每个CartDTO的价格，及tradeDTO整个交易的价格
        initPriceDTO(tradeDTO);
        //按购物车计算价格
        buildCartPrice(tradeDTO);
        //计算整个交易的价格
        buildTradePrice(tradeDTO);


    }

    /**
     * 特殊情况下对购物车金额进行护理
     *
     * @param tradeDTO
     */
    private void initPriceDTO(TradeDTO tradeDTO) {
        //初始化每个购物车价格
        tradeDTO.getCartList().forEach(cartDTO -> cartDTO.setPriceDetailDTO(new PriceDetailDTO()));
        //初始化整个交易的价格
        tradeDTO.setPriceDetailDTO(new PriceDetailDTO());
    }

    /**
     * 购物车价格
     *
     * @param tradeDTO 购物车展示信息
     */
    void buildCartPrice(TradeDTO tradeDTO) {
        //购物车列表
        List<CartDTO> cartDTOS = tradeDTO.getCartList();

        cartDTOS.forEach(cartDTO -> {
            //累加商品价格
            cartDTO.getPriceDetailDTO().accumulationPriceDTO(
                    cartDTO.getCheckedSkuList().stream().filter(CartSkuDTO::getChecked)
                            .map(CartSkuDTO::getPriceDetailDTO).collect(Collectors.toList())
            );
            //对每个商品的购买数量列成列表
            List<Integer> skuNum = cartDTO.getSkuList().stream().filter(CartSkuDTO::getChecked)
                    .map(CartSkuDTO::getNum).collect(Collectors.toList());
            for (Integer num : skuNum) {
                //累加商品数量
                cartDTO.addGoodsNum(num);
            }
        });
    }


    /**
     * 初始化购物车
     *
     * @param tradeDTO 购物车展示信息
     */
    void buildTradePrice(TradeDTO tradeDTO) {
        tradeDTO.getPriceDetailDTO().accumulationPriceDTO(
                tradeDTO.getCartList().stream().map(CartDTO::getPriceDetailDTO).collect(Collectors.toList()));
    }

}
