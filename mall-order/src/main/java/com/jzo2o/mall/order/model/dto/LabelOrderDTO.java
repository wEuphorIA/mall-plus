package com.jzo2o.mall.order.model.dto;

import com.jzo2o.mall.member.model.domain.StoreLogistics;
import com.jzo2o.mall.member.model.dto.StoreDeliverGoodsAddressDTO;
import com.jzo2o.mall.order.model.domain.Logistics;
import com.jzo2o.mall.order.model.domain.Order;
import com.jzo2o.mall.order.model.domain.OrderItem;
import lombok.Data;

import java.util.List;

/**
 * 电子面单DTO
 *
 */
@Data
public class LabelOrderDTO {

    //订单
    Order order;
    //订单货物
    List<OrderItem> orderItems;
    //物流公司
    Logistics logistics;
    //店铺物流公司配置
    StoreLogistics storeLogistics;
    //店铺发件地址
    StoreDeliverGoodsAddressDTO storeDeliverGoodsAddressDTO;
}
