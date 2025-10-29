package com.jzo2o.mall.aftersale.model.domain;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.jzo2o.mall.common.enums.UserEnums;
import com.jzo2o.mysql.domain.BaseIdEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * 售后日志
 */
@Data
@TableName("oms_after_sale_log")
@ApiModel(value = "售后日志")
@NoArgsConstructor
public class AfterSaleLog extends BaseIdEntity {

    @CreatedBy
    @TableField(fill = FieldFill.INSERT)
    @ApiModelProperty(value = "创建者", hidden = true)
    private String createBy;

    @CreatedDate
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(fill = FieldFill.INSERT)
    @ApiModelProperty(value = "创建时间", hidden = true)
    private Date createTime;

    @ApiModelProperty(value = "售后服务单号")
    private String sn;

    @ApiModelProperty(value = "操作者id(可以是卖家)")
    private String operatorId;

    /**
     * @see UserEnums
     */
    @ApiModelProperty(value = "操作者类型")
    private String operatorType;


    @ApiModelProperty(value = "操作者名称")
    private String operatorName;

    @ApiModelProperty(value = "日志信息")
    private String message;

    public AfterSaleLog(String sn, String operatorId, String operatorType, String operatorName, String message) {
        this.sn = sn;
        this.operatorId = operatorId;
        this.operatorType = operatorType;
        this.operatorName = operatorName;
        this.message = message;
    }
}
