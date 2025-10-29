package com.jzo2o.mall.order.model.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * 部分发货参数封装
 *
 */
@Data
public class PartDeliveryParamsDTO {

    @ApiModelProperty(value = "订单号")
    private String orderSn;

    @ApiModelProperty(value = "发货单号")
    private String logisticsNo;

    @ApiModelProperty(value = "发货方式")
    private String logisticsId;

    @ApiModelProperty(value = "物流详细")
    private List<PartDeliveryDTO> partDeliveryDTOList;
}
