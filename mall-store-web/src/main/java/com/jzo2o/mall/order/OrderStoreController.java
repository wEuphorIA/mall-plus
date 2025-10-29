package com.jzo2o.mall.order;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.NumberUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jzo2o.common.model.CurrentUser;
import com.jzo2o.common.utils.UserContext;
import com.jzo2o.mall.common.enums.OrderStatusEnum;
import com.jzo2o.mall.common.enums.ResultCode;
import com.jzo2o.mall.common.enums.ResultUtil;
import com.jzo2o.mall.common.exception.ServiceException;
import com.jzo2o.mall.common.model.AuthUser;
import com.jzo2o.mall.common.model.ResultMessage;
import com.jzo2o.mall.member.model.dto.MemberAddressDTO;
import com.jzo2o.mall.member.service.StoreLogisticsService;
import com.jzo2o.mall.order.model.domain.Order;
import com.jzo2o.mall.order.model.dto.*;
import com.jzo2o.mall.order.model.enums.OrderTagEnum;
import com.jzo2o.mall.order.service.LogisticsService;
import com.jzo2o.mall.order.service.OrderPackageService;
import com.jzo2o.mall.order.service.OrderPriceService;
import com.jzo2o.mall.order.service.OrderService;
import com.jzo2o.mall.system.model.dto.TracesDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 店铺端,订单接口
 **/
@Slf4j
@RestController
@RequestMapping("/order/order")
@Api(tags = "店铺端,订单接口")
public class OrderStoreController {

    /**
     * 订单
     */
    @Autowired
    private OrderService orderService;
    /**
     * 订单价格
     */
    @Autowired
    private OrderPriceService orderPriceService;
    /**
     * 物流公司
     */
    @Autowired
    private StoreLogisticsService storeLogisticsService;

    /**
     * 快递
     */
    @Autowired
    private LogisticsService logisticsService;

    @Autowired
    private OrderPackageService orderPackageService;


    @ApiOperation(value = "查询订单列表")
    @GetMapping
    public IPage<OrderSimpleDTO> queryMineOrder(OrderSearchParamsDTO orderSearchParams) {
        IPage<OrderSimpleDTO> orderSimpleDTOIPage = orderService.queryByParams(orderSearchParams);
        return orderSimpleDTOIPage;
    }


    @ApiOperation(value = "订单明细")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "orderSn", value = "订单编号", required = true, dataType = "String", paramType = "path")
    })
    @GetMapping(value = "/{orderSn}")
    public OrderDetailDTO detail(@NotNull @PathVariable String orderSn) {
        OrderDetailDTO orderDetailDTO = orderService.queryDetail(orderSn);
        return orderDetailDTO;
    }

    @ApiOperation(value = "修改收货人信息")
    @ApiImplicitParam(name = "orderSn", value = "订单sn", required = true, dataType = "String", paramType = "path")
    @PostMapping(value = "/update/{orderSn}/consignee")
    public ResultMessage<Object> consignee(@NotNull(message = "参数非法") @PathVariable String orderSn,
                                           @Valid MemberAddressDTO memberAddressDTO) {
        return ResultUtil.data(orderService.updateConsignee(orderSn, memberAddressDTO));
    }

    @ApiOperation(value = "修改订单价格")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "orderSn", value = "订单sn", required = true, dataType = "String", paramType = "path"),
            @ApiImplicitParam(name = "orderPrice", value = "订单价格", required = true, dataType = "Double", paramType = "query"),
    })
    @PutMapping(value = "/update/{orderSn}/price")
    public Order updateOrderPrice(@PathVariable String orderSn,
                                                  @NotNull(message = "订单价格不能为空") @RequestParam Double orderPrice) {
        if (!NumberUtil.isGreater(Convert.toBigDecimal(orderPrice), Convert.toBigDecimal(0))) {
            throw new ServiceException(ResultCode.ORDER_PRICE_ERROR);
        }
        Order order = orderPriceService.updatePrice(orderSn, orderPrice);
        return order;
    }

    @ApiOperation(value = "订单发货")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "orderSn", value = "订单sn", required = true, dataType = "String", paramType = "path"),
            @ApiImplicitParam(name = "logisticsNo", value = "发货单号", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "logisticsId", value = "物流公司", required = true, dataType = "String", paramType = "query")
    })
    @PostMapping(value = "/{orderSn}/delivery")
    public Order delivery(@NotNull(message = "参数非法") @PathVariable String orderSn,
                                          @NotNull(message = "发货单号不能为空") String logisticsNo,
                                          @NotNull(message = "请选择物流公司") String logisticsId) {
        Order delivery = orderService.delivery(orderSn, logisticsNo, logisticsId);
        return delivery;
    }

