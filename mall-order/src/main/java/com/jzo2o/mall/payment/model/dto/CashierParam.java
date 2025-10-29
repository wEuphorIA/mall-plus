package com.jzo2o.mall.payment.model.dto;

import cn.hutool.core.text.CharSequenceUtil;
import com.jzo2o.common.utils.StringUtils;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 * 支付参数
 */
@Data
@ToString
public class CashierParam {

    static final Long MAX_DETAIL_LENGTH = 30L;

    @ApiModelProperty(value = "价格")
    private Double price;

    @ApiModelProperty(value = "支付title")
    private String title;

    @ApiModelProperty(value = "支付详细描述")
    private String detail;

    @ApiModelProperty(value = "订单sn集合")
    private String orderSns;
//    @ApiModelProperty(value = "订单id")
//    private Long orderId;

    @ApiModelProperty(value = "支持支付方式")
    private List<String> support;


    @ApiModelProperty(value = "订单创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "支付自动结束时间")
    private Long autoCancel;

//    @ApiModelProperty(value = "剩余余额")
//    private Double walletValue;

    public String getDetail() {
        if (CharSequenceUtil.isEmpty(detail)) {
            return "清单详细";
        }
        return StringUtils.filterSpecialChart(StringUtils.sub(detail, 30));
    }
}
