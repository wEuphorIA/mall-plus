package com.jzo2o.mall.member.model.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 店铺营业执照信息
 *
 */
@Data
public class StoreLicenceDTO {

    @ApiModelProperty(value = "公司名称")
    private String companyName;

    @ApiModelProperty(value = "公司地址")
    private String companyAddress;

    @ApiModelProperty(value = "公司地址地区")
    private String companyAddressPath;

    @ApiModelProperty(value = "营业执照电子版")
    private String licencePhoto;

    @ApiModelProperty(value = "法定经营范围")
    private String scope;

    @ApiModelProperty(value = "员工总数")
    private Integer employeeNum;
}
