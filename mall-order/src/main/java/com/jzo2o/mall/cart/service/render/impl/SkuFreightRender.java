package com.jzo2o.mall.cart.service.render.impl;

import com.jzo2o.mall.cart.model.dto.CartSkuDTO;
import com.jzo2o.mall.cart.model.dto.TradeDTO;
import com.jzo2o.mall.cart.model.enums.DeliveryMethodEnum;
import com.jzo2o.mall.cart.model.enums.RenderStepEnums;
import com.jzo2o.mall.cart.service.render.CartRenderStep;
import com.jzo2o.mall.common.utils.CurrencyUtil;
import com.jzo2o.mall.member.model.domain.FreightTemplateChild;
import com.jzo2o.mall.member.model.domain.MemberAddress;
import com.jzo2o.mall.member.model.dto.FreightTemplateChildDTO;
import com.jzo2o.mall.member.model.dto.FreightTemplateDTO;
import com.jzo2o.mall.member.model.enums.FreightTemplateEnum;
import com.jzo2o.mall.member.service.FreightTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * sku 运费计算
 */
@Service
public class SkuFreightRender implements CartRenderStep {

    @Autowired
    private FreightTemplateService freightTemplateService;

    @Override
    public RenderStepEnums step() {
        return RenderStepEnums.SKU_FREIGHT;
    }

    @Override
    public void render(TradeDTO tradeDTO) {
        List<CartSkuDTO> cartSkuDTOS = tradeDTO.getCheckedSkuList();
        //会员收货地址问题处理
        MemberAddress memberAddress = tradeDTO.getMemberAddress();
        //如果收货地址为空，则抛出异常
        if (memberAddress == null) {
            return;
        }
        //选择物流的时候计算价格
        if (DeliveryMethodEnum.LOGISTICS.name().equals(tradeDTO.getCartList().get(0).getDeliveryMethod())) {
            if (memberAddress != null) {
                //运费分组信息，key是运费模版id，value是模版下的skuId
                Map<String, List<String>> freightGroups = freightTemplateGrouping(cartSkuDTOS);

                //循环运费模版
                for (Map.Entry<String, List<String>> freightTemplateGroup : freightGroups.entrySet()) {

                    //商品id列表
                    List<String> skuIds = freightTemplateGroup.getValue();

                    //当前购物车商品列表
                    List<CartSkuDTO> currentCartSkus = cartSkuDTOS.stream().filter(item -> skuIds.contains(item.getGoodsSku().getId())).collect(Collectors.toList());

                    //寻找对应对商品运费计算模版
                    FreightTemplateDTO freightTemplate = freightTemplateService.getFreightTemplate(freightTemplateGroup.getKey());
                    if (freightTemplate != null
                            && freightTemplate.getFreightTemplateChildList() != null
                            && !freightTemplate.getFreightTemplateChildList().isEmpty()) {
                        //店铺模版免运费则跳过
                        if (freightTemplate.getPricingMethod().equals(FreightTemplateEnum.FREE.name())) {
                            continue;
                        }

                        //运费模版
                        FreightTemplateChild freightTemplateChild = null;

                        //获取市级别id匹配运费模版
                        String addressId = memberAddress.getConsigneeAddressIdPath().split(",")[1];
                        for (FreightTemplateChild templateChild : freightTemplate.getFreightTemplateChildList()) {
                            //模版匹配判定
                            if (templateChild.getAreaId().contains(addressId)) {
                                freightTemplateChild = templateChild;
                                break;
                            }
                        }
                        //如果没有匹配到物流规则，则说明不支持配送
                        if (freightTemplateChild == null) {
                            if (tradeDTO.getNotSupportFreight() == null) {
                                tradeDTO.setNotSupportFreight(new ArrayList<>());
                            }
                            tradeDTO.getNotSupportFreight().addAll(currentCartSkus);
                            continue;
                        }

                        //物流规则模型创立
                        FreightTemplateChildDTO freightTemplateChildDTO = new FreightTemplateChildDTO(freightTemplateChild);
                        //模型写入运费模版设置的计费方式
                        freightTemplateChildDTO.setPricingMethod(freightTemplate.getPricingMethod());

                        //计算运费总数(件数或重量)
                        Double count = currentCartSkus.stream().mapToDouble(item ->
                                // 根据计费规则 累加计费基数
                                freightTemplateChildDTO.getPricingMethod().equals(FreightTemplateEnum.NUM.name()) ?//计费方式按件
                                        item.getNum().doubleValue() :
                                        CurrencyUtil.mul(item.getNum(), item.getGoodsSku().getWeight())//计费方式按重量
                        ).sum();

                        //计算总运费
                        Double countFreight = countFreight(count, freightTemplateChildDTO);

                        //写入SKU运费,将总运费分摊到每个sku上，剩余运费写入最后一个sku
                        resetFreightPrice(FreightTemplateEnum.valueOf(freightTemplateChildDTO.getPricingMethod()), count, countFreight, currentCartSkus);
                    }
                }
            }
        } else {
            //自提清空不配送商品
            tradeDTO.setNotSupportFreight(null);
        }
    }


