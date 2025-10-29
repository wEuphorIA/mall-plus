package com.jzo2o.mall.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jzo2o.mall.order.model.domain.Trade;
import org.apache.ibatis.annotations.Update;

/**
 * 交易数据处理层
 */
public interface TradeMapper extends BaseMapper<Trade> {

    /**
     * 修改交易金额
     *
     * @param tradeSn 交易编号
     */
    @Update("UPDATE oms_trade SET flow_price =(SELECT SUM(flow_price) FROM oms_order WHERE trade_sn=#{tradeSn}) WHERE sn=#{tradeSn}")
    void updateTradePrice(String tradeSn);
}