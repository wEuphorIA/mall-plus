package com.jzo2o.mall.common.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDateTime;


/**
 * 数据库基础实体类
 */
@Data
@JsonIgnoreProperties(value = { "handler", "fieldHandler"})
@AllArgsConstructor
@NoArgsConstructor
public abstract class BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;


    @ApiModelProperty(value = "唯一标识", hidden = true)
    private String id;


    @ApiModelProperty(value = "创建者", hidden = true)
    private String createBy;

    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "创建时间", hidden = true)
    private LocalDateTime createTime;

    @ApiModelProperty(value = "更新者", hidden = true)
    private String updateBy;

    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "更新时间", hidden = true)
    private LocalDateTime updateTime;

    @ApiModelProperty(value = "删除标志 true/false 删除/未删除", hidden = true)
    private Boolean deleteFlag;

}
