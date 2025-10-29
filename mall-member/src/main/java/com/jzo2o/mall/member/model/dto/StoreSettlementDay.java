package com.jzo2o.mall.member.model.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * 店铺结算日
 */
@Data
public class StoreSettlementDay {

    @ApiModelProperty(value = "店铺ID")
    private String storeId;

    @ApiModelProperty(value = "结算日")
    private LocalDateTime settlementDay;
}
