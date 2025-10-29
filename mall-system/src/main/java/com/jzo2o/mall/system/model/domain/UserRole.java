package com.jzo2o.mall.system.model.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.jzo2o.mysql.domain.BaseIdEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 用户角色
 */
@Data
@TableName("ams_user_role")
@ApiModel(value = "用户角色")
public class UserRole extends BaseIdEntity {

    @ApiModelProperty(value = "用户唯一id")
    private String userId;

    @ApiModelProperty(value = "角色唯一id")
    private String roleId;

    public UserRole(String userId, String roleId) {
        this.userId = userId;
        this.roleId = roleId;
    }

    public UserRole() {

    }
}