    /**
     * sku运费写入
     *
     * @param freightTemplateEnum 运费计算模式
     * @param count               计费基数总数，总重量或重个数，根据运费计算模式来定
     * @param countFreight        总运费
     * @param cartSkuDTOS          与运费相关的购物车商品
     */
    private void resetFreightPrice(FreightTemplateEnum freightTemplateEnum, Double count, Double countFreight, List<CartSkuDTO> cartSkuDTOS) {

        //剩余运费 默认等于总运费
        Double surplusFreightPrice = countFreight;

        //当前下标
        int index = 1;
        for (CartSkuDTO cartSkuDTO : cartSkuDTOS) {
            //如果是最后一个 则将剩余运费直接赋值
            //PS: 循环中避免百分比累加不等于100%，所以最后一个运费不以比例计算，直接将剩余运费赋值
            if (index == cartSkuDTOS.size()) {
                cartSkuDTO.getPriceDetailDTO().setFreightPrice(surplusFreightPrice);
                break;
            }

            Double freightPrice = freightTemplateEnum == FreightTemplateEnum.NUM ?
                    CurrencyUtil.mul(countFreight, CurrencyUtil.div(cartSkuDTO.getNum(), count)) :
                    CurrencyUtil.mul(countFreight,
                            CurrencyUtil.div(CurrencyUtil.mul(cartSkuDTO.getNum(), cartSkuDTO.getGoodsSku().getWeight()), count));

            //剩余运费=总运费-当前循环的商品运费
            surplusFreightPrice = CurrencyUtil.sub(surplusFreightPrice, freightPrice);

            cartSkuDTO.getPriceDetailDTO().setFreightPrice(freightPrice);
            index++;
        }
    }

    /**
     * 运费模版分组
     *
     * @param cartSkuVOS 购物车商品
     * @return map<运费模版id ， List < skuid>>
     */
    private Map<String, List<String>> freightTemplateGrouping(List<CartSkuDTO> cartSkuVOS) {
        /*Map<String, List<String>> map = new HashMap<>();
        //循环渲染购物车商品运费价格
        for (CartSkuDTO cartSkuDTO : cartSkuVOS) {
            ////免运费判定
            String freightTemplateId = cartSkuDTO.getGoodsSku().getFreightTemplateId();
            if (Boolean.TRUE.equals(cartSkuDTO.getIsFreeFreight()) || freightTemplateId == null) {
                continue;
            }
            //包含 则value值中写入sku标识，否则直接写入新的对象，key为模版id，value为new arraylist
            if (map.containsKey(freightTemplateId)) {
                map.get(freightTemplateId).add(cartSkuDTO.getGoodsSku().getId());
            } else {
                List<String> skuIdsList = new ArrayList<>();
                skuIdsList.add(cartSkuDTO.getGoodsSku().getId());
                map.put(freightTemplateId, skuIdsList);
            }
        }*/
        Map<String, List<String>> map = cartSkuVOS.stream()
                .filter(cartSkuDTO -> !Boolean.TRUE.equals(cartSkuDTO.getIsFreeFreight()) && cartSkuDTO.getGoodsSku().getFreightTemplateId() != null)
                .collect(Collectors.groupingBy(
                        cartSkuDTO -> cartSkuDTO.getGoodsSku().getFreightTemplateId(),
                        Collectors.mapping(
                                cartSkuDTO -> cartSkuDTO.getGoodsSku().getId(),
                                Collectors.toList()
                        )
                ));
        return map;
    }


    /**
     * 计算运费
     *
     * @param count    重量/件
     * @param template 计算模版
     * @return 运费
     */
    private Double countFreight(Double count, FreightTemplateChildDTO template) {
        try {
            Double finalFreight = template.getFirstPrice();
            //不满首重 / 首件
            if (template.getFirstCompany() >= count) {
                return finalFreight;
            }
            //如果续重/续件，费用不为空，则返回
            if (template.getContinuedCompany() == 0 || template.getContinuedPrice() == 0) {
                return finalFreight;
            }

            //计算 续重 / 续件 价格
            Double continuedCount = count - template.getFirstCompany();
            return CurrencyUtil.add(finalFreight,
                    CurrencyUtil.mul(Math.ceil(continuedCount / template.getContinuedCompany()), template.getContinuedPrice()));
        } catch (Exception e) {
            e.printStackTrace();
            return 0D;
        }


    }


}
