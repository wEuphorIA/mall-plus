package com.jzo2o.mall.order.model.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.jzo2o.mysql.domain.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@TableName("oms_order_package")
@ApiModel(value = "订单包裹")
@NoArgsConstructor
@AllArgsConstructor
public class OrderPackage extends BaseEntity {

    @ApiModelProperty(value = "包裹单号")
    private String packageNo;

    @ApiModelProperty(value = "订单编号")
    private String orderSn;

    @ApiModelProperty(value = "发货单号")
    private String logisticsNo;

    @ApiModelProperty(value = "物流公司CODE")
    private String logisticsCode;

    @ApiModelProperty(value = "物流公司名称")
    private String logisticsName;

    @ApiModelProperty(value = "收件人手机")
    private String consigneeMobile;

    @ApiModelProperty(value = "状态")
    private String status;

}
