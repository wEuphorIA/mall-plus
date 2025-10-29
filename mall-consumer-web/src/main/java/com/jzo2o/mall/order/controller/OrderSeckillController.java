package com.jzo2o.mall.order.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jzo2o.common.utils.UserContext;
import com.jzo2o.mall.cart.model.dto.TradeDTO;
import com.jzo2o.mall.cart.service.CartService;
import com.jzo2o.mall.common.enums.OrderStatusEnum;
import com.jzo2o.mall.common.enums.ResultCode;
import com.jzo2o.mall.common.exception.ServiceException;
import com.jzo2o.mall.common.model.AuthUser;
import com.jzo2o.mall.order.model.domain.Order;
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

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Objects;

/**
 * 买家端,订单接口
 */
@RestController
@Api(tags = "买家端,秒杀接口")
@RequestMapping("/order/seckill")
@Slf4j
public class OrderSeckillController {

    /**
     * 订单
     */
    @Autowired
    private OrderService orderService;

    @Autowired
    private TradeService tradeService;

    @Autowired
    private OrderPackageService orderPackageService;

    /**
     * 购物车
     */
    @Autowired
    private CartService cartService;


    @ApiOperation(value = "秒杀一个商品")
    @PostMapping
    @ApiImplicitParams({
            @ApiImplicitParam(name = "skuId", value = "产品ID", required = true, dataType = "Long", paramType = "query"),
            @ApiImplicitParam(name = "num", value = "此产品的购买数量", required = true, dataType = "int", paramType = "query")
    })
    public void add(@NotNull(message = "产品id不能为空") String skuId,
                    @NotNull(message = "购买数量不能为空") @Min(value = 1, message = "加入购物车数量必须大于0") Integer num) {
        try {
            //读取选中的列表
            cartService.seckill(skuId, num);
        } catch (ServiceException se) {
            log.info(se.getMsg(), se);
            throw se;
        } catch (Exception e) {
            log.error(ResultCode.CART_ERROR.message(), e);
            throw new ServiceException(ResultCode.CART_ERROR);
        }
    }

    @ApiOperation(value = "创建秒杀交易")
    @PostMapping(value = "/create/trade", consumes = "application/json", produces = "application/json")
    public TradeDTO createSeckillTrade(@RequestBody TradeParamsDTO tradeParams) {
        try {
            //读取选中的列表
            TradeDTO trade = tradeService.createSeckillTrade(tradeParams);
            return trade;
        } catch (ServiceException se) {
            log.info(se.getMsg(), se);
            throw se;
        } catch (Exception e) {
            log.error(ResultCode.ORDER_ERROR.message(), e);
            throw e;
        }
    }

}
