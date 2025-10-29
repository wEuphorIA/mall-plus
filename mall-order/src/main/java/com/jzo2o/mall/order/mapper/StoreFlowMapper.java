package com.jzo2o.mall.order.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.jzo2o.mall.order.model.domain.StoreFlow;
import com.jzo2o.mall.order.model.dto.StoreFlowPayDownloadDTO;
import com.jzo2o.mall.order.model.dto.StoreFlowRefundDownloadDTO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 商家订单流水数据处理层
 */
public interface StoreFlowMapper extends BaseMapper<StoreFlow> {

    /**
     * 获取结算单的入账流水
     * @param queryWrapper 查询条件
     * @return 入账流水
     */
    @Select("SELECT * FROM oms_store_flow ${ew.customSqlSegment}")
    List<StoreFlowPayDownloadDTO> getStoreFlowPayDownloadVO(@Param(Constants.WRAPPER) Wrapper<StoreFlow> queryWrapper);

    /**
     * 获取结算单的退款流水
     * @param queryWrapper 查询条件
     * @return 退款流水
     */
    @Select("SELECT * FROM oms_store_flow ${ew.customSqlSegment}")
    List<StoreFlowRefundDownloadDTO> getStoreFlowRefundDownloadVO(@Param(Constants.WRAPPER) Wrapper<StoreFlow> queryWrapper);
}