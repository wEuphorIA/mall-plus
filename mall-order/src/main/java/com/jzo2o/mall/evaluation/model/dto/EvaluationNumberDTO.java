package com.jzo2o.mall.evaluation.model.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 评价数量VO
 */
@Data
public class EvaluationNumberDTO {

    @ApiModelProperty(value = "全部评价")
    private Integer all;

    @ApiModelProperty(value = "好评数量")
    private Integer good;

    @ApiModelProperty(value = "中评数量")
    private Integer moderate;

    @ApiModelProperty(value = "差评数量")
    private Integer worse;

    @ApiModelProperty(value = "有图数量")
    private Long haveImage;
}
