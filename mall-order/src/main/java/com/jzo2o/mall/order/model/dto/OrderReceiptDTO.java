package com.jzo2o.mall.order.model.dto;

import com.jzo2o.mall.order.model.domain.Receipt;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;


/**
 * 订单发票
 */
@Data
@ApiModel(value = "订单发票")
public class OrderReceiptDTO extends Receipt {

    @ApiModelProperty(value = "订单状态")
    private String orderStatus;

}
