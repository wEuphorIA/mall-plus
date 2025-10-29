package com.jzo2o.mall.member.model.dto;

import cn.hutool.core.bean.BeanUtil;
import com.jzo2o.mall.member.model.domain.Clerk;
import com.jzo2o.mall.member.model.domain.StoreMenu;
import com.jzo2o.mall.member.model.domain.StoreRole;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 管理员VO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClerkDTO extends Clerk {

    private static final long serialVersionUID = -2378384199695839312L;

    @ApiModelProperty(value = "手机号")
    private String mobile;

    @ApiModelProperty(value = "所属部门名称")
    private String departmentTitle;

    @ApiModelProperty(value = "用户拥有角色")
    private List<StoreRole> roles;

    @ApiModelProperty(value = "用户拥有的权限")
    private List<StoreMenu> menus;


    public ClerkDTO(Clerk clerk) {
        BeanUtil.copyProperties(clerk, this);
    }

}
