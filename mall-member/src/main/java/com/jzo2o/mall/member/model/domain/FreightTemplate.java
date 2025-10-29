package com.jzo2o.mall.member.model.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.jzo2o.mall.member.model.enums.FreightTemplateEnum;
import com.jzo2o.mysql.domain.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;

/**
 * 运费模板
 */
@Data
@TableName("ums_freight_template")
@ApiModel(value = "运费模板")
public class FreightTemplate extends BaseEntity {

    @ApiModelProperty(value = "店铺ID", hidden = true)
    private String storeId;

    @NotEmpty(message = "模板名称不能为空")
    @ApiModelProperty(value = "模板名称")
    private String name;

    /**
     * @see FreightTemplateEnum
     */
    @NotEmpty(message = "计价方式不能为空")
    @ApiModelProperty(value = "计价方式：按件、按重量", allowableValues = "WEIGHT,NUM,FREE")
    private String pricingMethod;


}
