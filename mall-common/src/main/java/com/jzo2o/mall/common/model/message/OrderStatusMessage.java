package com.jzo2o.mall.common.model.message;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.jzo2o.mall.common.enums.ClientTypeEnum;
import com.jzo2o.mall.common.enums.OrderStatusEnum;
import com.jzo2o.mall.common.model.BaseEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
import java.util.List;

/**
 * 订单状态变更消息实体
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderStatusMessage {

    /**
     * 订单信息
     */
    private OrderDetail orderDetail;

    /**
     * 旧状态
     *
     * @see OrderStatusEnum
     */
    private OrderStatusEnum oldStatus;

    /**
     * 新状态
     *
     * @see OrderStatusEnum
     */
    private OrderStatusEnum newStatus;

    /**
     * 支付方式
     */
    private String paymentMethod;


    @Data
    @NoArgsConstructor
    public static class OrderDetail{

        /**
         * 订单
         */
        private Order order;

        /**
         * 子订单信息
         */
        private List<OrderItem> orderItems;

        /**
         * 订单状态
         */
        private String orderStatusValue;

        /**
         * 付款状态
         */
        private String payStatusValue;

        /**
         * 物流状态
         */
        private String deliverStatusValue;

        /**
         * 物流类型
         */
        private String deliveryMethodValue;

        /**
         * 支付类型
         */
        private String paymentMethodValue;

//    /**
//     * 发票
//     */
//    private Receipt receipt;

        //    /**
//     * 获取订单日志
//     */
//    private List<OrderLog> orderLogs;
        @ApiModelProperty(value = "价格详情")
        private String priceDetail;

        @Data
        @AllArgsConstructor
        @NoArgsConstructor
        public static class Order extends BaseEntity {
            @ApiModelProperty("订单编号")
            private String sn;

            @ApiModelProperty("交易编号 关联Trade")
            private String tradeSn;

            @ApiModelProperty(value = "店铺ID")
            private String storeId;

            @ApiModelProperty(value = "店铺名称")
            private String storeName;

            @ApiModelProperty(value = "会员ID")
            private String memberId;

            @ApiModelProperty(value = "用户名")
//    @Sensitive(strategy = SensitiveStrategy.PHONE)
            private String memberName;

            /**
             * @see OrderStatusEnum
             */
            @ApiModelProperty(value = "订单状态")
            private String orderStatus;

            @ApiModelProperty(value = "付款状态")
            private String payStatus;
            @ApiModelProperty(value = "货运状态")
            private String deliverStatus;

            @ApiModelProperty(value = "第三方付款流水号")
            private String receivableNo;

            @ApiModelProperty(value = "支付方式")
            private String paymentMethod;

            @ApiModelProperty(value = "支付时间")
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
            @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
            private Date paymentTime;

            @ApiModelProperty(value = "收件人姓名")
            private String consigneeName;

            @ApiModelProperty(value = "收件人手机")
            private String consigneeMobile;

            @ApiModelProperty(value = "配送方式")
            private String deliveryMethod;

            @ApiModelProperty(value = "地址名称， '，'分割")
            private String consigneeAddressPath;

            @ApiModelProperty(value = "地址id，'，'分割 ")
            private String consigneeAddressIdPath;

            @ApiModelProperty(value = "详细地址")
            private String consigneeDetail;

            @ApiModelProperty(value = "总价格")
            private Double flowPrice;

            @ApiModelProperty(value = "商品价格")
            private Double goodsPrice;

            @ApiModelProperty(value = "运费")
            private Double freightPrice;

            @ApiModelProperty(value = "优惠的金额")
            private Double discountPrice;

            @ApiModelProperty(value = "修改价格")
            private Double updatePrice;

            @ApiModelProperty(value = "发货单号")
            private String logisticsNo;

            @ApiModelProperty(value = "物流公司CODE")
            private String logisticsCode;

            @ApiModelProperty(value = "物流公司名称")
            private String logisticsName;

            @ApiModelProperty(value = "订单商品总重量")
            private Double weight;

            @ApiModelProperty(value = "商品数量")
            private Integer goodsNum;

            @ApiModelProperty(value = "买家订单备注")
            private String remark;

            @ApiModelProperty(value = "订单取消原因")
            private String cancelReason;

            @ApiModelProperty(value = "完成时间")
            @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
            private Date completeTime;

            @ApiModelProperty(value = "送货时间")
            @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
            private Date logisticsTime;

            @ApiModelProperty(value = "支付方式返回的交易号")
            private String payOrderNo;

            /**
             * @see ClientTypeEnum
             */
            @ApiModelProperty(value = "订单来源")
            private String clientType;

            @ApiModelProperty(value = "是否需要发票")
            private Boolean needReceipt;

            @ApiModelProperty(value = "是否为其他订单下的订单，如果是则为依赖订单的sn，否则为空")
            private String parentOrderSn = "";

            @ApiModelProperty(value = "是否为某订单类型的订单，如果是则为订单类型的id，否则为空")
            private String promotionId;

            @ApiModelProperty(value = "订单类型")
            private String orderType;

            @ApiModelProperty(value = "订单促销类型")
            private String orderPromotionType;

            @ApiModelProperty(value = "价格价格详情")
            private String priceDetail;

            @ApiModelProperty(value = "订单是否支持原路退回")
            private Boolean canReturn;

            @ApiModelProperty(value = "提货码")
            private String verificationCode;

            @ApiModelProperty(value = "分销员ID")
            private String distributionId;

            @ApiModelProperty(value = "使用的店铺会员优惠券id(,区分)")
            private String useStoreMemberCouponIds;

            @ApiModelProperty(value = "使用的平台会员优惠券id")
            private String usePlatformMemberCouponId;

            @ApiModelProperty(value = "qrCode  实物为提货码  虚拟货物为账号")
            private String qrCode;

            @ApiModelProperty(value = "自提点地址")
            private String storeAddressPath;

            @ApiModelProperty(value = "自提点电话")
            private String storeAddressMobile;

            @ApiModelProperty(value = "自提点地址经纬度")
            private String storeAddressCenter;
        }

        @Data
        @AllArgsConstructor
        @NoArgsConstructor
        public static class OrderItem extends BaseEntity{
            @ApiModelProperty(value = "订单编号")
            private String orderSn;

            @ApiModelProperty(value = "子订单编号")
            private String sn;

            @ApiModelProperty(value = "单价")
            private Double unitPrice;

            @ApiModelProperty(value = "小记")
            private Double subTotal;

            @ApiModelProperty(value = "商品ID")
            private String goodsId;

            @ApiModelProperty(value = "货品ID")
            private String skuId;

            @ApiModelProperty(value = "销售量")
            private Integer num;

            @ApiModelProperty(value = "交易编号")
            private String tradeSn;

            @ApiModelProperty(value = "图片")
            private String image;

            @ApiModelProperty(value = "商品名称")
            private String goodsName;

            @ApiModelProperty(value = "分类ID")
            private String categoryId;

            @ApiModelProperty(value = "快照id")
            private String snapshotId;

            @ApiModelProperty(value = "规格json")
            private String specs;

            @ApiModelProperty(value = "促销类型")
            private String promotionType;

            @ApiModelProperty(value = "促销id")
            private String promotionId;

            @ApiModelProperty(value = "销售金额")
            private Double goodsPrice;

            @ApiModelProperty(value = "实际金额")
            private Double flowPrice;

            @ApiModelProperty(value = "评论状态:未评论(UNFINISHED),待追评(WAIT_CHASE),评论完成(FINISHED)，")
            private String commentStatus;

            @ApiModelProperty(value = "售后状态")
            private String afterSaleStatus;

            @ApiModelProperty(value = "价格详情")
            private String priceDetail;

            @ApiModelProperty(value = "投诉状态")
            private String complainStatus;

            @ApiModelProperty(value = "交易投诉id")
            private String complainId;

            @ApiModelProperty(value = "退货商品数量")
            private Integer returnGoodsNumber;

            @ApiModelProperty(value = "退款状态")
            private String isRefund;

            @ApiModelProperty(value = "退款金额")
            private Double refundPrice;

            @ApiModelProperty(value = "已发货数量")
            private Integer deliverNumber;
        }

    }

}
