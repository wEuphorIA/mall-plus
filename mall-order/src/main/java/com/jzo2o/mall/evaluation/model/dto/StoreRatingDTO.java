package com.jzo2o.mall.evaluation.model.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 评分VO
 */
@Data
public class StoreRatingDTO {

    @ApiModelProperty(value = "物流评分")
    private String deliveryScore;

    @ApiModelProperty(value = "服务评分")
    private String serviceScore;

    @ApiModelProperty(value = "描述评分")
    private String descriptionScore;

}
