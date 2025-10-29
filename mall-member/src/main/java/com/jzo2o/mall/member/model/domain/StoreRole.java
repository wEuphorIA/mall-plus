package com.jzo2o.mall.member.model.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.jzo2o.mysql.domain.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;


/**
 * 部门
 */
@Data
@TableName("ums_store_role")
@ApiModel(value = "店铺角色")
public class StoreRole extends BaseEntity {

    @ApiModelProperty(value = "角色名")
    @NotEmpty(message = "角色名称必填")
    private String name;

    @ApiModelProperty(value = "店铺id", hidden = true)
    private String storeId;

    @ApiModelProperty(value = "是否为注册默认角色")
    private Boolean defaultRole = false;

    @ApiModelProperty(value = "备注")
    private String description;
}