package com.jzo2o.mall.order.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONUtil;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jzo2o.common.utils.BeanUtils;
import com.jzo2o.common.utils.UserContext;
import com.jzo2o.mall.cart.model.dto.TradeDTO;
import com.jzo2o.mall.cart.model.enums.DeliveryMethodEnum;
import com.jzo2o.mall.common.constants.MallMqConstants;
import com.jzo2o.mall.common.enums.*;
import com.jzo2o.mall.common.exception.ServiceException;
import com.jzo2o.mall.common.model.AuthUser;
import com.jzo2o.mall.common.model.message.OrderStatusMessage;
import com.jzo2o.mall.common.utils.SnowFlake;
import com.jzo2o.mall.member.model.dto.MemberAddressDTO;
import com.jzo2o.mall.member.service.StoreDetailService;
import com.jzo2o.mall.order.aop.OrderLogPoint;
import com.jzo2o.mall.order.mapper.OrderMapper;
import com.jzo2o.mall.order.model.domain.*;
import com.jzo2o.mall.order.model.dto.*;
import com.jzo2o.mall.order.model.enums.*;
import com.jzo2o.mall.order.service.*;
import com.jzo2o.mall.system.model.dto.TracesDTO;
import com.jzo2o.mysql.utils.PageUtils;
import com.jzo2o.rabbitmq.client.RabbitClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.*;

/**
 * 子订单业务层实现
 */
@Service
@Slf4j
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    private static final String ORDER_SN_COLUMN = "order_sn";

//    /**
//     * 延时任务
//     */
//    @Autowired
//    private TimeTrigger timeTrigger;
//    /**
//     * 发票
//     */
//    @Autowired
//    private ReceiptService receiptService;
    /**
     * 订单明细
     */
    @Autowired
    private OrderItemService orderItemService;
    /**
     * 物流公司
     */
    @Autowired
    private LogisticsService logisticsService;
    /**
     * 订单日志
     */
    @Autowired
    private OrderLogService orderLogService;
    /**
     * 订单流水
     */
    @Autowired
    private StoreFlowService storeFlowService;

    @Autowired
    private TradeService tradeService;


    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    @Autowired
    private StoreDetailService storeDetailService;

    @Autowired
    RabbitClient rabbitClient;
    /**
     * 订单包裹
     */
    @Autowired
    private OrderPackageService orderPackageService;
    /**
     * 订单包裹货物
     */
    @Autowired
    private OrderPackageItemService orderPackageItemService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<OrderDTO> intoDB(TradeDTO tradeDTO) {
        AuthUser authUser =  UserContext.getCurrentUser();
        //创建List用于存放订单
        List<Order> orders = new ArrayList<>(tradeDTO.getCartList().size());
        //存放订单明细
        List<OrderItem> orderItems = new ArrayList<>();
        //订单日志
        List<OrderLog> orderLogs = new ArrayList<>();
        //订单集合
        List<OrderDTO> orderDTOS = new ArrayList<>();
        //循环购物车
        tradeDTO.getCartList().forEach(cart -> {
            //当前购物车订单子项
            List<OrderItem> currentOrderItems = new ArrayList<>();

            Order order = new Order(cart, tradeDTO);
            //构建orderDTO对象
            OrderDTO orderDTO = new OrderDTO();
            BeanUtil.copyProperties(order, orderDTO);

            //持久化DO
            orders.add(order);
            String message = "订单[" + cart.getSn() + "]创建";
            //记录日志
            orderLogs.add(new OrderLog(cart.getSn(),authUser.getIdString(), authUser.getRole().getRole(),
                    authUser.getUsername(), message));
            cart.getCheckedSkuList().forEach(
                    sku -> {
                        orderItems.add(new OrderItem(sku, cart, tradeDTO));
                        currentOrderItems.add(new OrderItem(sku, cart, tradeDTO));
                    }
            );
            //写入子订单信息
            orderDTO.setOrderItems(currentOrderItems);
            //orderDTO 记录
            orderDTOS.add(orderDTO);

        });
//        tradeDTO.setOrderDTO(orderDTOS);
        //批量保存订单
        this.saveBatch(orders);
        //批量保存 子订单
        orderItemService.saveBatch(orderItems);
        //批量记录订单操作日志
        orderLogService.saveBatch(orderLogs);

        return orderDTOS;
    }

    @Override
    public IPage<OrderSimpleDTO> queryByParams(OrderSearchParamsDTO orderSearchParams) {
        QueryWrapper queryWrapper = orderSearchParams.queryWrapper();
        queryWrapper.groupBy("o.id");
        queryWrapper.orderByDesc("o.update_time");
        return this.baseMapper.queryByParams(PageUtils.initPage(orderSearchParams), queryWrapper);
    }

    /**
     * 订单信息
     *
     * @param orderSearchParams 查询参数
     * @return 订单信息
     */
    @Override
    public List<Order> queryListByParams(OrderSearchParamsDTO orderSearchParams) {
        return this.baseMapper.queryListByParams(orderSearchParams.queryWrapper());
    }

