package com.jzo2o.mall.cart.service.render;

import com.jzo2o.mall.cart.model.dto.TradeDTO;
import com.jzo2o.mall.cart.model.enums.CartTypeEnum;
import com.jzo2o.mall.cart.model.enums.RenderStepEnums;
import com.jzo2o.mall.cart.service.CartService;
import com.jzo2o.mall.common.exception.ServiceException;
import com.jzo2o.mall.order.model.domain.Trade;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 交易构造&&创建
 */
@Service
@Slf4j
public class TradeBuilder {

    /**
     * 购物车渲染步骤
     */
    @Autowired
    private List<CartRenderStep> cartRenderSteps;
//    /**
//     * 交易
//     */
//    @Autowired
//    private TradeService tradeService;
    /**
     * 购物车业务
     */
    @Autowired
    private CartService cartService;


    /**
     * 构造购物车
     * 购物车与结算信息不一致的地方主要是运费计算，购物车没有运费计算，其他规则都是一致都
     *
     * @param checkedWay 购物车类型
     * @return 购物车展示信息
     */
    public TradeDTO buildCart(CartTypeEnum checkedWay) {
        //读取对应购物车的商品信息
        TradeDTO tradeDTO = cartService.readDTO(checkedWay);

        //购物车需要将交易中的优惠券取消掉
//        if (checkedWay.equals(CartTypeEnum.CART)) {
//            tradeDTO.setStoreCoupons(null);
//            tradeDTO.setPlatformCoupon(null);
//        }

        //按照计划进行渲染
        renderCartBySteps(tradeDTO, RenderStepStatement.cartRender);
        return tradeDTO;
    }

    /**
     * 构造结算页面
     */
    public TradeDTO buildChecked(CartTypeEnum checkedWay) {
        //读取对应购物车的商品信息
        TradeDTO tradeDTO = cartService.readDTO(checkedWay);
        //需要对购物车渲染
//        if (isSingle(checkedWay)) {
//            renderCartBySteps(tradeDTO, RenderStepStatement.checkedSingleRender);
//        } else if (checkedWay.equals(CartTypeEnum.PINTUAN)) {
//            renderCartBySteps(tradeDTO, RenderStepStatement.pintuanTradeRender);
//        } else {
//            renderCartBySteps(tradeDTO, RenderStepStatement.checkedRender);
//        }
//        if (checkedWay.equals(CartTypeEnum.SECKILL)) {
//            renderCartBySteps(tradeDTO, RenderStepStatement.seckillRender);
//        } else {
//            renderCartBySteps(tradeDTO, RenderStepStatement.checkedRender);
//        }
        renderCartBySteps(tradeDTO, RenderStepStatement.checkedRender);
        return tradeDTO;
    }

//    /**
//     * 创建一笔交易
//     * 1.构造交易
//     * 2.创建交易
//     *
//     * @param tradeDTO 交易模型
//     * @return 交易信息
//     */
//    public Trade createTrade(TradeDTO tradeDTO) {
//
//        //需要对购物车渲染
//        if (isSingle(tradeDTO.getCartTypeEnum())) {
//            renderCartBySteps(tradeDTO, RenderStepStatement.singleTradeRender);
//        } else if (tradeDTO.getCartTypeEnum().equals(CartTypeEnum.PINTUAN)) {
//            renderCartBySteps(tradeDTO, RenderStepStatement.pintuanTradeRender);
//        } else {
//            renderCartBySteps(tradeDTO, RenderStepStatement.tradeRender);
//        }
//        //添加order订单及order_item子订单并返回
//        return tradeService.createTrade(tradeDTO);
//    }

    /**
     * 根据购物车构造交易对象
     *
     * @param tradeDTO 交易模型
     * @return 交易信息
     */
    public TradeDTO renderTradeByCart(TradeDTO tradeDTO){
        renderCartBySteps(tradeDTO, RenderStepStatement.tradeRender);
        return tradeDTO;
    }
    /**
     * 构造秒杀交易对象
     *
     * @param tradeDTO 交易模型
     * @return 交易信息
     */
    public TradeDTO renderSeckillTrade(TradeDTO tradeDTO){
        renderCartBySteps(tradeDTO, RenderStepStatement.seckillTradeRender);
        return tradeDTO;
    }

//
//    /**
//     * 是否为单品渲染
//     *
//     * @param checkedWay 购物车类型
//     * @return 返回是否单品
//     */
//    private boolean isSingle(CartTypeEnum checkedWay) {
//        //拼团   积分   砍价商品
//
//        return (checkedWay.equals(CartTypeEnum.POINTS) || checkedWay.equals(CartTypeEnum.KANJIA));
//    }

    /**
     * 根据渲染步骤，渲染购物车信息
     *
     * @param tradeDTO      交易DTO
     * @param defaultRender 渲染枚举
     */
    private void renderCartBySteps(TradeDTO tradeDTO, RenderStepEnums[] defaultRender) {
        for (RenderStepEnums step : defaultRender) {
            for (CartRenderStep render : cartRenderSteps) {
                try {
                    if (render.step().equals(step)) {
                        render.render(tradeDTO);
                    }
                } catch (ServiceException e) {
                    throw e;
                } catch (Exception e) {
                    log.error("购物车{}渲染异常：", render.getClass(), e);
                }
            }
        }
    }
}
