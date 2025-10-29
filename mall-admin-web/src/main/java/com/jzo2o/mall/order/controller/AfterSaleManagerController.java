package com.jzo2o.mall.order.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jzo2o.mall.aftersale.model.domain.AfterSale;
import com.jzo2o.mall.aftersale.model.dto.AfterSaleDTO;
import com.jzo2o.mall.aftersale.model.dto.AfterSaleSearchParamsDTO;
import com.jzo2o.mall.aftersale.service.AfterSaleService;
import com.jzo2o.mall.common.enums.ResultUtil;
import com.jzo2o.mall.common.model.ResultMessage;
import com.jzo2o.mall.member.model.dto.StoreAfterSaleAddressDTO;
import com.jzo2o.mall.system.model.dto.TracesDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 管理端,售后接口
 */
@RestController
@RequestMapping("/order/afterSale")
@Api(tags = "管理端,售后接口")
public class AfterSaleManagerController {

    /**
     * 售后
     */
    @Autowired
    private AfterSaleService afterSaleService;

    @ApiOperation(value = "分页获取售后服务")
    @GetMapping(value = "/page")
    public IPage<AfterSaleDTO> getByPage(AfterSaleSearchParamsDTO searchParams) {
        IPage<AfterSaleDTO> afterSalePages = afterSaleService.getAfterSalePages(searchParams);
        return afterSalePages;
    }

    @ApiOperation(value = "获取导出售后服务列表列表")
    @GetMapping(value = "/exportAfterSaleOrder")
    public List<AfterSale> exportAfterSaleOrder(AfterSaleSearchParamsDTO searchParams) {
        List<AfterSale> afterSales = afterSaleService.exportAfterSaleOrder(searchParams);
        return afterSales;
    }

    //将下边的代码照上边两个方法修改下
    @ApiOperation(value = "查看售后服务详情")
    @ApiImplicitParam(name = "sn", value = "售后单号", required = true, paramType = "path")
    @GetMapping(value = "/get/{sn}")
    public AfterSaleDTO get(@NotNull(message = "售后单号") @PathVariable("sn") String sn) {
        AfterSaleDTO afterSale = afterSaleService.getAfterSale(sn);
        return afterSale;
    }

    @ApiOperation(value = "查看买家退货物流踪迹")
    @ApiImplicitParam(name = "sn", value = "售后单号", required = true, paramType = "path")
    @GetMapping(value = "/getDeliveryTraces/{sn}")
    public TracesDTO getDeliveryTraces(@PathVariable String sn) {
        TracesDTO tracesDTO = afterSaleService.deliveryTraces(sn);
        return tracesDTO;
    }

//    @PreventDuplicateSubmissions
//    @ApiOperation(value = "售后线下退款")
//    @ApiImplicitParams({
//            @ApiImplicitParam(name = "afterSaleSn", value = "售后sn", required = true, paramType = "path"),
//            @ApiImplicitParam(name = "remark", value = "备注", paramType = "query")
//    })
//    @PutMapping(value = "/refund/{afterSaleSn}")
//    public ResultMessage<AfterSale> refund(@NotNull(message = "请选择售后单") @PathVariable String afterSaleSn,
//                                           @RequestParam String remark) {
//
//        return ResultUtil.data(afterSaleService.refund(afterSaleSn, remark));
//    }

//    @PreventDuplicateSubmissions
//    @ApiOperation(value = "审核售后申请")
//    @ApiImplicitParams({
//            @ApiImplicitParam(name = "afterSaleSn", value = "售后sn", required = true, paramType = "path"),
//            @ApiImplicitParam(name = "serviceStatus", value = "PASS：审核通过，REFUSE：审核未通过", required = true, paramType = "query"),
//            @ApiImplicitParam(name = "remark", value = "备注", paramType = "query"),
//            @ApiImplicitParam(name = "actualRefundPrice", value = "实际退款金额", paramType = "query")
//    })
//    @PutMapping(value = "/review/{afterSaleSn}")
//    public ResultMessage<AfterSale> review(@NotNull(message = "请选择售后单") @PathVariable String afterSaleSn,
//                                           @NotNull(message = "请审核") String serviceStatus,
//                                           String remark,
//                                           Double actualRefundPrice) {
//
//        return ResultUtil.data(afterSaleService.review(afterSaleSn, serviceStatus, remark,actualRefundPrice));
//    }

//    @ApiOperation(value = "获取商家售后收件地址")
//    @ApiImplicitParam(name = "sn", value = "售后单号", required = true, paramType = "path")
//    @GetMapping(value = "/getStoreAfterSaleAddress/{sn}")
//    public ResultMessage<StoreAfterSaleAddressDTO> getStoreAfterSaleAddress(@NotNull(message = "售后单号") @PathVariable("sn") String sn) {
//        return ResultUtil.data(afterSaleService.getStoreAfterSaleAddressDTO(sn));
//    }
}