//    @PreventDuplicateSubmissions
//    @ApiOperation(value = "订单顺丰发货")
//    @ApiImplicitParam(name = "orderSn", value = "订单sn", required = true, dataType = "String", paramType = "path")
//    @PostMapping(value = "/{orderSn}/shunfeng/delivery")
//    public ResultMessage<Object> shunFengDelivery(@NotNull(message = "参数非法") @PathVariable String orderSn) {
//        return ResultUtil.data(orderService.shunFengDelivery(orderSn));
//    }

    @ApiOperation(value = "取消订单")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "orderSn", value = "订单编号", required = true, dataType = "String", paramType = "path"),
            @ApiImplicitParam(name = "reason", value = "取消原因", required = true, dataType = "String", paramType = "query")
    })
    @PostMapping(value = "/{orderSn}/cancel")
    public Order cancel(@PathVariable String orderSn, @RequestParam String reason) {
        Order cancel = orderService.cancel(orderSn, reason);
        return cancel;
    }

//    @ApiOperation(value = "根据核验码获取订单信息")
//    @ApiImplicitParam(name = "verificationCode", value = "核验码", required = true, paramType = "path")
//    @GetMapping(value = "/getOrderByVerificationCode/{verificationCode}")
//    public ResultMessage<Object> getOrderByVerificationCode(@PathVariable String verificationCode) {
//        return ResultUtil.data(orderService.getOrderByVerificationCode(verificationCode));
//    }

//    @PreventDuplicateSubmissions
//    @ApiOperation(value = "订单核验")
//    @ApiImplicitParams({
//            @ApiImplicitParam(name = "orderSn", value = "订单号", required = true, paramType = "path"),
//            @ApiImplicitParam(name = "verificationCode", value = "核验码", required = true, paramType = "path")
//    })
//    @PutMapping(value = "/take/{orderSn}/{verificationCode}")
//    public ResultMessage<Object> take(@PathVariable String orderSn, @PathVariable String verificationCode) {
//        return ResultUtil.data(orderService.take(orderSn, verificationCode));
//    }

//    @PreventDuplicateSubmissions
//    @ApiOperation(value = "订单核验")
//    @ApiImplicitParams({
//            @ApiImplicitParam(name = "verificationCode", value = "核验码", required = true, paramType = "path")
//    })
//    @PutMapping(value = "/take/{verificationCode}")
//    public ResultMessage<Object> take(@PathVariable String verificationCode) {
//        return ResultUtil.data(orderService.take(verificationCode));
//    }

    @ApiOperation(value = "查询物流踪迹")
    @ApiImplicitParam(name = "orderSn", value = "订单编号", required = true, dataType = "String", paramType = "path")
    @GetMapping(value = "/getTraces/{orderSn}")
    public TracesDTO getTraces(@NotBlank(message = "订单编号不能为空") @PathVariable String orderSn) {
        TracesDTO traces = orderService.getTraces(orderSn);
        return traces;
    }

    @ApiOperation(value = "下载待发货的订单列表", produces = "application/octet-stream")
    @GetMapping(value = "/downLoadDeliverExcel")
    public void downLoadDeliverExcel(HttpServletResponse response) {
        response.setHeader("Processed-Mark","1");
//        HttpServletResponse response = ThreadContextHolder.getHttpResponse();
        AuthUser authUser = UserContext.getCurrentUser();
        String storeId = authUser.getStoreId();
        //获取店铺已经选择物流公司列表
        List<String> logisticsName = storeLogisticsService.getStoreSelectedLogisticsName(storeId);
        //查询待发货订单
        OrderSearchParamsDTO orderSearchParamsDTO = new OrderSearchParamsDTO();
        orderSearchParamsDTO.setStoreId(storeId);
        orderSearchParamsDTO.setTag(OrderTagEnum.WAIT_SHIP.name());
        List<OrderExportDTO> orderExportDTOS = orderService.queryExportOrder(orderSearchParamsDTO);
        //下载订单批量发货Excel
        this.orderService.getBatchDeliverList(response, logisticsName,orderExportDTOS);

    }
