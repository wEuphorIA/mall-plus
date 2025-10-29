package com.jzo2o.mall.order.model.dto;


import com.jzo2o.mall.cart.model.enums.DeliveryMethodEnum;
import com.jzo2o.mall.common.enums.PaymentMethodEnum;
import com.jzo2o.mall.order.model.domain.Order;
import com.jzo2o.mall.order.model.domain.OrderItem;
import com.jzo2o.mall.order.model.domain.OrderLog;
import com.jzo2o.mall.order.model.enums.DeliverStatusEnum;
import com.jzo2o.mall.common.enums.OrderStatusEnum;
import com.jzo2o.mall.common.enums.PayStatusEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 订单详情VO
 */
@Data
@NoArgsConstructor
public class OrderDetailDTO implements Serializable {


    private static final long serialVersionUID = -6293102172184734928L;

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

    /**
     * 获取订单日志
     */
    private List<OrderLog> orderLogs;
    @ApiModelProperty(value = "价格详情")
    private String priceDetail;

    public OrderDetailDTO(Order order, List<OrderItem> orderItems, List<OrderLog> orderLogs/*, Receipt receipt*/) {
        this.order = order;
        this.orderItems = orderItems;
        this.orderLogs = orderLogs;
//        this.receipt =  receipt;
    }

    /**
     * 可操作类型
     */
    public AllowOperationDTO getAllowOperationVO() {
        return new AllowOperationDTO(this.order);
    }

    public String getOrderStatusValue() {
        try {
            return OrderStatusEnum.valueOf(order.getOrderStatus()).description();
        } catch (Exception e) {
            return "";
        }
    }

    public String getPayStatusValue() {
        try {
            return PayStatusEnum.valueOf(order.getPayStatus()).description();
        } catch (Exception e) {
            return "";
        }

    }

    public String getDeliverStatusValue() {
        try {
            return DeliverStatusEnum.valueOf(order.getDeliverStatus()).getDescription();
        } catch (Exception e) {
            return "";
        }
    }

    public String getDeliveryMethodValue() {
        try {
            return DeliveryMethodEnum.valueOf(order.getDeliveryMethod()).getDescription();
        } catch (Exception e) {
            return "";
        }
    }

    public String getPaymentMethodValue() {
        try {
            return PaymentMethodEnum.valueOf(order.getPaymentMethod()).paymentName();
        } catch (Exception e) {
            return "";
        }
    }
}