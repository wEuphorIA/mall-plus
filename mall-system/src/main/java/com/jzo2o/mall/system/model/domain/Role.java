package com.jzo2o.mall.system.model.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.jzo2o.mysql.domain.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;


/**
 * 角色
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("ams_role")
@ApiModel(value = "角色")
public class Role extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "角色名")
    private String name;

    @ApiModelProperty(value = "是否为注册默认角色")
    private Boolean defaultRole;

    @ApiModelProperty(value = "备注")
    private String description;

}
