package com.jzo2o.mall.system.model.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.jzo2o.mysql.domain.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;


/**
 * 角色权限绑定关系
 */
@Data
@TableName("ams_role_menu")
@ApiModel(value = "角色权限")
public class RoleMenu extends BaseEntity {

    private static final long serialVersionUID = -4680260092546996026L;

    @ApiModelProperty(value = "角色id")
    private String roleId;

    @ApiModelProperty(value = "菜单")
    private String menuId;

    @ApiModelProperty(value = "是否拥有操作数据权限，为否则只有查看权限")
    private Boolean isSuper;

}