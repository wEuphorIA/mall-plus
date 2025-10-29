package com.jzo2o.mall.cart.controller;

import com.jzo2o.mall.cart.model.dto.TradeDTO;
import com.jzo2o.mall.cart.model.enums.CartTypeEnum;
import com.jzo2o.mall.cart.service.CartService;
import com.jzo2o.mall.common.enums.ResultCode;
import com.jzo2o.mall.common.enums.ResultUtil;
import com.jzo2o.mall.common.exception.ServiceException;
import com.jzo2o.mall.common.model.ResultMessage;
import com.jzo2o.mall.order.model.dto.ReceiptInputDTO;
import com.jzo2o.mall.order.model.dto.TradeParamsDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 买家端，购物车接口
 */
@Slf4j
@RestController
@Api(tags = "买家端，购物车接口")
@RequestMapping("/trade/carts")
public class CartController {

    /**
     * 购物车
     */
    @Autowired
    private CartService cartService;
    @ApiOperation(value = "向购物车中添加一个产品")
    @PostMapping
    @ApiImplicitParams({
            @ApiImplicitParam(name = "skuId", value = "产品ID", required = true, dataType = "Long", paramType = "query"),
            @ApiImplicitParam(name = "num", value = "此产品的购买数量", required = true, dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "cartType", value = "购物车类型，默认加入购物车", paramType = "query")
    })
    public void add(@NotNull(message = "产品id不能为空") String skuId,
                                     @NotNull(message = "购买数量不能为空") @Min(value = 1, message = "加入购物车数量必须大于0") Integer num,
                                     String cartType) {
        try {
            //读取选中的列表
            cartService.add(skuId, num, cartType, false);
        } catch (ServiceException se) {
            log.info(se.getMsg(), se);
            throw se;
        } catch (Exception e) {
            log.error(ResultCode.CART_ERROR.message(), e);
            throw new ServiceException(ResultCode.CART_ERROR);
        }
    }


    @ApiOperation(value = "获取购物车页面购物车详情")
    @GetMapping("/all")
    public TradeDTO cartAll() {
        TradeDTO allTradeDTO = this.cartService.getAllTradeDTO();
        return allTradeDTO;
    }

    @ApiOperation(value = "获取购物车数量")
    @GetMapping("/count")
    public long cartCount(@RequestParam(required = false) Boolean checked) {
        long cartNum = this.cartService.getCartNum(checked);
        return cartNum;
    }

//    @ApiOperation(value = "获取购物车可用优惠券数量")
//    @GetMapping("/coupon/num")
//    @ApiImplicitParams({
//            @ApiImplicitParam(name = "way", value = "购物车购买：CART/立即购买：BUY_NOW ", required = true, paramType = "query")
//    })
//    public ResultMessage<Long> cartCouponNum(String way) {
//        return ResultUtil.data(this.cartService.getCanUseCoupon(CartTypeEnum.valueOf(way)));
//    }

