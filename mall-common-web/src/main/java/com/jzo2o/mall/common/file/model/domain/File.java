package com.jzo2o.mall.common.file.model.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.jzo2o.mysql.domain.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;


/**
 * 文件系统
 */
@Data
@TableName("cms_file")
@ApiModel(value = "文件")
public class File extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "原文件名")
    private String name;

    @ApiModelProperty(value = "存储文件名")
    private String fileKey;

    @ApiModelProperty(value = "大小")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long fileSize;

    @ApiModelProperty(value = "文件类型")
    private String fileType;

    @ApiModelProperty(value = "路径")
    private String url;

    @ApiModelProperty(value = "拥有者id")
    private String ownerId;

    @ApiModelProperty(value = "用户类型")
    private String userEnums;

    @ApiModelProperty(value = "文件夹ID")
    private String fileDirectoryId;
}