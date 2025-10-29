package com.jzo2o.mall.order.controller;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.NumberUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jzo2o.mall.common.enums.ResultCode;
import com.jzo2o.mall.common.enums.ResultUtil;
import com.jzo2o.mall.common.model.ResultMessage;
import com.jzo2o.mall.order.model.dto.OrderDetailDTO;
import com.jzo2o.mall.order.model.dto.OrderExportDTO;
import com.jzo2o.mall.order.model.dto.OrderSearchParamsDTO;
import com.jzo2o.mall.order.model.dto.OrderSimpleDTO;
import com.jzo2o.mall.order.service.OrderService;
import com.jzo2o.mall.system.model.dto.TracesDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 管理端,订单API
 */
@RestController
@RequestMapping("/order/order")
@Api(tags = "管理端,订单API")
public class OrderManagerController {

    /**
     * 订单
     */
    @Autowired
    private OrderService orderService;
    /**
     * 订单价格
     */
//    @Autowired
//    private OrderPriceService orderPriceService;


    @ApiOperation(value = "查询订单列表分页")
    @GetMapping
    public IPage<OrderSimpleDTO> queryMineOrder(OrderSearchParamsDTO orderSearchParams) {
        IPage<OrderSimpleDTO> orderSimpleDTOIPage = orderService.queryByParams(orderSearchParams);
        return orderSimpleDTOIPage;
    }

    @ApiOperation(value = "查询订单导出列表")
    @GetMapping("/queryExportOrder")
    public List<OrderExportDTO> queryExportOrder(OrderSearchParamsDTO orderSearchParams) {
        List<OrderExportDTO> orderExportDTOS = orderService.queryExportOrder(orderSearchParams);
        return orderExportDTOS;
    }


    @ApiOperation(value = "订单明细")
    @ApiImplicitParam(name = "orderSn", value = "订单编号", required = true, dataType = "String", paramType = "path")
    @GetMapping(value = "/{orderSn}")
    public OrderDetailDTO detail(@PathVariable String orderSn) {
        OrderDetailDTO orderDetailDTO = orderService.queryDetail(orderSn);
        return orderDetailDTO;
    }


//    @PreventDuplicateSubmissions
//    @ApiOperation(value = "确认收款")
//    @ApiImplicitParam(name = "orderSn", value = "订单编号", required = true, dataType = "String", paramType = "path")
//    @PostMapping(value = "/{orderSn}/pay")
//    public ResultMessage<Object> payOrder(@PathVariable String orderSn) {
//        orderPriceService.adminPayOrder(orderSn);
//        return ResultUtil.success();
//    }

//    @PreventDuplicateSubmissions
//    @ApiOperation(value = "修改收货人信息")
//    @ApiImplicitParam(name = "orderSn", value = "订单sn", required = true, dataType = "String", paramType = "path")
//    @PostMapping(value = "/update/{orderSn}/consignee")
//    public ResultMessage<Order> consignee(@NotNull(message = "参数非法") @PathVariable String orderSn,
//                                          @Valid MemberAddressDTO memberAddressDTO) {
//        return ResultUtil.data(orderService.updateConsignee(orderSn, memberAddressDTO));
//    }

//    @PreventDuplicateSubmissions
//    @ApiOperation(value = "修改订单价格")
//    @ApiImplicitParams({
//            @ApiImplicitParam(name = "orderSn", value = "订单sn", required = true, dataType = "String", paramType = "path"),
//            @ApiImplicitParam(name = "price", value = "订单价格", required = true, dataType = "Double", paramType = "query"),
//    })
//    @PutMapping(value = "/update/{orderSn}/price")
//    public ResultMessage<Order> updateOrderPrice(@PathVariable String orderSn,
//                                                 @NotNull(message = "订单价格不能为空") @RequestParam Double price) {
//        if (NumberUtil.isGreater(Convert.toBigDecimal(price), Convert.toBigDecimal(0))) {
//            return ResultUtil.data(orderPriceService.updatePrice(orderSn, price));
//        } else {
//            return ResultUtil.error(ResultCode.ORDER_PRICE_ERROR);
//        }
//    }


//    @PreventDuplicateSubmissions
//    @ApiOperation(value = "取消订单")
//    @ApiImplicitParams({
//            @ApiImplicitParam(name = "orderSn", value = "订单编号", required = true, dataType = "String", paramType = "path"),
//            @ApiImplicitParam(name = "reason", value = "取消原因", required = true, dataType = "String", paramType = "query")
//    })
//    @PostMapping(value = "/{orderSn}/cancel")
//    public ResultMessage<Order> cancel(@ApiIgnore @PathVariable String orderSn, @RequestParam String reason) {
//        return ResultUtil.data(orderService.cancel(orderSn, reason));
//    }


    @ApiOperation(value = "查询物流踪迹")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "orderSn", value = "订单编号", required = true, dataType = "String", paramType = "path")
    })
    @PostMapping(value = "/getTraces/{orderSn}")
    public TracesDTO getTraces(@NotBlank(message = "订单编号不能为空") @PathVariable String orderSn) {
        TracesDTO traces = orderService.getTraces(orderSn);
        return traces;
    }
}