package com.jzo2o.mall.order.model.dto;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.jzo2o.common.utils.StringUtils;
import com.jzo2o.common.utils.UserContext;
import com.jzo2o.mall.common.enums.UserEnums;
import com.jzo2o.mall.common.model.AuthUser;
import com.jzo2o.mall.order.model.enums.BillStatusEnum;
import com.jzo2o.mysql.domain.PageVO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * 结算单搜索参数
 *
 */
@Data
public class BillSearchParamsDTO extends PageVO {

    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "起始日期")
    private String startDate;

    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "结束日期")
    private String endDate;

    @ApiModelProperty(value = "账单号")
    private String sn;

    /**
     * @see BillStatusEnum
     */
    @ApiModelProperty(value = "状态：OUT(已出账),CHECK(已对账),EXAMINE(已审核),PAY(已付款)")
    private String billStatus;

    @ApiModelProperty(value = "店铺名称")
    private String storeName;

    @ApiModelProperty(value = "店铺ID", hidden = true)
    private String storeId;

    public <T> QueryWrapper<T> queryWrapper() {
        AuthUser authUser = UserContext.getCurrentUser();
        QueryWrapper<T> wrapper = new QueryWrapper<>();

        //创建时间
        if (StringUtils.isNotEmpty(startDate) && StringUtils.isNotEmpty(endDate)) {
            wrapper.between("create_time", startDate, endDate);
        } else if (StringUtils.isNotEmpty(startDate)) {
            wrapper.ge("create_time", startDate);
        } else if (StringUtils.isNotEmpty(endDate)) {
            wrapper.le("create_time", endDate);
        }
        //账单号
        wrapper.eq(StringUtils.isNotEmpty(sn), "sn", sn);
        //结算状态
        wrapper.eq(StringUtils.isNotEmpty(billStatus), "bill_status", billStatus);
        //店铺名称
        wrapper.eq(StringUtils.isNotEmpty(storeName), "store_name", storeName);
        //按卖家查询
        wrapper.eq(StringUtils.equals(authUser.getRole().name(), UserEnums.STORE.name()),
                "store_id", authUser.getStoreId());
        return wrapper;
    }

}
