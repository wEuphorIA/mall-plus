package com.jzo2o.mall.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jzo2o.mall.order.model.domain.Logistics;
import com.jzo2o.mall.system.model.dto.LogisticsSetting;
import com.jzo2o.mall.system.model.dto.TracesDTO;

import java.util.List;
import java.util.Map;

/**
 * 物流公司业务层
 */
public interface LogisticsService extends IService<Logistics> {

    /**
     * 查询物流信息
     *
     * @param logisticsId 物流公司ID
     * @param logisticsNo 单号
     * @param phone       手机号
     * @return
     */
    TracesDTO getLogisticTrack(String logisticsId, String logisticsNo, String phone);

    /**
     * 获取物流信息
     * @param logisticsId
     * @param logisticsNo
     * @param phone
     * @param from
     * @param to
     * @return
     */
    TracesDTO getLogisticMapTrack(String logisticsId, String logisticsNo, String phone, String from, String to);

    /**
     * 打印电子面单
     * @param orderSn 订单编号
     * @param logisticsId 物流Id
     * @return
     */
    Map labelOrder(String orderSn, String logisticsId);
//
//    /**
//     * 顺丰平台下单
//     * @param orderDetailVO 订单信息
//     * @return 顺丰单号
//     */
//    String sfCreateOrder(OrderDetailVO orderDetailVO);

    /**
     * 获取已开启的物流公司列表
     *
     * @return 物流公司列表
     */
    List<Logistics> getOpenLogistics();

    /**
     * 获取物流设置
     * @return
     */
    LogisticsSetting getLogisticsSetting();
}