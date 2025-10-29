package com.jzo2o.mall.member.model.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 店铺详细VO
 *
 * @author pikachu
 * @since 2020-03-09 21:53:20
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class StoreDetailDTO extends StoreEditDTO {

    @ApiModelProperty(value = "会员名称")
    private String memberName;

}
