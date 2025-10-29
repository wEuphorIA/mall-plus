package com.jzo2o.mall.order;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jzo2o.common.utils.UserContext;
import com.jzo2o.mall.common.enums.ResultUtil;
import com.jzo2o.mall.common.model.AuthUser;
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
import org.apache.http.auth.AUTH;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.Objects;

/**
 * 店铺端,结算单接口
 */
@RestController
@Api(tags = "店铺端,结算单接口")
@RequestMapping("/order/bill")
public class BillStoreController {

    @Autowired
    private BillService billService;

    @Autowired
    private StoreFlowService storeFlowService;

    @ApiOperation(value = "获取结算单分页")
    @GetMapping(value = "/getByPage")
    public IPage<BillListDTO> getByPage(BillSearchParamsDTO billSearchParams) {
        AuthUser authUser = UserContext.getCurrentUser();
        String storeId = authUser.getStoreId();
        billSearchParams.setStoreId(storeId);
        IPage<BillListDTO> billListDTOIPage = billService.billPage(billSearchParams);
        return billListDTOIPage;
    }

    @ApiOperation(value = "通过id获取结算单")
    @ApiImplicitParam(name = "id", value = "结算单ID", required = true, paramType = "path", dataType = "String")
    @GetMapping(value = "/get/{id}")
    public Bill get(@PathVariable String id) {
        Bill bill = billService.getById(id);
        return bill;
    }

    @ApiOperation(value = "获取商家结算单流水分页")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "结算单ID", required = true, paramType = "path", dataType = "String"),
            @ApiImplicitParam(name = "flowType", value = "流水类型:PAY、REFUND", paramType = "query", dataType = "String")
    })
    @GetMapping(value = "/{id}/getStoreFlow")
    public IPage<StoreFlow> getStoreFlow(@PathVariable String id, String flowType, PageVO pageVO) {
        IPage<StoreFlow> storeFlow = storeFlowService.getStoreFlow(id, flowType, pageVO);
        return storeFlow;
    }

    @ApiOperation(value = "获取商家分销订单流水分页")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "结算单ID", required = true, paramType = "path", dataType = "String")
    })
    @GetMapping(value = "/{id}/getDistributionFlow")
    public IPage<StoreFlow> getDistributionFlow(@PathVariable String id, PageVO pageVO) {
//        OperationalJudgment.judgment(billService.getById(id));
        IPage<StoreFlow> distributionFlow = storeFlowService.getDistributionFlow(id, pageVO);
        return distributionFlow;
    }

    @ApiOperation(value = "核对结算单")
    @ApiImplicitParam(name = "id", value = "结算单ID", required = true, paramType = "path", dataType = "String")
    @PutMapping(value = "/check/{id}")
    public void examine(@PathVariable String id) {
//        OperationalJudgment.judgment(billService.getById(id));
        billService.check(id);
    }

    @ApiOperation(value = "下载结算单", produces = "application/octet-stream")
    @ApiImplicitParam(name = "id", value = "结算单ID", required = true, paramType = "path", dataType = "String")
    @GetMapping(value = "/downLoad/{id}")
    public void downLoadDeliverExcel(@PathVariable String id,HttpServletResponse response ) {
        response.setHeader("Processed-Mark","1");
//        OperationalJudgment.judgment(billService.getById(id));
//        HttpServletResponse response = ThreadContextHolder.getHttpResponse();
        billService.download(response, id);

    }

}
