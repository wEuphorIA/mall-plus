package com.jzo2o.mall.cart.model.dto;

import com.jzo2o.mall.cart.model.enums.CartTypeEnum;
import com.jzo2o.mall.member.model.domain.MemberAddress;
import com.jzo2o.mall.order.model.dto.OrderDTO;
import com.jzo2o.mall.order.model.dto.ReceiptInputDTO;
import com.jzo2o.mall.promotion.model.domain.MemberCoupon;
import com.jzo2o.mall.promotion.model.dto.MemberCouponExtDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 购物车视图
 */
@Data
public class TradeDTO implements Serializable {

    private static final long serialVersionUID = -3137165707807057810L;

    @ApiModelProperty(value = "sn")
    private String sn;

    @ApiModelProperty(value = "购物车列表(以卖家为单位)")
    private List<CartDTO> cartList;

    @ApiModelProperty(value = "整笔交易中所有的规格商品")
    private List<CartSkuDTO> skuList;

    @ApiModelProperty(value = "此商品价格流水计算")
    private PriceDetailDTO priceDetailDTO;


    @ApiModelProperty(value = "发票信息")
    private ReceiptInputDTO receipt;

    @ApiModelProperty(value = "是否需要发票")
    private Boolean needReceipt;


    @ApiModelProperty(value = "不支持配送方式")
    private List<CartSkuDTO> notSupportFreight;

    /**
     * 购物车类型
     */
    private CartTypeEnum cartTypeEnum;
    /**
     * 店铺备注
     */
    private List<StoreRemarkDTO> storeRemark;

//    /**
//     * sku促销连线 包含满优惠
//     * <p>
//     * KEY值为 sku_id+"_"+SuperpositionPromotionEnum
//     * VALUE值为 对应的活动ID
//     *
//     * @see SuperpositionPromotionEnum
//     */
//    private Map<String, String> skuPromotionDetail;
//
    /**
     * 使用平台优惠券，一笔订单只能使用一个平台优惠券
     */
    private MemberCouponDTO platformCoupon;

    /**
     * key 为商家id
     * value 为商家优惠券
     * 店铺优惠券
     */
    private Map<String, MemberCouponDTO> storeCoupons;

    /**
     * 可用优惠券列表
     */
    private List<MemberCoupon> canUseCoupons;

    /**
     * 无法使用优惠券无法使用的原因
     */
    private List<MemberCouponExtDTO> cantUseCoupons;

    /**
     * 收货地址
     */
    private MemberAddress memberAddress;

//    /**
//     * 自提地址
//     */
//    private StoreAddress storeAddress;

    /**
     * 客户端类型
     */
    private String clientType;

    /**
     * 买家名称
     */
    private String memberName;

    /**
     * 买家id
     */
    private String memberId;

    /**
     * 分销商id
     */
//    private String distributionId;


    /**
     * 订单DTO
     */
//    private List<OrderDTO> orderDTO;

    private List<CartSkuDTO> checkedSkuList;

    public TradeDTO(CartTypeEnum cartTypeEnum) {
        this.cartTypeEnum = cartTypeEnum;

        this.skuList = new ArrayList<>();
        this.cartList = new ArrayList<>();
//        this.skuPromotionDetail = new HashMap<>();
        this.storeCoupons = new HashMap<>();
        this.priceDetailDTO = new PriceDetailDTO();
        this.cantUseCoupons = new ArrayList<>();
        this.canUseCoupons = new ArrayList<>();
        this.needReceipt = false;
    }

    public TradeDTO() {
        this(CartTypeEnum.CART);
    }

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

    public void removeCoupon() {
        this.canUseCoupons = new ArrayList<>();
        this.cantUseCoupons = new ArrayList<>();
    }
}
