package com.jzo2o.mall.order.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jzo2o.mall.aftersale.model.domain.AfterSale;
import com.jzo2o.mall.aftersale.model.domain.AfterSaleLog;
import com.jzo2o.mall.aftersale.model.domain.AfterSaleReason;
import com.jzo2o.mall.aftersale.model.dto.AfterSaleApplyDTO;
import com.jzo2o.mall.aftersale.model.dto.AfterSaleDTO;
import com.jzo2o.mall.aftersale.model.dto.AfterSaleSearchParamsDTO;
import com.jzo2o.mall.aftersale.service.AfterSaleLogService;
import com.jzo2o.mall.aftersale.service.AfterSaleReasonService;
import com.jzo2o.mall.aftersale.service.AfterSaleService;
import com.jzo2o.mall.common.enums.ResultUtil;
import com.jzo2o.mall.common.model.ResultMessage;
import com.jzo2o.mall.member.model.dto.StoreAfterSaleAddressDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

/**
 * 买家端,售后管理接口
 */
@RestController
@Api(tags = "买家端,售后管理接口")
@RequestMapping("/order/afterSale")
public class AfterSaleBuyerController {
    /**
     * 售后
     */
    @Autowired
    private AfterSaleService afterSaleService;
    /**
     * 售后原因
     */
    @Autowired
    private AfterSaleReasonService afterSaleReasonService;
    /**
     * 售后日志
     */
    @Autowired
    private AfterSaleLogService afterSaleLogService;

    @ApiOperation(value = "查看售后服务详情")
    @ApiImplicitParam(name = "sn", value = "售后单号", required = true, paramType = "path")
    @GetMapping(value = "/get/{sn}")
    public AfterSaleDTO get(@NotNull(message = "售后单号") @PathVariable("sn") String sn) {
        AfterSaleDTO afterSale = afterSaleService.getAfterSale(sn);
        return afterSale;
    }

    @ApiOperation(value = "分页获取售后服务")
    @GetMapping(value = "/page")
    public IPage<AfterSaleDTO> getByPage(AfterSaleSearchParamsDTO searchParams) {
        IPage<AfterSaleDTO> afterSalePages = afterSaleService.getAfterSalePages(searchParams);
        return afterSalePages;
    }

    @ApiOperation(value = "获取申请售后页面信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "sn", value = "订单货物编号", required = true, dataType = "String", paramType = "path")
    })
    @GetMapping(value = "/applyAfterSaleInfo/{sn}")
    public AfterSaleApplyDTO applyAfterSaleInfo(@PathVariable String sn) {
        AfterSaleApplyDTO afterSaleDTO = afterSaleService.getAfterSaleVO(sn);
        return afterSaleDTO;
    }

    @PostMapping(value = "/save/{orderItemSn}")
    @ApiImplicitParam(name = "orderItemSn", value = "订单货物编号", required = true, paramType = "query")
    @ApiOperation(value = "申请售后")
    public AfterSale save(AfterSaleDTO afterSaleDTO) {
        AfterSale afterSale = afterSaleService.saveAfterSale(afterSaleDTO);
        return afterSale;

    }

    @ApiOperation(value = "买家 退回 物流信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "afterSaleSn", value = "售后sn", required = true, dataType = "String", paramType = "path"),
            @ApiImplicitParam(name = "logisticsNo", value = "发货单号", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "logisticsId", value = "物流公司id", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "mDeliverTime", value = "买家发货时间", required = true, dataType = "date", paramType = "query")

    })
    @PostMapping(value = "/delivery/{afterSaleSn}")
    public AfterSale delivery(@NotNull(message = "售后编号不能为空") @PathVariable("afterSaleSn") String afterSaleSn,
                                             @NotNull(message = "发货单号不能为空") @RequestParam String logisticsNo,
                                             @NotNull(message = "请选择物流公司") @RequestParam String logisticsId,
                                             @NotNull(message = "请选择发货时间") @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date mDeliverTime) {
        AfterSale afterSale = afterSaleService.buyerDelivery(afterSaleSn, logisticsNo, logisticsId, mDeliverTime);
        return afterSale;
    }

    @ApiOperation(value = "售后，取消售后")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "afterSaleSn", value = "售后sn", required = true, dataType = "String", paramType = "path")
    })
    @PostMapping(value = "/cancel/{afterSaleSn}")
    public AfterSale cancel(@NotNull(message = "参数非法") @PathVariable("afterSaleSn") String afterSaleSn) {
        AfterSale cancel = afterSaleService.cancel(afterSaleSn);
        return cancel;
    }

    @ApiOperation(value = "获取商家售后收件地址")
    @ApiImplicitParam(name = "sn", value = "售后单号", required = true, paramType = "path")
    @GetMapping(value = "/getStoreAfterSaleAddress/{sn}")
    public StoreAfterSaleAddressDTO  getStoreAfterSaleAddress(@NotNull(message = "售后单号") @PathVariable("sn") String sn) {
        StoreAfterSaleAddressDTO storeAfterSaleAddressDTO = afterSaleService.getStoreAfterSaleAddressDTO(sn);
        return storeAfterSaleAddressDTO;
    }

    @ApiOperation(value = "获取售后原因")
    @ApiImplicitParam(name = "serviceType", value = "售后类型", required = true, paramType = "path", allowableValues = "CANCEL,RETURN_GOODS,RETURN_MONEY,COMPLAIN")
    @GetMapping(value = "/get/afterSaleReason/{serviceType}")
    public List<AfterSaleReason> getAfterSaleReason(@PathVariable String serviceType) {
        List<AfterSaleReason> afterSaleReasons = afterSaleReasonService.afterSaleReasonList(serviceType);
        return afterSaleReasons;
    }

    @ApiOperation(value = "获取售后日志")
    @ApiImplicitParam(name = "sn", value = "售后编号", required = true, paramType = "path")
    @GetMapping(value = "/get/getAfterSaleLog/{sn}")
    public List<AfterSaleLog> getAfterSaleLog(@PathVariable String sn) {
        List<AfterSaleLog> afterSaleLog = afterSaleLogService.getAfterSaleLog(sn);
        return afterSaleLog;
    }

}
