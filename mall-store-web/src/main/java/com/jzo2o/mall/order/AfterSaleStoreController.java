package com.jzo2o.mall.order;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jzo2o.common.utils.UserContext;
import com.jzo2o.mall.aftersale.model.domain.AfterSale;
import com.jzo2o.mall.aftersale.model.dto.AfterSaleDTO;
import com.jzo2o.mall.aftersale.model.dto.AfterSaleSearchParamsDTO;
import com.jzo2o.mall.aftersale.service.AfterSaleService;
import com.jzo2o.mall.common.enums.ResultUtil;
import com.jzo2o.mall.common.model.AuthUser;
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
import java.util.Objects;

/**
 * 店铺端,售后管理接口
 */
@RestController
@Api(tags = "店铺端,售后管理接口")
@RequestMapping("/order/afterSale")
public class AfterSaleStoreController {

    @Autowired
    private AfterSaleService afterSaleService;

    @ApiOperation(value = "查看售后服务详情")
    @ApiImplicitParam(name = "sn", value = "售后单号", required = true, paramType = "path")
    @GetMapping(value = "/{sn}")
    public AfterSaleDTO get(@PathVariable String sn) {
        AfterSaleDTO afterSale = afterSaleService.getAfterSale(sn);
        return afterSale;
    }

    @ApiOperation(value = "分页获取售后服务")
    @GetMapping(value = "/page")
    public IPage<AfterSaleDTO> getByPage(AfterSaleSearchParamsDTO searchParams) {
        AuthUser authUser = UserContext.getCurrentUser();
        String storeId = authUser.getStoreId();
        searchParams.setStoreId(storeId);
        IPage<AfterSaleDTO> afterSalePages = afterSaleService.getAfterSalePages(searchParams);
        return afterSalePages;
    }

    @ApiOperation(value = "获取导出售后服务列表列表")
    @GetMapping(value = "/exportAfterSaleOrder")
    public List<AfterSale> exportAfterSaleOrder(AfterSaleSearchParamsDTO searchParams) {
        AuthUser authUser = UserContext.getCurrentUser();
        String storeId = authUser.getStoreId();
        searchParams.setStoreId(storeId);
        List<AfterSale> afterSales = afterSaleService.exportAfterSaleOrder(searchParams);
        return afterSales;
    }

    @ApiOperation(value = "审核售后申请")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "afterSaleSn", value = "售后sn", required = true, paramType = "path"),
            @ApiImplicitParam(name = "serviceStatus", value = "PASS：审核通过，REFUSE：审核未通过", required = true, paramType = "query"),
            @ApiImplicitParam(name = "remark", value = "备注", paramType = "query"),
            @ApiImplicitParam(name = "actualRefundPrice", value = "实际退款金额", paramType = "query")
    })
    @PutMapping(value = "/review/{afterSaleSn}")
    public AfterSale review(@NotNull(message = "请选择售后单") @PathVariable String afterSaleSn,
                                           @NotNull(message = "请审核") String serviceStatus,
                                           String remark,
                                           Double actualRefundPrice) {
        AfterSale review = afterSaleService.review(afterSaleSn, serviceStatus, remark, actualRefundPrice);
        return review;
    }

    @ApiOperation(value = "卖家确认收货")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "afterSaleSn", value = "售后sn", required = true, paramType = "path"),
            @ApiImplicitParam(name = "serviceStatus", value = "PASS：审核通过，REFUSE：审核未通过", required = true, paramType = "query"),
            @ApiImplicitParam(name = "remark", value = "备注", paramType = "query")
    })
    @PutMapping(value = "/confirm/{afterSaleSn}")
    public AfterSale confirm(@NotNull(message = "请选择售后单") @PathVariable String afterSaleSn,
                                            @NotNull(message = "请审核") String serviceStatus,
                                            String remark) {
        AfterSale afterSale = afterSaleService.storeConfirm(afterSaleSn, serviceStatus, remark);
        return afterSale;
    }

    @ApiOperation(value = "查看买家退货物流踪迹")
    @ApiImplicitParam(name = "sn", value = "售后单号", required = true, paramType = "path")
    @GetMapping(value = "/getDeliveryTraces/{sn}")
    public TracesDTO getDeliveryTraces(@PathVariable String sn) {
        TracesDTO tracesDTO = afterSaleService.deliveryTraces(sn);
        return tracesDTO;
    }

    @ApiOperation(value = "获取商家售后收件地址")
    @ApiImplicitParam(name = "sn", value = "售后单号", required = true, paramType = "path")
    @GetMapping(value = "/getStoreAfterSaleAddress/{sn}")
    public StoreAfterSaleAddressDTO getStoreAfterSaleAddress(@NotNull(message = "售后单号") @PathVariable("sn") String sn) {
        StoreAfterSaleAddressDTO storeAfterSaleAddressDTO = afterSaleService.getStoreAfterSaleAddressDTO(sn);
        return storeAfterSaleAddressDTO;
    }

}
