package com.jzo2o.mall.order.model.domain;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.jzo2o.common.utils.StringUtils;
import com.jzo2o.mall.common.enums.UserEnums;
import com.jzo2o.mysql.domain.BaseEntity;
import com.jzo2o.mysql.domain.BaseIdEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * 订单日志
 */
@Data
@TableName("oms_order_log")
@ApiModel(value = "订单日志")
@NoArgsConstructor
public class OrderLog extends BaseEntity {

    private static final long serialVersionUID = -1599270944927160096L;



    @ApiModelProperty(value = "订单编号")
    private String orderSn;

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

    public OrderLog(String orderSn, String operatorId, String operatorType, String operatorName, String message) {
        this.orderSn = orderSn;
        this.operatorId = operatorId;
        this.operatorType = operatorType;
        this.operatorName = operatorName;
        this.message = message;
    }
}