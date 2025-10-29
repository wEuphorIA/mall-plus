package com.jzo2o.mall.system.model.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.jzo2o.mysql.domain.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * 设置
 */
@Data
@TableName("ams_setting")
@ApiModel(value = "配置")
@NoArgsConstructor
public class Setting extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "配置值value")
    private String settingValue;

}