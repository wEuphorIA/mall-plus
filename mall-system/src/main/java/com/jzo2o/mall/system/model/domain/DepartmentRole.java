package com.jzo2o.mall.system.model.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.jzo2o.mysql.domain.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * 角色部门绑定关系
 */
@Data
@TableName("ams_department_role")
@ApiModel(value = "角色部门")
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentRole extends BaseEntity {


    private static final long serialVersionUID = 2342812932116647050L;

    @ApiModelProperty(value = "角色id")
    private String roleId;

    @ApiModelProperty(value = "部门id")
    private String departmentId;

}