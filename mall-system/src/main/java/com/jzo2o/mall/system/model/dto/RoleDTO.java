package com.jzo2o.mall.system.model.dto;

import com.jzo2o.mall.system.model.domain.Role;
import com.jzo2o.mall.system.model.domain.RoleMenu;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * RoleVO
 */
@Data
public class RoleDTO extends Role {

    private static final long serialVersionUID = 8625345346785692513L;

    @ApiModelProperty(value = "拥有权限")
    private List<RoleMenu> roleMenus;
}
