package com.jzo2o.mall.order.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jzo2o.mall.common.enums.ResultUtil;
import com.jzo2o.mall.common.model.ResultMessage;
import com.jzo2o.mall.order.model.domain.Bill;
import com.jzo2o.mall.order.model.domain.StoreFlow;
import com.jzo2o.mall.order.model.dto.BillListDTO;
import com.jzo2o.mall.order.model.dto.BillSearchParamsDTO;
import com.jzo2o.mall.order.service.BillService;
import com.jzo2o.mall.order.service.StoreFlowService;
import com.jzo2o.mysql.domain.PageVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;

/**
 * 管理端,商家结算单接口
 */
@RestController
@Api(tags = "管理端,商家结算单接口")
@RequestMapping("/order/bill")
public class BillManagerController {
    @Autowired
    private BillService billService;

    @Autowired
    private StoreFlowService storeFlowService;

    @ApiOperation(value = "通过id获取结算单")
    @ApiImplicitParam(name = "id", value = "结算单ID", required = true, paramType = "path")
    @GetMapping(value = "/get/{id}")
    public Bill get(@PathVariable @NotNull String id) {
        Bill bill = billService.getById(id);
        return bill;
    }

    @ApiOperation(value = "获取结算单分页")
    @GetMapping(value = "/getByPage")
    public IPage<BillListDTO> getByPage(BillSearchParamsDTO billSearchParams) {
        IPage<BillListDTO> billListDTOIPage = billService.billPage(billSearchParams);
        return billListDTOIPage;
    }

    @ApiOperation(value = "获取商家结算单流水分页")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "结算单ID", required = true, paramType = "path"),
            @ApiImplicitParam(name = "flowType", value = "流水类型:PAY、REFUND", paramType = "query")
    })
    @GetMapping(value = "/{id}/getStoreFlow")
    public IPage<StoreFlow> getStoreFlow(@PathVariable String id, String flowType, PageVO pageVO) {
        IPage<StoreFlow> storeFlow = storeFlowService.getStoreFlow(id, flowType, pageVO);
        return storeFlow;
    }

    @ApiOperation(value = "支付结算单")
    @ApiImplicitParam(name = "id", value = "结算单ID", required = true, paramType = "path")
    @PutMapping(value = "/pay/{id}")
    public void pay(@PathVariable String id) {
        billService.complete(id);
    }

    @ApiOperation(value = "下载结算单", produces = "application/octet-stream")
    @ApiImplicitParam(name = "id", value = "结算单ID", required = true, paramType = "path", dataType = "String")
    @GetMapping(value = "/downLoad/{id}")
    public void downLoadDeliverExcel(@PathVariable String id,HttpServletResponse response) {
        response.setHeader("Processed-Mark","1");
//        OperationalJudgment.judgment(billService.getById(id));
//        HttpServletResponse response = ThreadContextHolder.getHttpResponse();
        billService.download(response, id);

    }


}
