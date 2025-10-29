package com.jzo2o.mall.cart.service.render.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.jzo2o.mall.cart.model.dto.CartDTO;
import com.jzo2o.mall.cart.model.dto.CartSkuDTO;
import com.jzo2o.mall.cart.model.dto.TradeDTO;
import com.jzo2o.mall.cart.model.enums.CartTypeEnum;
import com.jzo2o.mall.cart.model.enums.DeliveryMethodEnum;
import com.jzo2o.mall.cart.model.enums.RenderStepEnums;
import com.jzo2o.mall.cart.service.render.CartRenderStep;
import com.jzo2o.mall.member.service.MemberService;
import com.jzo2o.mall.product.model.domain.GoodsSku;
import com.jzo2o.mall.common.enums.GoodsAuthEnum;
import com.jzo2o.mall.common.enums.GoodsStatusEnum;
import com.jzo2o.mall.promotion.model.enums.PromotionTypeEnum;
import com.jzo2o.mall.product.service.GoodsSkuService;
import com.jzo2o.mall.promotion.model.domain.Coupon;
import com.jzo2o.mall.promotion.model.dto.CouponDTO;
import com.jzo2o.mall.promotion.service.PromotionGoodsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 商品有效性校验
 */
@Service
@Slf4j
public class CheckDataRender implements CartRenderStep {

    @Autowired
    private GoodsSkuService goodsSkuService;

//    @Autowired
//    private OrderService orderService;

    @Autowired
    private MemberService memberService;

//    @Autowired
//    private WholesaleService wholesaleService;
//
//    /**
//     * 商品索引
//     */
    @Autowired
    private PromotionGoodsService promotionGoodsService;

    @Override
    public RenderStepEnums step() {
        return RenderStepEnums.CHECK_DATA;
    }

    @Override
    public void render(TradeDTO tradeDTO) {

        //校验商品有效性
        checkData(tradeDTO);

        //店铺分组数据初始化，按店铺将商品分组，分组后的数据存储到了CartList属性中
        groupStore(tradeDTO);

    }

    /**
     * 校验商品属性
     *
     * @param tradeDTO 购物车视图
     */
    private void checkData(TradeDTO tradeDTO) {
        //购物车中的所有商品
        List<CartSkuDTO> cartSkuDtOS = tradeDTO.getSkuList();

        //循环购物车中的商品
        for (CartSkuDTO cartSkuDTO : cartSkuDtOS) {

            //如果失效，确认sku为未选中状态
            if (Boolean.TRUE.equals(cartSkuDTO.getInvalid())) {
                //设置购物车未选中
                cartSkuDTO.setChecked(false);
            }

            //缓存中的商品信息
            GoodsSku dataSku = goodsSkuService.getGoodsSkuByIdFromCache(cartSkuDTO.getGoodsSku().getId());

            //商品上架状态判定  sku为空、sku非上架状态、sku审核不通过
            boolean checkGoodsStatus = dataSku == null || !GoodsAuthEnum.PASS.name().equals(dataSku.getAuthFlag()) || !GoodsStatusEnum.UPPER.name().equals(dataSku.getMarketEnable());
            //商品有效性判定 sku不为空且sku的更新时间不为空且sku的更新时间在购物车sku的更新时间之后(说明商品在加入购物车后进行了更新,购物车中的商品为无效商品)
            boolean checkGoodsValid = dataSku != null &&
                    dataSku.getUpdateTime() != null &&
                    cartSkuDTO.getGoodsSku() != null &&
                    cartSkuDTO.getGoodsSku().getUpdateTime() != null &&
                    dataSku.getUpdateTime().isAfter(cartSkuDTO.getGoodsSku().getUpdateTime());


            if (checkGoodsStatus || checkGoodsValid) {
                if (checkGoodsValid) {
                    //根据sku构造购物车sku信息并计算价格
                    cartSkuDTO.rebuildBySku(dataSku);
                }
                //如果商品(sku)未上架
                if (checkGoodsStatus) {
                    //设置购物车未选中
                    cartSkuDTO.setChecked(false);
                    //设置购物车此sku商品已失效
                    cartSkuDTO.setInvalid(true);
                    //设置失效消息
                    cartSkuDTO.setErrorMessage("商品已下架");
                    continue;
                }

            }

            //商品库存判定
            if (dataSku.getQuantity() < cartSkuDTO.getNum()) {
                //设置购物车未选中
                cartSkuDTO.setChecked(false);
                //设置失效消息
                cartSkuDTO.setErrorMessage("商品库存不足,现有库存数量[" + dataSku.getQuantity() + "]");
            }
        }
    }

    /**
     * 店铺分组
     *
     * @param tradeDTO
     */
    private void groupStore(TradeDTO tradeDTO) {
        //渲染的购物车
        List<CartDTO> cartList = new ArrayList<>();
        if (tradeDTO.getCartList() == null || tradeDTO.getCartList().size() == 0) {
            //根据店铺分组
            Map<String, List<CartSkuDTO>> storeCollect = tradeDTO.getSkuList().stream().collect(Collectors.groupingBy(CartSkuDTO::getStoreId));
            for (Map.Entry<String, List<CartSkuDTO>> storeCart : storeCollect.entrySet()) {
                if (!storeCart.getValue().isEmpty()) {
                    CartDTO cartDTO = new CartDTO(storeCart.getValue().get(0));
//                    if (CharSequenceUtil.isEmpty(cartDTO.getDeliveryMethod())) {
//                        //默认物流
//                        cartDTO.setDeliveryMethod(DeliveryMethodEnum.LOGISTICS.name());
//                    }
                    cartDTO.setSkuList(storeCart.getValue());
//                    try {
//                        //筛选属于当前店铺的优惠券
//                        storeCart.getValue().forEach(i -> i.getPromotionMap().forEach((key, value) -> {
//                            if (key.contains(PromotionTypeEnum.COUPON.name())) {
//                                JSONObject promotionsObj = JSONUtil.parseObj(value);
//                                Coupon coupon = JSONUtil.toBean(promotionsObj, Coupon.class);
//                                if (key.contains(PromotionTypeEnum.COUPON.name()) && coupon.getStoreId().equals(storeCart.getKey())) {
//                                    cartDTO.getCanReceiveCoupon().add(new CouponDTO(coupon));
//                                }
//                            }
//                        }));
//                    } catch (Exception e) {
//                        log.error("筛选属于当前店铺的优惠券发生异常！", e);
//                    }
                    //只要该店铺有选中的sku则选中店铺
                    storeCart.getValue().stream().filter(i -> Boolean.TRUE.equals(i.getChecked())).findFirst().ifPresent(cartSkuDTO -> cartDTO.setChecked(true));
                    cartList.add(cartDTO);
                }
            }
            tradeDTO.setCartList(cartList);
        }

    }

}
