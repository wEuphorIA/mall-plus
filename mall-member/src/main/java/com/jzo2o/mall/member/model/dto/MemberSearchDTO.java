package com.jzo2o.mall.member.model.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 会员搜索VO
 *
 * @author Bulbasaur
 * @since 2020/12/15 10:48
 */
@Data
public class MemberSearchDTO {

    @ApiModelProperty(value = "用户名")
    private String username;

    @ApiModelProperty(value = "昵称")
    private String nickName;

    @ApiModelProperty(value = "用户手机号码")
    private String mobile;
    @ApiModelProperty(value = "会员状态")
    private String disabled;
}
