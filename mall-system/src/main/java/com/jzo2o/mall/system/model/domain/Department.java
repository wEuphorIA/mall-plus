package com.jzo2o.mall.system.model.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.jzo2o.mysql.domain.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;


/**
 * 部门
 */
@Data
@TableName("ams_department")
@ApiModel(value = "部门")
@EqualsAndHashCode(callSuper = true)
public class Department extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "部门名称")
    @NotNull(message = "部门名称不能为空")
    private String title;

    @ApiModelProperty(value = "父id")
    private String parentId;

    @ApiModelProperty(value = "排序值")
    private Double sortOrder;
}