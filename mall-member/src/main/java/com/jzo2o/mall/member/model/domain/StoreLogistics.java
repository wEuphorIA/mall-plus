package com.jzo2o.mall.member.model.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.jzo2o.mall.member.model.dto.StoreLogisticsCustomerDTO;
import com.jzo2o.mysql.domain.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

/**
 * 店铺-物流公司设置
 */
@Data
@TableName("ums_store_logistics")
@ApiModel(value = "店铺-物流公司")
@AllArgsConstructor
@NoArgsConstructor
public class StoreLogistics extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "店铺ID")
    private String storeId;

    @ApiModelProperty(value = "物流公司ID")
    @NotNull
    private String logisticsId;

    @ApiModelProperty(value = "客户代码")
    private String customerName;

    @ApiModelProperty(value = "客户密码")
    private String customerPwd;

    @ApiModelProperty(value = "密钥")
    private String monthCode;

    @ApiModelProperty(value = "归属网点/网点编码")
    private String sendSite;

    @ApiModelProperty(value = "收件快递员")
    private String sendStaff;

    @ApiModelProperty(value = "是否使用电子面单")
    private boolean faceSheetFlag;

    @ApiModelProperty(value = "支付方式")
    private String payType;

    @ApiModelProperty(value = "快递类型")
    private String expType;

    public StoreLogistics(StoreLogisticsCustomerDTO storeLogisticsCustomerDTO){
        this.customerName=storeLogisticsCustomerDTO.getCustomerName();
        this.customerPwd=storeLogisticsCustomerDTO.getCustomerPwd();
        this.sendSite=storeLogisticsCustomerDTO.getSendSite();
        this.sendStaff=storeLogisticsCustomerDTO.getSendStaff();
        this.monthCode=storeLogisticsCustomerDTO.getMonthCode();
        this.faceSheetFlag=storeLogisticsCustomerDTO.isFaceSheetFlag();
        this.payType = storeLogisticsCustomerDTO.getPayType();
        this.expType = storeLogisticsCustomerDTO.getExpType();
    }



}