    @ApiOperation(value = "更新购物车中单个产品数量", notes = "更新购物车中的单个产品的数量或选中状态")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "skuId", value = "产品id数组", required = true, dataType = "Long", paramType = "path"),
            @ApiImplicitParam(name = "num", value = "产品数量", dataType = "int", paramType = "query"),
    })
    @PostMapping(value = "/sku/num/{skuId}")
    public void update(@NotNull(message = "产品id不能为空") @PathVariable(name = "skuId") String skuId,
                                        Integer num) {
        cartService.add(skuId, num, CartTypeEnum.CART.name(), true);
    }


    @ApiOperation(value = "更新购物车中单个产品", notes = "更新购物车中的单个产品的数量或选中状态")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "skuId", value = "产品id数组", required = true, dataType = "Long", paramType = "path")
    })
    @PostMapping(value = "/sku/checked/{skuId}")
    public void updateChecked(@NotNull(message = "产品id不能为空") @PathVariable(name = "skuId") String skuId,
                                               boolean checked) {
        cartService.checked(skuId, checked);
    }


    @ApiOperation(value = "购物车选中设置")
    @PostMapping(value = "/sku/checked", produces = MediaType.APPLICATION_JSON_VALUE)
    public void updateAll(boolean checked) {
        cartService.checkedAll(checked);
    }


    @ApiOperation(value = "批量设置某商家的商品为选中或不选中")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "storeId", value = "卖家id", required = true, dataType = "Long", paramType = "path")
    })
    @ResponseBody
    @PostMapping(value = "/store/{storeId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public void updateStoreAll(@NotNull(message = "卖家id不能为空") @PathVariable(name = "storeId") String storeId, boolean checked) {
        cartService.checkedStore(storeId, checked);
    }


    @ApiOperation(value = "清空购物车")
    @DeleteMapping()
    public void clean() {
        cartService.clean();
    }


    @ApiOperation(value = "删除购物车中的一个或多个产品")
    @DeleteMapping(value = "/sku/remove")
    public void delete(String[] skuIds) {
        cartService.delete(skuIds);
    }


    @ApiOperation(value = "获取结算页面购物车详情")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "way", value = "购物车购买：CART/立即购买：BUY_NOW", required = true, paramType = "query")
    })
    @GetMapping("/checked")
    public TradeDTO cartChecked(@NotNull(message = "读取选中列表") String way) {
        try {
            //读取选中的列表
            TradeDTO checkedTradeDTO = this.cartService.getCheckedTradeDTO(CartTypeEnum.valueOf(way));
            return checkedTradeDTO;
        } catch (ServiceException se) {
            log.error(se.getMsg(), se);
            throw se;
        } catch (Exception e) {
            log.error(ResultCode.CART_ERROR.message(), e);
            throw new ServiceException(ResultCode.CART_ERROR);
        }
    }

    @ApiOperation(value = "选择收货地址")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "shippingAddressId", value = "收货地址id ", required = true, paramType = "query"),
            @ApiImplicitParam(name = "way", value = "购物车类型 ", paramType = "query")
    })
    @GetMapping("/shippingAddress")
    public void shippingAddress(@NotNull(message = "收货地址ID不能为空") String shippingAddressId,
                                                 String way) {
        try {
            cartService.shippingAddress(shippingAddressId, way);
        } catch (ServiceException se) {
            log.error(ResultCode.SHIPPING_NOT_APPLY.message(), se);
            throw new ServiceException(ResultCode.SHIPPING_NOT_APPLY);
        } catch (Exception e) {
            log.error(ResultCode.CART_ERROR.message(), e);
            throw new ServiceException(ResultCode.CART_ERROR);
        }
    }

    @ApiOperation(value = "选择配送方式")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "shippingMethod", value = "配送方式：SELF_PICK_UP(自提)," +
                    "LOCAL_TOWN_DELIVERY(同城配送)," +
                    "LOGISTICS(物流) ", required = true, paramType = "query"),
            @ApiImplicitParam(name = "way", value = "购物车类型 ", paramType = "query")
    })
    @PutMapping("/shippingMethod")
    public void shippingMethod(@NotNull(message = "配送方式不能为空") String shippingMethod,
                                                String way) {
        try {
            cartService.shippingMethod( shippingMethod, way);
        } catch (ServiceException se) {
            log.error(se.getMsg(), se);
            throw se;
        } catch (Exception e) {
            log.error(ResultCode.CART_ERROR.message(), e);
            throw new ServiceException(ResultCode.CART_ERROR);
        }
    }

    @ApiOperation(value = "获取用户可选择的物流方式")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "way", value = "购物车类型 ", paramType = "query")
    })
    @GetMapping("/shippingMethodList")
    public List<String> shippingMethodList(String way) {
        try {
            List<String> strings = cartService.shippingMethodList(way);
            return strings;
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new ServiceException(ResultCode.ERROR);
        }
    }

    @ApiOperation(value = "选择发票")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "way", value = "购物车购买：CART/立即购买：BUY_NOW ", required = true, paramType = "query"),
    })
    @GetMapping("/select/receipt")
    public void selectReceipt(String way, ReceiptInputDTO receipt) {
        this.cartService.shippingReceipt(receipt, way);
    }

    @ApiOperation(value = "选择优惠券")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "way", value = "购物车购买：CART/立即购买：BUY_NOW", required = true, paramType = "query"),
            @ApiImplicitParam(name = "memberCouponId", value = "优惠券id ", required = true, paramType = "query"),
            @ApiImplicitParam(name = "used", value = "使用true 弃用false ", required = true, paramType = "query")
    })
    @GetMapping("/select/coupon")
    public void selectCoupon(String way, @NotNull(message = "优惠券id不能为空") String memberCouponId, boolean used) {
        this.cartService.selectCoupon(memberCouponId, way, used);
    }
}
