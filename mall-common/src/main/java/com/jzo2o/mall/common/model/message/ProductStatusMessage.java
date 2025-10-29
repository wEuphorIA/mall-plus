package com.jzo2o.mall.common.model.message;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.jzo2o.mall.common.enums.ClientTypeEnum;
import com.jzo2o.mall.common.enums.GoodsAuthEnum;
import com.jzo2o.mall.common.enums.GoodsStatusEnum;
import com.jzo2o.mall.common.enums.OrderStatusEnum;
import com.jzo2o.mall.common.model.BaseEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
import java.util.List;

/**
 * 商品状态变更消息实体
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductStatusMessage {

    /**
     * 商品id
     */
    private String goodsId;

    /**
     * 商品审核状态
     */
    private GoodsAuthEnum goodsAuthEnum;

    /**
     * 商品状态
     */
    private GoodsStatusEnum goodsStatusEnum;



}
