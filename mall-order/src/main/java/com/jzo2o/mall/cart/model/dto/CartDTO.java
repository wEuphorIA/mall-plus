package com.jzo2o.mall.cart.model.dto;

import com.jzo2o.mall.cart.model.enums.DeliveryMethodEnum;
import com.jzo2o.mall.promotion.model.domain.MemberCoupon;
import com.jzo2o.mall.promotion.model.dto.CouponDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


/**
 * 购物车展示VO
 */
@Data
@ApiModel(description = "购物车")
@NoArgsConstructor
public class CartDTO extends CartBaseDTO implements Serializable {

    private static final long serialVersionUID = -5651775413457562422L;

    @ApiModelProperty(value = "购物车中的产品列表")
    private List<CartSkuDTO> skuList;

    @ApiModelProperty(value = "sn")
    private String sn;

    @ApiModelProperty(value = "购物车页展示时，店铺内的商品是否全选状态.1为店铺商品全选状态,0位非全选")
    private Boolean checked;

    @ApiModelProperty(value = "使用的优惠券列表")
    private List<MemberCoupon> couponList;

    @ApiModelProperty(value = "使用的优惠券列表")
    private List<CouponDTO> canReceiveCoupon;


    @ApiModelProperty(value = "赠送积分")
    private Integer giftPoint;

    @ApiModelProperty(value = "重量")
    private Double weight;

    @ApiModelProperty(value = "购物车商品数量")
    private Integer goodsNum;

    @ApiModelProperty(value = "购物车商品数量")
    private String remark;

    /**
     * @see DeliveryMethodEnum
     */
    @ApiModelProperty(value = "配送方式")
    private String deliveryMethod = DeliveryMethodEnum.LOGISTICS.name();

    @ApiModelProperty(value = "已参与的的促销活动提示，直接展示给客户")
    private String promotionNotice;

    public CartDTO(CartSkuDTO cartSkuDTO) {
        this.setStoreId(cartSkuDTO.getStoreId());
        this.setStoreName(cartSkuDTO.getStoreName());
        this.setDeliveryMethod(cartSkuDTO.getDeliveryMethod());
        this.setSkuList(new ArrayList<>());
        this.setCouponList(new ArrayList<>());
//        this.setGiftList(new ArrayList<>());
//        this.setGiftCouponList(new ArrayList<>());
        this.setCanReceiveCoupon(new ArrayList<>());
        this.setChecked(false);
//        this.isFull = false;
        this.weight = 0d;
        this.giftPoint = 0;
        this.remark = "";
    }

    public void addGoodsNum(Integer goodsNum) {
        if (this.goodsNum == null) {
            this.goodsNum = goodsNum;
        } else {
            this.goodsNum += goodsNum;
        }
    }

    private List<CartSkuDTO> checkedSkuList;
    /**
     * 过滤购物车中已选择的sku
     *
     * @return
     */
    public List<CartSkuDTO> getCheckedSkuList() {
        if (skuList != null && !skuList.isEmpty()) {
            return skuList.stream().filter(CartSkuDTO::getChecked).collect(Collectors.toList());
        }
        return skuList;
    }

    public void setCheckedSkuList(List<CartSkuDTO> checkedSkuList) {
        this.checkedSkuList = checkedSkuList;
    }
}
