package com.jzo2o.mall.member.model.dto;

import com.jzo2o.mysql.domain.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 物流公司设置
 */

@Data
@ApiModel(value = "物流公司VO")
public class StoreLogisticsDTO extends BaseEntity {

    @ApiModelProperty(value = "物流公司ID")
    private String logisticsId;

    @ApiModelProperty(value = "物流公司名称")
    private String name;

    @ApiModelProperty(value = "已选择", notes = "如果已选择则有值，没有选择则无值")
    private String selected;

    @ApiModelProperty(value = "是否使用电子面单")
    private Boolean faceSheetFlag;
}