package com.jzo2o.mall.order.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jzo2o.mall.cart.model.dto.TradeDTO;
import com.jzo2o.mall.common.enums.OrderStatusEnum;
import com.jzo2o.mall.common.model.message.OrderStatusMessage;
import com.jzo2o.mall.member.model.dto.MemberAddressDTO;
import com.jzo2o.mall.order.model.domain.Order;
import com.jzo2o.mall.order.model.dto.*;
import com.jzo2o.mall.system.model.dto.TracesDTO;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 子订单业务层
 */
public interface OrderService extends IService<Order> {


    /**
     * 系统取消订单
     *
     * @param orderSn 订单编号
     * @param reason  错误原因
     * @param refundMoney 是否退款
     */
    void systemCancel(String orderSn, String reason,Boolean refundMoney);

    /**
     * 根据sn查询
     *
     * @param orderSn 订单编号
     * @return 订单信息
     */
    Order getBySn(String orderSn);


    /**
     * 订单查询
     *
     * @param orderSearchParams 查询参数
     * @return 简短订单分页
     */
    IPage<OrderSimpleDTO> queryByParams(OrderSearchParamsDTO orderSearchParams);

    /**
     * 订单信息
     *
     * @param orderSearchParams 查询参数
     * @return 订单信息
     */
    List<Order> queryListByParams(OrderSearchParamsDTO orderSearchParams);

//    /**
//     * 根据促销查询订单
//     *
//     * @param orderPromotionType 订单类型
//     * @param payStatus          支付状态
//     * @param parentOrderSn      依赖订单编号
//     * @param orderSn            订单编号
//     * @return 订单信息
//     */
//    List<Order> queryListByPromotion(String orderPromotionType, String payStatus, String parentOrderSn, String orderSn);

//    /**
//     * 根据促销查询订单
//     *
//     * @param orderPromotionType 订单类型
//     * @param payStatus          支付状态
//     * @param parentOrderSn      依赖订单编号
//     * @param orderSn            订单编号
//     * @return 订单信息
//     */
//    long queryCountByPromotion(String orderPromotionType, String payStatus, String parentOrderSn, String orderSn);

//    /**
//     * 父级拼团订单分组
//     *
//     * @param pintuanId 拼团id
//     * @return 拼团订单信息
//     */
//    List<Order> queryListByPromotion(String pintuanId);


    /**
     * 查询导出订单列表
     *
     * @param orderSearchParams 查询参数
     * @return 导出订单列表
     */
    List<OrderExportDTO> queryExportOrder(OrderSearchParamsDTO orderSearchParams);


    /**
     * 订单详细
     *
     * @param orderSn 订单SN
     * @return 订单详细
     */
    OrderDetailDTO queryDetail(String orderSn);

    /**
     * 创建订单
     * 1.检查交易信息
     * 2.循环交易购物车列表，创建订单以及相关信息
     *
     * @param tradeDTO 交易DTO
     */
    List<OrderDTO> intoDB(TradeDTO tradeDTO);

    /**
     * 订单付款
     * 修改订单付款信息
     * 记录订单流水
     *
     * @param orderId       订单编号
     * @param paymentMethod 支付方法
     * @param receivableNo  第三方流水
     */
    void payOrder(Long orderId, String paymentMethod, String receivableNo);
//
//    /**
//     * 订单确认成功
//     *
//     * @param orderSn
//     */
//    void afterOrderConfirm(String orderSn);
//
    /**
     * 取消订单
     *
     * @param orderSn 订单SN
     * @param reason  取消理由
     * @return 订单
     */
    Order cancel(String orderSn, String reason);


    /**
     * 发货信息修改
     * 日志功能内部实现
     *
     * @param orderSn          订单编号
     * @param memberAddressDTO 收货地址信息
     * @return 订单
     */
    Order updateConsignee(String orderSn, MemberAddressDTO memberAddressDTO);

