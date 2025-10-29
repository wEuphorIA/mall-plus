package com.jzo2o.mall.order.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.jzo2o.mall.order.model.domain.Receipt;
import com.jzo2o.mall.order.model.dto.OrderReceiptDTO;
import com.jzo2o.mall.order.model.dto.OrderSimpleDTO;
import com.jzo2o.mall.order.model.dto.ReceiptSearchParams;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 发票数据处理层
 */
public interface ReceiptMapper extends BaseMapper<Receipt> {

    /**
     * 查询发票信息
     *
     * @param page              分页
     * @param queryWrapper 查询条件
     * @return
     */
    @Select("select r.*,o.order_status from oms_receipt r inner join oms_order o ON o.sn=r.order_sn ${ew.customSqlSegment}")
    IPage<OrderReceiptDTO> getReceipt(IPage<OrderSimpleDTO> page, @Param(Constants.WRAPPER) Wrapper<ReceiptSearchParams> queryWrapper);
}