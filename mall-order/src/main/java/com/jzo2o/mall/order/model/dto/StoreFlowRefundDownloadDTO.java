package com.jzo2o.mall.order.model.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 店铺流水下载
 */
@Data
public class StoreFlowRefundDownloadDTO extends StoreFlowPayDownloadDTO {

    @ApiModelProperty(value = "售后SN")
    private String refundSn;

}
