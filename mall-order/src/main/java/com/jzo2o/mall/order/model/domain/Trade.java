package com.jzo2o.mall.order.model.domain;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.annotation.TableName;
import com.jzo2o.mall.cart.model.dto.TradeDTO;
import com.jzo2o.mall.cart.model.enums.DeliveryMethodEnum;
import com.jzo2o.mall.common.enums.PayStatusEnum;
import com.jzo2o.mysql.domain.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * 交易
 */
@Data
@TableName("oms_trade")
@ApiModel(value = "交易")
@NoArgsConstructor
public class Trade extends BaseEntity {

    private static final long serialVersionUID = 5177608752643561827L;

    @ApiModelProperty(value = "交易编号")
    private String sn;

    @ApiModelProperty(value = "买家id")
    private String memberId;

    @ApiModelProperty(value = "买家用户名")
    private String memberName;

    /**
     * @see PayStatusEnum
     */
    @ApiModelProperty(value = "支付方式")
    private String paymentMethod;

    /**
     * @see PayStatusEnum
     */
    @ApiModelProperty(value = "付款状态")
    private String payStatus;

    @ApiModelProperty(value = "总价格")
    private Double flowPrice;

    @ApiModelProperty(value = "原价")
    private Double goodsPrice;

    @ApiModelProperty(value = "运费")
    private Double freightPrice;

    @ApiModelProperty(value = "优惠的金额")
    private Double discountPrice;

    /**
     * @see DeliveryMethodEnum
     */
    @ApiModelProperty(value = "配送方式")
    private String deliveryMethod;

    @ApiModelProperty(value = "收货人姓名")
    private String consigneeName;

    @ApiModelProperty(value = "收件人手机")
    private String consigneeMobile;

    @ApiModelProperty(value = "地址名称， '，'分割")
    private String consigneeAddressPath;

    @ApiModelProperty(value = "地址id，'，'分割 ")
    private String consigneeAddressIdPath;

    public Trade(TradeDTO tradeDTO) {
        String originId = this.getId();
        if (tradeDTO.getMemberAddress() != null) {
            BeanUtil.copyProperties(tradeDTO.getMemberAddress(), this);
            this.setConsigneeMobile(tradeDTO.getMemberAddress().getMobile());
            this.setConsigneeName(tradeDTO.getMemberAddress().getName());
        }
        BeanUtil.copyProperties(tradeDTO, this);
        BeanUtil.copyProperties(tradeDTO.getPriceDetailDTO(), this);
        this.setPayStatus(PayStatusEnum.UNPAID.name());
        this.setId(originId);
    }
}