    /**
     * 订单发货
     *
     * @param orderSn       订单编号
     * @param invoiceNumber 发货单号
     * @param logisticsId   物流公司
     * @return 订单
     */
    Order delivery(String orderSn, String invoiceNumber, String logisticsId);
//
//    /**
//     * 订单发货
//     *
//     * @param orderSn       订单编号
//     * @return 订单
//     */
//    Order shunFengDelivery(String orderSn);
//
    /**
     * 获取物流踪迹
     *
     * @param orderSn 订单编号
     * @return 物流踪迹
     */
    TracesDTO getTraces(String orderSn);
//
//    /**
//     * 获取地图版 物流踪迹
//     *
//     * @param orderSn 订单编号
//     * @return 物流踪迹
//     */
//    Traces getMapTraces(String orderSn);
//
//    /**
//     * 订单核验
//     *
//     * @param verificationCode 验证码
//     * @param orderSn          订单编号
//     * @return 订单
//     */
//    Order take(String orderSn, String verificationCode);
//
//
//    /**
//     * 订单核验
//     *
//     * @param verificationCode 验证码
//     * @return 订单
//     */
//    Order take(String verificationCode);
//
//    /**
//     * 根据核验码获取订单信息
//     *
//     * @param verificationCode 验证码
//     * @return 订单
//     */
//    Order getOrderByVerificationCode(String verificationCode);
//
    /**
     * 订单完成
     *
     * @param orderSn 订单编号
     */
    void complete(String orderSn);
//
//    /**
//     * 系统定时完成订单
//     *
//     * @param orderSn 订单编号
//     */
//    void systemComplete(String orderSn);
//
    /**
     * 通过trade 获取订单列表
     *
     * @param tradeSn 交易编号
     * @return 订单列表
     */
    List<Order> getByTradeSn(String tradeSn);
    /**
     * 发送更新订单状态的信息
     *
     * @param orderDetailDTO 订单信息
     */
    void sendUpdateStatusMessage(OrderDetailDTO orderDetailDTO, OrderStatusEnum oldStatus, OrderStatusEnum newStatus);

    /**
     * 普通商品订单确认
     * 修改订单状态为待发货
     * 发送订单状态变更消息
     *
     * @param orderSn 订单编号
     */
    public void normalOrderConfirm(String orderSn);


    /**
     * 根据订单sn逻辑删除订单
     *
     * @param sn 订单sn
     */
    void deleteOrder(String sn);
//
//    /**
//     * 开具发票
//     *
//     * @param sn 订单sn
//     * @return
//     */
//    Boolean invoice(String sn);
//
//    /**
//     * 自动成团订单处理
//     *
//     * @param pintuanId     拼团活动id
//     * @param parentOrderSn 拼团订单sn
//     */
//    void agglomeratePintuanOrder(String pintuanId, String parentOrderSn);
//
    /**
     * 获取待发货订单编号列表
     *
     * @param response      响应
     * @param logisticsName 店铺选择的物流公司
     * @param orderExportDTOS 待发货订单列表
     */
    void getBatchDeliverList(HttpServletResponse response, List<String> logisticsName,List<OrderExportDTO> orderExportDTOS);

    /**
     * 订单批量发货
     *
     * @param files 文件
     */
    void batchDeliver(MultipartFile files);

//
//    /**
//     * 获取订单实际支付的总金额
//     *
//     * @param orderSn 订单sn
//     * @return 金额
//     */
//    Double getPaymentTotal(String orderSn);
//
//    /**
//     * 查询订单支付记录
//     *
//     * @param page         分页
//     * @param queryWrapper 查询条件
//     * @return 订单支付记录分页
//     */
//    IPage<PaymentLog> queryPaymentLogs(IPage<PaymentLog> page, Wrapper<PaymentLog> queryWrapper);
//
//    /**
//     * 检查是否开始虚拟成团
//     *
//     * @param pintuanId   拼团活动id
//     * @param requiredNum 成团人数
//     * @param fictitious  是否开启成团
//     * @return 是否成功
//     */
//    boolean checkFictitiousOrder(String pintuanId, Integer requiredNum, Boolean fictitious);
//
    /**
     * 订单部分发货
     *
     * @param partDeliveryParamsDTO 参数
     * @return 订单
     */
    Order partDelivery(PartDeliveryParamsDTO partDeliveryParamsDTO);

}