package com.jzo2o.mall.member.model.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * 店员dto
 */
@Data
@NoArgsConstructor
public class ClerkAddDTO {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "会员用户名")
    @NotEmpty(message = "会员用户名不能为空")
    @Length(max = 30, message = "会员用户名不能超过20个字符")
    private String username;

    @ApiModelProperty(value = "会员密码")
    @NotEmpty(message = "会员密码不能为空")
    private String password;

    @NotEmpty(message = "手机号码不能为空")
    @ApiModelProperty(value = "手机号码", required = true)
//    @Sensitive(strategy = SensitiveStrategy.PHONE)
    private String mobile;

    @ApiModelProperty(value = "所属部门id")
    private String departmentId;

    @ApiModelProperty(value = "是否是超级管理员 超级管理员/普通管理员")
    private Boolean isSuper = false;

    @ApiModelProperty(value = "角色")
    private List<String> roles;

    @ApiModelProperty(value = "会员id", required = true)
    private String memberId;

    @ApiModelProperty(value = "是否是店主", hidden = true)
    private Boolean shopkeeper = false;

    @ApiModelProperty(value = "店铺id", hidden = true)
    private String storeId;


}
