package com.jzo2o.mall.order.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jzo2o.common.utils.UserContext;
import com.jzo2o.mall.cart.model.dto.TradeDTO;
import com.jzo2o.mall.common.enums.OrderStatusEnum;
import com.jzo2o.mall.common.enums.ResultCode;
import com.jzo2o.mall.common.enums.ResultUtil;
import com.jzo2o.mall.common.exception.ServiceException;
import com.jzo2o.mall.common.model.AuthUser;
import com.jzo2o.mall.common.model.ResultMessage;
import com.jzo2o.mall.order.model.domain.Order;
import com.jzo2o.mall.order.model.domain.Trade;
import com.jzo2o.mall.order.model.dto.*;
import com.jzo2o.mall.order.service.OrderPackageService;
import com.jzo2o.mall.order.service.OrderService;
import com.jzo2o.mall.order.service.TradeService;
import com.jzo2o.mall.system.model.dto.TracesDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Objects;

/**
 * 买家端,订单接口
 */
@RestController
@Api(tags = "买家端,订单接口")
@RequestMapping("/order/order")
@Slf4j
public class OrderBuyerController {
    /**
     * 订单
     */
    @Autowired
    private OrderService orderService;

    @Autowired
    private TradeService tradeService;

    @Autowired
    private OrderPackageService orderPackageService;

    @ApiOperation(value = "创建交易")
    @PostMapping(value = "/create/trade", consumes = "application/json", produces = "application/json")
    public TradeDTO createTrade(@RequestBody TradeParamsDTO tradeParams) {
        //读取选中的列表
        TradeDTO trade = tradeService.createTrade(tradeParams);
        return trade;
    }

    @ApiOperation(value = "查询会员订单列表")
    @GetMapping
    public IPage<OrderSimpleDTO> queryMineOrder(OrderSearchParamsDTO orderSearchParams) {
        AuthUser currentUser = Objects.requireNonNull(UserContext.getCurrentUser());
        orderSearchParams.setMemberId(currentUser.getIdString());
        IPage<OrderSimpleDTO> orderSimpleDTOIPage = orderService.queryByParams(orderSearchParams);
        return orderSimpleDTOIPage;
    }

    @ApiOperation(value = "订单明细")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "orderSn", value = "订单编号", required = true, paramType = "path")
    })
    @GetMapping(value = "/{orderSn}")
    public OrderDetailDTO detail(@NotNull(message = "订单编号不能为空") @PathVariable("orderSn") String orderSn) {
        OrderDetailDTO orderDetailDTO = orderService.queryDetail(orderSn);
        return orderDetailDTO;
    }

    @ApiOperation(value = "确认收货")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "orderSn", value = "订单编号", required = true, paramType = "path")
    })
    @PostMapping(value = "/{orderSn}/receiving")
    public void receiving(@NotNull(message = "订单编号不能为空") @PathVariable("orderSn") String orderSn) {
        Order order = orderService.getBySn(orderSn);
        if (order == null) {
            throw new ServiceException(ResultCode.ORDER_NOT_EXIST);
        }
        //判定是否是待收货状态
        if (!order.getOrderStatus().equals(OrderStatusEnum.DELIVERED.name())) {
            throw new ServiceException(ResultCode.ORDER_DELIVERED_ERROR);
        }
        orderService.complete(orderSn);
    }
    @ApiOperation(value = "取消订单")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "orderSn", value = "订单编号", required = true, dataType = "String", paramType = "path"),
            @ApiImplicitParam(name = "reason", value = "取消原因", required = true, dataType = "String", paramType = "query")
    })
    @PostMapping(value = "/{orderSn}/cancel")
    public void cancel(@ApiIgnore @PathVariable String orderSn, @RequestParam String reason) {
        orderService.cancel(orderSn, reason);
    }

    @ApiOperation(value = "删除订单")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "orderSn", value = "订单编号", required = true, dataType = "String", paramType = "path")
    })
    @DeleteMapping(value = "/{orderSn}")
    public void deleteOrder(@PathVariable String orderSn) {
        orderService.deleteOrder(orderSn);
    }

    @ApiOperation(value = "查询物流踪迹")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "orderSn", value = "订单编号", required = true, dataType = "String", paramType = "path")
    })
    @PostMapping(value = "/getTraces/{orderSn}")
    public TracesDTO getTraces(@NotBlank(message = "订单编号不能为空") @PathVariable String orderSn) {
        TracesDTO traces = orderService.getTraces(orderSn);
        return traces;
    }
    @ApiOperation(value = "查询物流踪迹")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "orderSn", value = "订单编号", required = true, dataType = "String", paramType = "path")
    })
    @GetMapping(value = "/getTracesList/{orderSn}")
    public List<OrderPackageDTO> getTracesList(@NotBlank(message = "订单编号不能为空") @PathVariable String orderSn) {
        List<OrderPackageDTO> orderPackageList = orderPackageService.getOrderPackageList(orderSn);
        return orderPackageList;
    }

    @ApiOperation(value = "查看包裹列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "orderSn", value = "订单编号", required = true, dataType = "String", paramType = "path")
    })
    @GetMapping(value = "/getPackage/{orderSn}")
    public List<OrderPackageDTO> getPackage(@NotBlank(message = "订单编号不能为空") @PathVariable String orderSn) {
        List<OrderPackageDTO> orderPackageList = orderPackageService.getOrderPackageList(orderSn);
        return orderPackageList;
    }
}
