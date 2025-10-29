package com.jzo2o.mall.common.model.message;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.jzo2o.mall.common.enums.ClientTypeEnum;
import com.jzo2o.mall.common.enums.OrderStatusEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
import java.util.List;

/**
 * 库存扣减及回滚消息
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class StockUpdateMessage {

    /**
     * 订单号
     */
    private String orderSn;

    /**
     * 是否是库存回滚  true:库存回滚,false:库存扣减
     */
    private Boolean isRollback;

}
