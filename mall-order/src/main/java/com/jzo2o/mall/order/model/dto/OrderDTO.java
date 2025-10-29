package com.jzo2o.mall.order.model.dto;

import cn.hutool.core.bean.BeanUtil;
import com.jzo2o.mall.order.model.domain.Order;
import com.jzo2o.mall.order.model.domain.OrderItem;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 订单vo
 */
@Data
@NoArgsConstructor
public class OrderDTO extends Order {


    private static final long serialVersionUID = 5820637554656388777L;

    @ApiModelProperty(value = "订单商品项目")
    private List<OrderItem> orderItems;


    public OrderDTO(Order order, List<OrderItem> orderItems){
        BeanUtil.copyProperties(order, this);
        this.setOrderItems(orderItems);
    }
}