//    @ApiOperation(value = "下载待发货的订单列表", produces = "application/octet-stream")
//    @GetMapping(value = "/downLoadDeliverExcel")
//    public void downLoadDeliverExcel(HttpServletResponse response) {
//        response.setHeader("Processed-Mark","1");
////        HttpServletResponse response = ThreadContextHolder.getHttpResponse();
//        AuthUser authUser = UserContext.getCurrentUser();
//        String storeId = authUser.getStoreId();
//        //获取店铺已经选择物流公司列表
//        List<String> logisticsName = storeLogisticsService.getStoreSelectedLogisticsName(storeId);
//        //下载订单批量发货Excel
//        this.orderService.getBatchDeliverList(response, logisticsName);
//
//    }

    @PostMapping(value = "/batchDeliver", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ApiOperation(value = "上传文件进行订单批量发货")
    public void batchDeliver(@RequestPart("files") MultipartFile files) {
        orderService.batchDeliver(files);
    }

    @ApiOperation(value = "查询订单导出列表")
    @GetMapping("/queryExportOrder")
    public List<OrderExportDTO> queryExportOrder(OrderSearchParamsDTO orderSearchParams) {
        List<OrderExportDTO> orderExportDTOS = orderService.queryExportOrder(orderSearchParams);
        return orderExportDTOS;
    }

    @ApiOperation(value = "创建电子面单")
    @PostMapping(value = "/{orderSn}/createElectronicsFaceSheet")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "orderSn", value = "订单号", required = true, paramType = "path"),
            @ApiImplicitParam(name = "logisticsId", value = "物流公司", required = true, dataType = "String", paramType = "query")
    })
    public Map createElectronicsFaceSheet(@NotNull(message = "参数非法") @PathVariable String orderSn,
                                                            @NotNull(message = "请选择物流公司") String logisticsId) {
        Map map = logisticsService.labelOrder(orderSn, logisticsId);
        return map;
    }

    @ApiOperation(value = "查看包裹列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "orderSn", value = "订单编号", required = true, dataType = "String", paramType = "path")
    })
    @GetMapping(value = "/getPackage/{orderSn}")
    public List<OrderPackageDTO> getPackage(@NotBlank(message = "订单编号不能为空") @PathVariable String orderSn) {
        List<OrderPackageDTO> orderPackageVOList = orderPackageService.getOrderPackageList(orderSn);
        return orderPackageVOList;
    }

    @ApiOperation(value = "查询物流踪迹")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "orderSn", value = "订单编号", required = true, dataType = "String", paramType = "path")
    })
    @GetMapping(value = "/getTracesList/{orderSn}")
    public List<OrderPackageDTO> getTracesList(@NotBlank(message = "订单编号不能为空") @PathVariable String orderSn) {
        List<OrderPackageDTO> orderPackageVOList = orderPackageService.getOrderPackageList(orderSn);
        return orderPackageVOList;
    }

    @ApiOperation(value = "订单包裹发货")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "orderSn", value = "订单sn", required = true, dataType = "String", paramType = "path"),
            @ApiImplicitParam(name = "logisticsNo", value = "发货单号", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "logisticsId", value = "物流公司", required = true, dataType = "String", paramType = "query")
    })
    @PostMapping(value = "/{orderSn}/partDelivery")
    public Order delivery(@RequestBody PartDeliveryParamsDTO partDeliveryParamsDTO) {
        Order order = orderService.partDelivery(partDeliveryParamsDTO);
        return order;
    }
}