//    /**
//     * 根据促销查询订单
//     *
//     * @param orderPromotionType 订单类型
//     * @param payStatus          支付状态
//     * @param parentOrderSn      依赖订单编号
//     * @param orderSn            订单编号
//     * @return 订单信息
//     */
//    @Override
//    public List<Order> queryListByPromotion(String orderPromotionType, String payStatus, String parentOrderSn, String orderSn) {
//        LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();
//        //查找团长订单和已和当前拼团订单拼团的订单
//        queryWrapper.eq(Order::getOrderPromotionType, orderPromotionType)
//                .eq(Order::getPayStatus, payStatus)
//                .and(i -> i.eq(Order::getParentOrderSn, parentOrderSn).or(j -> j.eq(Order::getSn, orderSn)));
//        return this.list(queryWrapper);
//    }
//
//    /**
//     * 根据促销查询订单
//     *
//     * @param orderPromotionType 订单类型
//     * @param payStatus          支付状态
//     * @param parentOrderSn      依赖订单编号
//     * @param orderSn            订单编号
//     * @return 订单信息
//     */
//    @Override
//    public long queryCountByPromotion(String orderPromotionType, String payStatus, String parentOrderSn, String orderSn) {
//        LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();
//        //查找团长订单和已和当前拼团订单拼团的订单
//        queryWrapper.eq(Order::getOrderPromotionType, orderPromotionType)
//                .eq(Order::getPayStatus, payStatus)
//                .and(i -> i.eq(Order::getParentOrderSn, parentOrderSn).or(j -> j.eq(Order::getSn, orderSn)));
//        return this.count(queryWrapper);
//    }
//
//    /**
//     * 父级拼团订单
//     *
//     * @param pintuanId 拼团id
//     * @return 拼团订单信息
//     */
//    @Override
//    public List<Order> queryListByPromotion(String pintuanId) {
//        LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();
//        queryWrapper.eq(Order::getOrderPromotionType, PromotionTypeEnum.PINTUAN.name());
//        queryWrapper.eq(Order::getPromotionId, pintuanId);
//        queryWrapper.nested(i -> i.eq(Order::getPayStatus, PayStatusEnum.PAID.name()).or(j -> j.eq(Order::getOrderStatus,
//                OrderStatusEnum.PAID.name())));
//        queryWrapper.ne(Order::getOrderStatus, OrderStatusEnum.CANCELLED.name());
//        return this.list(queryWrapper);
//    }
//
    @Override
    public List<OrderExportDTO> queryExportOrder(OrderSearchParamsDTO orderSearchParams) {
        Wrapper<OrderSimpleDTO> objectQueryWrapper = orderSearchParams.queryWrapper();
        List<OrderExportDTO> orderExportDTOS = this.baseMapper.queryExportOrder(objectQueryWrapper);
        return orderExportDTOS;
    }

    @Override
    public OrderDetailDTO queryDetail(String orderSn) {
        Order order = this.getBySn(orderSn);
        if (order == null) {
            throw new ServiceException(ResultCode.ORDER_NOT_EXIST);
        }
        //查询订单项信息
        List<OrderItem> orderItems = orderItemService.getByOrderSn(orderSn);
        //查询订单日志信息
        List<OrderLog> orderLogs = orderLogService.getOrderLog(orderSn);
        //查询发票信息
//        Receipt receipt = receiptService.getByOrderSn(orderSn);
        //查询订单和自订单，然后写入vo返回
        return new OrderDetailDTO(order, orderItems,orderLogs);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Order cancel(String orderSn, String reason) {
        Order order = this.getBySn(orderSn);
        //订单旧状态
        String oldStatus = order.getOrderStatus();
        if (CharSequenceUtil.equalsAny(order.getOrderStatus(),
                OrderStatusEnum.UNDELIVERED.name(),
                OrderStatusEnum.UNPAID.name())) {

            order.setOrderStatus(OrderStatusEnum.CANCELLED.name());
            order.setCancelReason(reason);
            //修改订单
            this.updateById(order);
            //生成店铺退款流水
            this.generatorStoreRefundFlow(order);
//            orderStatusMessage(order);
            //查询订单信息
            OrderDetailDTO orderDetailDTO = this.queryDetail(orderSn);
            //发送消息通知其他系统
            this.sendUpdateStatusMessage(orderDetailDTO, OrderStatusEnum.valueOf(oldStatus), OrderStatusEnum.CANCELLED);
            return order;
        } else {
            throw new ServiceException(ResultCode.ORDER_CAN_NOT_CANCEL);
        }
    }


    @Override
    @OrderLogPoint(description = "'订单['+#orderSn+']系统取消，原因为：'+#reason", orderSn = "#orderSn")
    @Transactional(rollbackFor = Exception.class)
    public void systemCancel(String orderSn, String reason,Boolean refundMoney) {
        Order order = this.getBySn(orderSn);
        //订单旧状态
        String oldStatus = order.getOrderStatus();
        if (OrderStatusEnum.UNPAID.name().equals(order.getOrderStatus())) {
            order.setOrderStatus(OrderStatusEnum.CANCELLED.name());
            order.setCancelReason(reason);
            //修改订单
            this.updateById(order);
            //生成店铺退款流水
            this.generatorStoreRefundFlow(order);
//            orderStatusMessage(order);
            //查询订单信息
            OrderDetailDTO orderDetailDTO = this.queryDetail(orderSn);
            //发送消息通知其他系统
            this.sendUpdateStatusMessage(orderDetailDTO, OrderStatusEnum.valueOf(oldStatus), OrderStatusEnum.CANCELLED);
        }
    }

    /**
     * 获取订单
     *
     * @param orderSn 订单编号
     * @return 订单详情
     */
    @Override
    public Order getBySn(String orderSn) {
        return this.getOne(new LambdaQueryWrapper<Order>().eq(Order::getSn, orderSn));
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void payOrder(Long orderId, String paymentMethod, String receivableNo) {

        Order order = this.getById(orderId);
        //订单旧状态
        String oldStatus = order.getOrderStatus();
        //如果订单已支付，就不能再次进行支付
        if (!order.getPayStatus().equals(PayStatusEnum.UNPAID.name())) {
            log.error("订单[ {} ]当前状态为{}，无法付款", orderId,order.getPayStatus());
            return ;
//            throw new ServiceException(ResultCode.PAY_DOUBLE_ERROR);
        }

        //修改订单状态
        order.setPaymentTime(new Date());
        order.setPaymentMethod(paymentMethod);
        order.setPayStatus(PayStatusEnum.PAID.name());
//        order.setOrderStatus(OrderStatusEnum.PAID.name());
        order.setOrderStatus(OrderStatusEnum.UNDELIVERED.name());
        order.setReceivableNo(receivableNo);//支付系统交易号
        order.setPayOrderNo(receivableNo);//支付系统交易号
        order.setCanReturn(true);//支持原路返回
        this.updateById(order);

        //记录店铺订单支付流水
        storeFlowService.payOrder(order.getSn());

        //查询订单信息
        OrderDetailDTO orderDetailDTO = this.queryDetail(order.getSn());
        this.sendUpdateStatusMessage(orderDetailDTO, OrderStatusEnum.UNPAID, OrderStatusEnum.UNDELIVERED);
//        this.sendUpdateStatusMessage(orderDetailDTO, OrderStatusEnum.UNPAID, OrderStatusEnum.PAID);

        String message = "订单付款，付款方式[" + paymentMethod + "]";
        OrderLog orderLog = new OrderLog(order.getSn(), "-1", UserEnums.SYSTEM.getRole(), "系统操作", message);
        orderLogService.save(orderLog);

    }
//
//    @Override
//    @OrderLogPoint(description = "'库存确认'", orderSn = "#orderSn")
//    @Transactional(rollbackFor = Exception.class)
//    public void afterOrderConfirm(String orderSn) {
//        Order order = this.getBySn(orderSn);
//        //判断是否为拼团订单，进行特殊处理
//        //判断订单类型进行不同的订单确认操作
//        if (OrderPromotionTypeEnum.PINTUAN.name().equals(order.getOrderPromotionType())) {
//            String parentOrderSn = CharSequenceUtil.isEmpty(order.getParentOrderSn()) ? orderSn : order.getParentOrderSn();
//            this.checkPintuanOrder(order.getPromotionId(), parentOrderSn);
//        } else {
//            //判断订单类型
//            if (order.getOrderType().equals(OrderTypeEnum.NORMAL.name())) {
//                normalOrderConfirm(orderSn);
//            } else {
//                virtualOrderConfirm(orderSn);
//            }
//        }
//    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Order updateConsignee(String orderSn, MemberAddressDTO memberAddressDTO) {
        Order order = this.getBySn(orderSn);
        //要记录之前的收货地址，所以需要以代码方式进行调用 不采用注解
        String message = "订单[" + orderSn + "]收货信息修改，由[" + order.getConsigneeDetail() + "]修改为[" + memberAddressDTO.getConsigneeDetail() + "]";
        //记录订单操作日志
        BeanUtil.copyProperties(memberAddressDTO, order);
        this.updateById(order);
        AuthUser authUser = UserContext.getCurrentUser();
        OrderLog orderLog = new OrderLog(orderSn, authUser.getIdString(), authUser.getRole().getRole(),
                authUser.getUsername(), message);
        orderLogService.save(orderLog);
        return order;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Order delivery(String orderSn, String logisticsNo, String logisticsId) {
        Order order = this.getBySn(orderSn);
        //订单旧状态
        String oldStatus = order.getOrderStatus();
        //如果订单未发货，并且订单状态值等于待发货
        if (order.getDeliverStatus().equals(DeliverStatusEnum.UNDELIVERED.name()) && order.getOrderStatus().equals(OrderStatusEnum.UNDELIVERED.name())) {
            //获取对应物流
            Logistics logistics = logisticsService.getById(logisticsId);
            if (logistics == null) {
                throw new ServiceException(ResultCode.ORDER_LOGISTICS_ERROR);
            }
            //写入物流信息
            order.setLogisticsCode(logistics.getId());
            order.setLogisticsName(logistics.getName());
            order.setLogisticsNo(logisticsNo);
            order.setLogisticsTime(new Date());
            order.setDeliverStatus(DeliverStatusEnum.DELIVERED.name());
            this.updateById(order);
            //修改订单状态为已发送
            this.updateStatus(orderSn, OrderStatusEnum.DELIVERED);
            //修改订单货物可以进行售后、投诉
            orderItemService.update(new UpdateWrapper<OrderItem>().eq(ORDER_SN_COLUMN, orderSn)
                    .set("after_sale_status", OrderItemAfterSaleStatusEnum.NOT_APPLIED.name())
                    .set("complain_status", OrderComplaintStatusEnum.NO_APPLY.name()));
            //查询订单信息得到OrderDetailDTO类型
            OrderDetailDTO orderDetailDTO = this.queryDetail(orderSn);
            this.sendUpdateStatusMessage(orderDetailDTO,OrderStatusEnum.valueOf(oldStatus),OrderStatusEnum.DELIVERED);
        } else {
            throw new ServiceException(ResultCode.ORDER_DELIVER_ERROR);
        }
        return order;
    }
//
//    @Override
//    @Transactional(rollbackFor = Exception.class)
//    public Order shunFengDelivery(String orderSn) {
//        OrderDetailVO orderDetailVO = this.queryDetail(orderSn);
//        String logisticsNo = logisticsService.sfCreateOrder(orderDetailVO);
//        Logistics logistics = logisticsService.getOne(new LambdaQueryWrapper<Logistics>().eq(Logistics::getCode, "SF"));
//        return delivery(orderSn, logisticsNo, logistics.getId());
//    }
//
    @Override
    public TracesDTO getTraces(String orderSn) {
        //获取订单信息
        Order order = this.getBySn(orderSn);
        //获取踪迹信息
        TracesDTO logisticTrack = logisticsService.getLogisticTrack(order.getLogisticsCode(), order.getLogisticsNo(), order.getConsigneeMobile());
        return logisticTrack;
    }
//
//    @Override
//    public Traces getMapTraces(String orderSn) {
//        //获取订单信息
//        Order order = this.getBySn(orderSn);
//        //获取店家信息
//        StoreDeliverGoodsAddressDTO storeDeliverGoodsAddressDTO = storeDetailService.getStoreDeliverGoodsAddressDto(order.getStoreId());
//        String from = storeDeliverGoodsAddressDTO.getSalesConsignorAddressPath().substring(0,
//                storeDeliverGoodsAddressDTO.getSalesConsignorAddressPath().indexOf(",") - 1);
//        String to = order.getConsigneeAddressPath().substring(0, order.getConsigneeAddressPath().indexOf(",") - 1);
//        //获取踪迹信息
//        return logisticsService.getLogisticMapTrack(order.getLogisticsCode(), order.getLogisticsNo(), order.getConsigneeMobile(), from, to);
//    }
//
//    @Override
//    @OrderLogPoint(description = "'订单['+#orderSn+']核销，核销码['+#verificationCode+']'", orderSn = "#orderSn")
//    @Transactional(rollbackFor = Exception.class)
//    public Order take(String orderSn, String verificationCode) {
//
//        //获取订单信息
//        Order order = this.getBySn(orderSn);
//        //检测虚拟订单信息
//        checkVerificationOrder(order, verificationCode);
//        order.setOrderStatus(OrderStatusEnum.COMPLETED.name());
//        //订单完成
//        this.complete(orderSn);
//        return order;
//    }
//
//    @Override
//    public Order take(String verificationCode) {
//        String storeId = OperationalJudgment.judgment(UserContext.getCurrentUser()).getStoreId();
//        Order order = this.getOne(new LambdaQueryWrapper<Order>().eq(Order::getVerificationCode, verificationCode).eq(Order::getStoreId, storeId));
//        if (order == null) {
//            throw new ServiceException(ResultCode.ORDER_NOT_EXIST);
//        }
//        order.setOrderStatus(OrderStatusEnum.COMPLETED.name());
//        //订单完成
//        this.complete(order.getSn());
//        return order;
//    }
//
//    @Override
//    public Order getOrderByVerificationCode(String verificationCode) {
//        String storeId = Objects.requireNonNull(UserContext.getCurrentUser()).getStoreId();
//        return this.getOne(new LambdaQueryWrapper<Order>()
//                .in(Order::getOrderStatus, OrderStatusEnum.TAKE.name(), OrderStatusEnum.STAY_PICKED_UP.name())
//                .eq(Order::getStoreId, storeId)
//                .eq(Order::getVerificationCode, verificationCode));
//    }
//
    @Override
    @OrderLogPoint(description = "'订单['+#orderSn+']完成'", orderSn = "#orderSn")
    @Transactional(rollbackFor = Exception.class)
    public void complete(String orderSn) {
        //是否可以查询到订单
//        Order order = OperationalJudgment.judgment(this.getBySn(orderSn));
        Order order = this.getBySn(orderSn);
        complete(order, orderSn);
    }
//
//    @Override
//    @OrderLogPoint(description = "'订单['+#orderSn+']完成'", orderSn = "#orderSn")
//    @Transactional(rollbackFor = Exception.class)
//    public void systemComplete(String orderSn) {
//        Order order = this.getBySn(orderSn);
//        complete(order, orderSn);
//    }
//
    /**
     * 完成订单方法封装
     *
     * @param order   订单
     * @param orderSn 订单编号
     */
    @Transactional(rollbackFor = Exception.class)
    public void complete(Order order, String orderSn) {//修改订单状态为完成
        this.updateStatus(orderSn, OrderStatusEnum.COMPLETED);

        //修改订单货物可以进行评价
        orderItemService.update(new UpdateWrapper<OrderItem>().eq(ORDER_SN_COLUMN, orderSn)
                .set("comment_status", CommentStatusEnum.UNFINISHED.name()));
        this.update(new LambdaUpdateWrapper<Order>().eq(Order::getSn, orderSn).set(Order::getCompleteTime, new Date()));
        //发送订单状态改变消息
        OrderDetailDTO orderDetailDTO = this.queryDetail(orderSn);
        this.sendUpdateStatusMessage(orderDetailDTO,OrderStatusEnum.DELIVERED,OrderStatusEnum.COMPLETED);

//        OrderMessage orderMessage = new OrderMessage();
//        orderMessage.setNewStatus(OrderStatusEnum.COMPLETED);
//        orderMessage.setOrderSn(order.getSn());
//        this.sendUpdateStatusMessage(orderMessage);

        //发送当前商品购买完成的信息（用于更新商品数据）
//        List<OrderItem> orderItems = orderItemService.getByOrderSn(orderSn);
//        List<GoodsCompleteMessage> goodsCompleteMessageList = new ArrayList<>();
//        for (OrderItem orderItem : orderItems) {
//            GoodsCompleteMessage goodsCompleteMessage = new GoodsCompleteMessage();
//            goodsCompleteMessage.setGoodsId(orderItem.getGoodsId());
//            goodsCompleteMessage.setSkuId(orderItem.getSkuId());
//            goodsCompleteMessage.setBuyNum(orderItem.getNum());
//            goodsCompleteMessage.setMemberId(order.getMemberId());
//            goodsCompleteMessageList.add(goodsCompleteMessage);
//        }
//        //发送商品购买消息
//        if (!goodsCompleteMessageList.isEmpty()) {
//            String destination = rocketmqCustomProperties.getGoodsTopic() + ":" + GoodsTagsEnum.BUY_GOODS_COMPLETE.name();
//            //发送订单变更mq消息
//            rocketMQTemplate.asyncSend(destination, JSONUtil.toJsonStr(goodsCompleteMessageList), RocketmqSendCallbackBuilder.commonCallback());
//        }
    }
//
    @Override
    public List<Order> getByTradeSn(String tradeSn) {
        LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();
        return this.list(queryWrapper.eq(Order::getTradeSn, tradeSn));
    }

    @Override
    public void sendUpdateStatusMessage(OrderDetailDTO orderDetailDTO,OrderStatusEnum oldStatus,OrderStatusEnum newStatus) {
//        applicationEventPublisher.publishEvent(new TransactionCommitSendMQEvent("发送订单变更mq消息", rocketmqCustomProperties.getOrderTopic(),
//                OrderTagsEnum.STATUS_CHANGE.name(), JSONUtil.toJsonStr(orderMessage)));
        //发送订单已付款消息
        OrderStatusMessage orderMessage = new OrderStatusMessage();
        //复制订单信息
        OrderStatusMessage.OrderDetail orderDetail = BeanUtils.toBean(orderDetailDTO, OrderStatusMessage.OrderDetail.class);
        orderMessage.setOrderDetail(orderDetail);
        //付款方式
        orderMessage.setPaymentMethod(orderDetailDTO.getPaymentMethodValue());
        //订单新状态
        orderMessage.setNewStatus(newStatus);
        //订单旧状态
        orderMessage.setOldStatus(oldStatus);
        //发送消息
        rabbitClient.sendMsg(MallMqConstants.Exchanges.EXCHANGE_ORDER, MallMqConstants.RoutingKeys.ORDER_STATUS_UPDATE_ROUTINGKEY, orderMessage);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteOrder(String sn) {
        Order order = this.getBySn(sn);
        if (order == null) {
            log.error("订单号为" + sn + "的订单不存在！");
            throw new ServiceException();
        }
        LambdaUpdateWrapper<Order> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Order::getSn, sn).set(Order::getDeleteFlag, true);
        this.update(updateWrapper);
        LambdaUpdateWrapper<OrderItem> orderItemLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        orderItemLambdaUpdateWrapper.eq(OrderItem::getOrderSn, sn).set(OrderItem::getDeleteFlag, true);
        this.orderItemService.update(orderItemLambdaUpdateWrapper);
    }
//
//    @Override
//    public Boolean invoice(String sn) {
//        //根据订单号查询发票信息
//        Receipt receipt = receiptService.getByOrderSn(sn);
//        //校验发票信息是否存在
//        if (receipt != null) {
//            receipt.setReceiptStatus(1);
//            return receiptService.updateById(receipt);
//        }
//        throw new ServiceException(ResultCode.USER_RECEIPT_NOT_EXIST);
//    }
//
//    /**
//     * 自动成团订单处理
//     *
//     * @param pintuanId     拼团活动id
//     * @param parentOrderSn 拼团订单sn
//     */
//    @Override
//    @Transactional(rollbackFor = Exception.class)
//    public void agglomeratePintuanOrder(String pintuanId, String parentOrderSn) {
//        //获取拼团配置
//        Pintuan pintuan = pintuanService.getById(pintuanId);
//        List<Order> list = this.getPintuanOrder(pintuanId, parentOrderSn);
//        if (Boolean.TRUE.equals(pintuan.getFictitious()) && pintuan.getRequiredNum() > list.size()) {
//            //如果开启虚拟成团且当前订单数量不足成团数量，则认为拼团成功
//            this.pintuanOrderSuccess(list);
//        } else if (Boolean.FALSE.equals(pintuan.getFictitious()) && pintuan.getRequiredNum() > list.size()) {
//            //如果未开启虚拟成团且当前订单数量不足成团数量，则认为拼团失败
//            this.pintuanOrderFailed(list);
//        }
//    }
//
    @Override
    public void getBatchDeliverList(HttpServletResponse response,List<String> logisticsName, List<OrderExportDTO> orderExportDTOS) {
        ExcelWriter writer = ExcelUtil.getWriter();
        //Excel 头部
        ArrayList<String> rows = new ArrayList<>();
        rows.add("订单编号");
        rows.add("物流公司");
        rows.add("物流编号");
        rows.add("商品名称");
        rows.add("商品数量");
        rows.add("联系人");
        rows.add("联系方式");
        rows.add("地址");
        writer.writeHeadRow(rows);


        //存放下拉列表  ----店铺已选择物流公司列表
        String[] logiList = logisticsName.toArray(new String[]{});
        CellRangeAddressList cellRangeAddressList = new CellRangeAddressList(1, 10000, 1, 1);
        writer.addSelect(cellRangeAddressList, logiList);

        //如果订单列表orderExportDTOS不为空则加入要导出的excel中
        if (!orderExportDTOS.isEmpty()) {
            for (OrderExportDTO orderExportDTO : orderExportDTOS) {
                ArrayList<Object> row = new ArrayList<>();
                row.add(orderExportDTO.getSn());
                row.add(orderExportDTO.getLogisticsName());
                row.add(null);//物流编号待用户填写
                row.add(orderExportDTO.getGoodsName());
                row.add(orderExportDTO.getNum());
                row.add(orderExportDTO.getConsigneeName());
                row.add(orderExportDTO.getConsigneeMobile());
                row.add(orderExportDTO.getConsigneeAddressPath()!=null?orderExportDTO.getConsigneeAddressPath():"" + orderExportDTO.getConsigneeDetail()!=null?orderExportDTO.getConsigneeDetail():"");
                writer.writeRow(row);
            }
        }

        ServletOutputStream out = null;
        try {
            //设置公共属性，列表名称
            response.setContentType("application/vnd.ms-excel;charset=utf-8");
            response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode("批量发货导入模板", "UTF8") + ".xls");
            out = response.getOutputStream();
            writer.flush(out, true);
        } catch (Exception e) {
            log.error("获取待发货订单编号列表错误", e);
        } finally {
            writer.close();
            IoUtil.close(out);
        }

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDeliver(MultipartFile files) {

        InputStream inputStream;
        List<OrderBatchDeliverDTO> orderBatchDeliverDTOList = new ArrayList<>();
        try {
            inputStream = files.getInputStream();
            //2.应用HUtool ExcelUtil获取ExcelReader指定输入流和sheet
            ExcelReader excelReader = ExcelUtil.getReader(inputStream);
            //可以加上表头验证
            //3.读取第二行到最后一行数据
            List<List<Object>> read = excelReader.read(1, excelReader.getRowCount());
            for (List<Object> objects : read) {
                OrderBatchDeliverDTO orderBatchDeliverDTO = new OrderBatchDeliverDTO();
                orderBatchDeliverDTO.setOrderSn(objects.get(0).toString());
                orderBatchDeliverDTO.setLogisticsName(objects.get(1).toString());
                orderBatchDeliverDTO.setLogisticsNo(objects.get(2).toString());
                orderBatchDeliverDTOList.add(orderBatchDeliverDTO);
            }
        } catch (Exception e) {
            throw new ServiceException(ResultCode.ORDER_BATCH_DELIVER_ERROR);
        }
        //循环检查是否符合规范
        checkBatchDeliver(orderBatchDeliverDTOList);
        //订单批量发货
        for (OrderBatchDeliverDTO orderBatchDeliverDTO : orderBatchDeliverDTOList) {
            this.delivery(orderBatchDeliverDTO.getOrderSn(), orderBatchDeliverDTO.getLogisticsNo(), orderBatchDeliverDTO.getLogisticsId());
        }
    }


//    @Override
//    public Double getPaymentTotal(String orderSn) {
//        Order order = this.getBySn(orderSn);
//        Trade trade = tradeService.getBySn(order.getTradeSn());
//        //如果交易不为空，则返回交易的金额，否则返回订单金额
//        if (CharSequenceUtil.isNotEmpty(trade.getPayStatus())
//                && trade.getPayStatus().equals(PayStatusEnum.PAID.name())) {
//            return trade.getFlowPrice();
//        }
//        return order.getFlowPrice();
//    }
//
//    @Override
//    public IPage<PaymentLog> queryPaymentLogs(IPage<PaymentLog> page, Wrapper<PaymentLog> queryWrapper) {
//        return baseMapper.queryPaymentLogs(page, queryWrapper);
//    }
//
    /**
     * 循环检查批量发货订单列表
     *
     * @param list 待发货订单列表
     */
    private void checkBatchDeliver(List<OrderBatchDeliverDTO> list) {
        AuthUser authUser = UserContext.getCurrentUser();
        List<Logistics> logistics = logisticsService.list();
        for (OrderBatchDeliverDTO orderBatchDeliverDTO : list) {
            //查看订单号是否存在-是否是当前店铺的订单
            Order order = this.getOne(new LambdaQueryWrapper<Order>()
                    .eq(Order::getStoreId, authUser.getStoreId())
                    .eq(Order::getSn, orderBatchDeliverDTO.getOrderSn()));
            if (order == null) {
                throw new ServiceException("订单编号：'" + orderBatchDeliverDTO.getOrderSn() + " '不存在");
            } else if (!order.getOrderStatus().equals(OrderStatusEnum.UNDELIVERED.name())) {
                throw new ServiceException("订单编号：'" + orderBatchDeliverDTO.getOrderSn() + " '不能发货");
            }
            //获取物流公司
            logistics.forEach(item -> {
                if (item.getName().equals(orderBatchDeliverDTO.getLogisticsName())) {
                    orderBatchDeliverDTO.setLogisticsId(item.getId());
                }
            });
            if (CharSequenceUtil.isEmpty(orderBatchDeliverDTO.getLogisticsId())) {
                throw new ServiceException("物流公司：'" + orderBatchDeliverDTO.getLogisticsName() + " '不存在");
            }
        }
    }
//
//    /**
//     * 检查是否开始虚拟成团
//     *
//     * @param pintuanId   拼团活动id
//     * @param requiredNum 成团人数
//     * @param fictitious  是否开启成团
//     * @return 是否成功
//     */
//    @Override
//    @Transactional(rollbackFor = Exception.class)
//    public boolean checkFictitiousOrder(String pintuanId, Integer requiredNum, Boolean fictitious) {
//        Map<String, List<Order>> collect = this.queryListByPromotion(pintuanId)
//                .stream().collect(Collectors.groupingBy(Order::getParentOrderSn));
//
//        for (Map.Entry<String, List<Order>> entry : collect.entrySet()) {
//            //是否开启虚拟成团
//            if (Boolean.FALSE.equals(fictitious) && CharSequenceUtil.isNotEmpty(entry.getKey()) && entry.getValue().size() < requiredNum) {
//                //如果未开启虚拟成团且已参团人数小于成团人数，则自动取消订单
//                String reason = "拼团活动结束订单未付款，系统自动取消订单";
//                if (CharSequenceUtil.isNotEmpty(entry.getKey())) {
//                    this.systemCancel(entry.getKey(), reason,true);
//                } else {
//                    for (Order order : entry.getValue()) {
//                        if (!CharSequenceUtil.equalsAny(order.getOrderStatus(), OrderStatusEnum.COMPLETED.name(), OrderStatusEnum.DELIVERED.name(),
//                                OrderStatusEnum.TAKE.name(), OrderStatusEnum.STAY_PICKED_UP.name())) {
//                            this.systemCancel(order.getSn(), reason,true);
//                        }
//                    }
//                }
//            } else if (Boolean.TRUE.equals(fictitious)) {
//                this.fictitiousPintuan(entry, requiredNum);
//            }
//        }
//        return false;
//    }
//
    @Override
    public Order partDelivery(PartDeliveryParamsDTO partDeliveryParamsDTO) {
        String logisticsId = partDeliveryParamsDTO.getLogisticsId();
        String orderSn = partDeliveryParamsDTO.getOrderSn();
        String invoiceNumber = partDeliveryParamsDTO.getLogisticsNo();

        //获取对应物流
        Logistics logistics = logisticsService.getById(logisticsId);
        if (logistics == null) {
            throw new ServiceException(ResultCode.ORDER_LOGISTICS_ERROR);
        }
        Order order = this.getBySn(orderSn);
        //获取订单明细
        List<OrderItem> orderItemList = orderItemService.getByOrderSn(orderSn);

        OrderPackage orderPackage = new OrderPackage();
        orderPackage.setPackageNo(SnowFlake.createStr("OP"));
        orderPackage.setOrderSn(orderSn);
        orderPackage.setLogisticsNo(invoiceNumber);
        orderPackage.setLogisticsCode(logistics.getCode());
        orderPackage.setLogisticsName(logistics.getName());
        orderPackage.setStatus("1");
        orderPackage.setConsigneeMobile(order.getConsigneeMobile());
        orderPackageService.save(orderPackage);
//        List<OrderLog> orderLogList = new ArrayList<>();
        for (PartDeliveryDTO partDeliveryDTO : partDeliveryParamsDTO.getPartDeliveryDTOList()) {
            for (OrderItem orderItem : orderItemList) {
                //寻找订单货物进行判断
                if (partDeliveryDTO.getOrderItemId().equals(orderItem.getId())) {
                    if ((partDeliveryDTO.getDeliveryNum() + orderItem.getDeliverNumber()) > orderItem.getNum()) {
                        throw new ServiceException("发货数量不正确!");
                    }
                    orderItem.setDeliverNumber((partDeliveryDTO.getDeliveryNum() + orderItem.getDeliverNumber()));

                    // 记录分包裹中每个item子单的具体发货信息
                    OrderPackageItem orderPackageItem = new OrderPackageItem();
                    orderPackageItem.setOrderSn(orderSn);
                    orderPackageItem.setPackageNo(orderPackage.getPackageNo());
                    orderPackageItem.setOrderItemSn(orderItem.getSn());
                    orderPackageItem.setDeliverNumber(partDeliveryDTO.getDeliveryNum());
                    orderPackageItem.setLogisticsTime(new Date());
                    orderPackageItem.setGoodsName(orderItem.getGoodsName());
                    orderPackageItem.setThumbnail(orderItem.getImage());
                    orderPackageItemService.save(orderPackageItem);
                }
            }
        }
        //修改订单货物
        orderItemService.updateBatchById(orderItemList);

        //判断订单货物是否全部发货完毕
        Boolean delivery = true;
        for (OrderItem orderItem : orderItemList) {
            if (orderItem.getDeliverNumber() < orderItem.getNum()) {
                delivery = false;
                break;
            }
        }
        //是否全部发货
        if (delivery) {
            return delivery(orderSn, invoiceNumber, logisticsId);
        }
        return order;
    }

//
//    /**
//     * 订单状态变更消息
//     *
//     * @param order 订单信息
//     */
//    @Transactional(rollbackFor = Exception.class)
//    public void orderStatusMessage(Order order) {
//        OrderMessage orderMessage = new OrderMessage();
//        orderMessage.setOrderSn(order.getSn());
//        orderMessage.setNewStatus(OrderStatusEnum.valueOf(order.getOrderStatus()));
//        this.sendUpdateStatusMessage(orderMessage);
//    }

    /**
     * 生成店铺退款流水
     *
     * @param order 订单信息
     */
    private void generatorStoreRefundFlow(Order order) {
        // 判断订单是否是付款
        if (!PayStatusEnum.PAID.name().equals((order.getPayStatus()))) {
            return;
        }
        List<OrderItem> items = orderItemService.getByOrderSn(order.getSn());
        List<StoreFlow> storeFlows = new ArrayList<>();
        for (OrderItem item : items) {
            StoreFlow storeFlow = new StoreFlow(order, item, FlowTypeEnum.REFUND);
            storeFlows.add(storeFlow);
        }
        storeFlowService.saveBatch(storeFlows);
    }

    /**
     * 此方法只提供内部调用，调用前应该做好权限处理
     * 修改订单状态
     *
     * @param orderSn     订单编号
     * @param orderStatus 订单状态
     */
    private void updateStatus(String orderSn, OrderStatusEnum orderStatus) {
        this.baseMapper.updateStatus(orderStatus.name(), orderSn);
    }


    /**
     * 普通商品订单确认
     * 修改订单状态为待发货
     * 发送订单状态变更消息
     *
     * @param orderSn 订单编号
     */
    @Transactional(rollbackFor = Exception.class)
    public void normalOrderConfirm(String orderSn) {
        OrderStatusEnum orderStatusEnum = null;
        Order order = this.getBySn(orderSn);
        //订单旧状态
        String oldStatus = order.getOrderStatus();
        if (DeliveryMethodEnum.SELF_PICK_UP.name().equals(order.getDeliveryMethod())) {
            orderStatusEnum = OrderStatusEnum.STAY_PICKED_UP;
        } else if (DeliveryMethodEnum.LOGISTICS.name().equals(order.getDeliveryMethod())) {
            orderStatusEnum = OrderStatusEnum.UNDELIVERED;
        }
        //修改订单
        this.update(new LambdaUpdateWrapper<Order>()
                .eq(Order::getSn, orderSn)
                .set(Order::getOrderStatus, orderStatusEnum.name()));

        //查询订单信息
        OrderDetailDTO orderDetailDTO = this.queryDetail(orderSn);
        //订单状态变更消息
        this.sendUpdateStatusMessage(orderDetailDTO, orderStatusEnum.valueOf(oldStatus), orderStatusEnum);
    }

}