package com.jzo2o.mall.promotion.model.dto;

import com.jzo2o.mall.promotion.model.domain.Seckill;
import com.jzo2o.mall.promotion.model.domain.SeckillApply;
import com.jzo2o.mall.promotion.model.enums.SeckillApplyStatusEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

/**
 * 秒杀活动视图对象
 **/
@EqualsAndHashCode(callSuper = true)
@Data
@ToString(callSuper = true)
public class SeckillDTO extends Seckill {

    private static final long serialVersionUID = 2891461638257152270L;

    /**
     * @see SeckillApplyStatusEnum
     */
    @ApiModelProperty(value = "报名状态")
    private String seckillApplyStatus;

    /**
     * 当前秒杀活动下所有的秒杀申请信息
     */
    private List<SeckillApply> seckillApplyList;

}
