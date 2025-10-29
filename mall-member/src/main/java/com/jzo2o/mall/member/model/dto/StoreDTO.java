package com.jzo2o.mall.member.model.dto;

import com.jzo2o.mall.member.model.domain.Store;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 店铺VO
 */
@Data
public class StoreDTO extends Store {

    @ApiModelProperty(value = "库存预警数量")
    private Integer stockWarning;

    @ApiModelProperty(value = "登录用户的昵称")
    private String nickName;

}
