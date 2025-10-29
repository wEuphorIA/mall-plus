package com.jzo2o.mall.cart.service.impl;

import cn.hutool.core.text.CharSequenceUtil;
import com.jzo2o.common.utils.DateUtils;
import com.jzo2o.common.utils.UserContext;
import com.jzo2o.mall.cart.model.dto.*;
import com.jzo2o.mall.cart.model.enums.CartTypeEnum;
import com.jzo2o.mall.cart.model.enums.DeliveryMethodEnum;
import com.jzo2o.mall.cart.service.CartService;
import com.jzo2o.mall.cart.service.render.TradeBuilder;
import com.jzo2o.mall.common.enums.ResultCode;
import com.jzo2o.mall.common.exception.ServiceException;
import com.jzo2o.mall.common.model.AuthUser;
import com.jzo2o.mall.common.utils.CurrencyUtil;
import com.jzo2o.mall.member.model.domain.MemberAddress;
import com.jzo2o.mall.member.model.domain.Store;
import com.jzo2o.mall.member.service.MemberAddressService;
import com.jzo2o.mall.member.service.MemberService;
import com.jzo2o.mall.member.service.StoreService;
import com.jzo2o.mall.order.model.dto.ReceiptInputDTO;
import com.jzo2o.mall.product.model.domain.GoodsSku;
import com.jzo2o.mall.common.enums.GoodsAuthEnum;
import com.jzo2o.mall.common.enums.GoodsStatusEnum;
import com.jzo2o.mall.promotion.model.dto.SeckillGoodsDTO;
import com.jzo2o.mall.promotion.model.dto.SeckillTimelineDTO;
import com.jzo2o.mall.promotion.model.enums.PromotionTypeEnum;
import com.jzo2o.mall.product.service.GoodsSkuService;
import com.jzo2o.mall.promotion.model.domain.MemberCoupon;
import com.jzo2o.mall.promotion.model.dto.MemberCouponSearchParams;
import com.jzo2o.mall.promotion.model.enums.MemberCouponStatusEnum;
import com.jzo2o.mall.promotion.model.enums.PromotionsScopeTypeEnum;
import com.jzo2o.mall.promotion.service.MemberCouponService;
import com.jzo2o.mall.promotion.service.PromotionGoodsService;
import com.jzo2o.mall.promotion.service.SeckillApplyService;
import com.jzo2o.mall.search.service.EsGoodsSearchService;
import com.jzo2o.redis.helper.Cache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 购物车业务层实现
 */
@Slf4j
@Service
public class CartServiceImpl implements CartService {

    static String errorMessage = "购物车异常，请稍后重试";

    /**
     * 缓存
     */
    @Autowired
    private Cache<Object> cache;
    /**
     * 会员优惠券
     */
    @Autowired
    private MemberCouponService memberCouponService;

    @Autowired
    private SeckillApplyService seckillApplyService;
    /**
     * 规格商品
     */
    @Autowired
    private GoodsSkuService goodsSkuService;
//    /**
//     * 促销商品
//     */
//    @Autowired
//    private PointsGoodsService pointsGoodsService;
    /**
     * 会员地址
     */
    @Autowired
    private MemberAddressService memberAddressService;
    /**
     * ES商品
     */
    @Autowired
    private EsGoodsSearchService esGoodsSearchService;
//    /**
//     * 砍价
//     */
//    @Autowired
//    private KanjiaActivityService kanjiaActivityService;
//    /**
//     * 交易
//     */
    @Autowired
    private TradeBuilder tradeBuilder;

    @Autowired
    private MemberService memberService;

    @Autowired
    private PromotionGoodsService promotionGoodsService;
//
//    @Autowired
//    private WholesaleService wholesaleService;

    @Autowired
    private StoreService storeService;

//    @Autowired
//    private StoreAddressService storeAddressService;


    //根据商品的更新时间校验商品的有效性(私有方法)
    private boolean checkGoodsValid( GoodsSku dataSku,CartSkuDTO cartSkuDTO) {
        boolean result = dataSku != null &&
                dataSku.getUpdateTime() != null &&
                cartSkuDTO.getGoodsSku() != null &&
                cartSkuDTO.getGoodsSku().getUpdateTime() != null &&
                dataSku.getUpdateTime().isAfter(cartSkuDTO.getGoodsSku().getUpdateTime());
        return result;
   }
    @Override
    public void add(String skuId, Integer num, String cartType, Boolean cover) {
        AuthUser currentUser = UserContext.getCurrentUser();
        if (num <= 0) {
            throw new ServiceException(ResultCode.CART_NUM_ERROR);
        }
        CartTypeEnum cartTypeEnum = getCartType(cartType);
        //判断商品是否为上架状态，否则不能加入购物车
        GoodsSku dataSku = checkGoods(skuId);
        try {
//            Map<String, Object> promotionMap = promotionGoodsService.getCurrentGoodsPromotion(dataSku, cartTypeEnum.name());
            //购物车方式购买需要保存之前的选择，其他方式购买，则直接抹除掉之前的记录
            TradeDTO tradeDTO = null;
            if (cartTypeEnum.equals(CartTypeEnum.CART)) {
                //如果存在，则变更数量不做新增，否则新增一个商品进入集合
                tradeDTO = this.readDTO(cartTypeEnum);
                //购物车中商品
                List<CartSkuDTO> cartSkuDTOS = tradeDTO.getSkuList();
                //根据skuId找到购物车中的商品
                CartSkuDTO cartSkuDTO = cartSkuDTOS.stream().filter(i -> i.getGoodsSku().getId().equals(skuId)).findFirst().orElse(null);
                //购物车中已经存在，更新数量，使用checkGoodsValid方法对比商品的更新时间：确保用户购物车中的商品是最新的状态。商品加入购物车后修改了信息说明购物车中可能已经存在过时的商品信息，需要更新或替换
                if (cartSkuDTO != null && !checkGoodsValid(dataSku, cartSkuDTO)) {
                    //如果覆盖购物车中商品数量
                    if (Boolean.TRUE.equals(cover)) {
//                        cartSkuDTO.setNum(num);
                        //检查并设置购物车商品数量
                        this.checkSetGoodsQuantity(cartSkuDTO, skuId, num);
                    } else {
                        int oldNum = cartSkuDTO.getNum();
                        int newNum = oldNum + num;
                        this.checkSetGoodsQuantity(cartSkuDTO, skuId, newNum);
                    }
//                    cartSkuDTO.setPromotionMap(null);
                    //计算购物车小计
                    cartSkuDTO.rebuildSubTotal();
                } else {
                    //先清理一下无效商品
                    cartSkuDTOS.remove(cartSkuDTO);
                    //购物车中不存在此商品，则新建立一个
                    cartSkuDTO = new CartSkuDTO(dataSku);

                    cartSkuDTO.setCartType(cartTypeEnum);
                    //再设置加入购物车的数量
                    this.checkSetGoodsQuantity(cartSkuDTO, skuId, num);
                    //计算购物车小计
                    cartSkuDTO.rebuildSubTotal();
                    cartSkuDTOS.add(cartSkuDTO);
                }
                //新加入的商品都是选中的
                cartSkuDTO.setChecked(true);
//                cartSkuDTO.setPromotionMap(promotionMap);
            } else if (cartTypeEnum.equals(CartTypeEnum.BUY_NOW)) {//立即购买

                tradeDTO = new TradeDTO(cartTypeEnum);
                tradeDTO.setMemberId(currentUser.getIdString());
                tradeDTO.setMemberName(currentUser.getUsername());
                List<CartSkuDTO> cartSkuDTOS = tradeDTO.getSkuList();

                //购物车中不存在此商品，则新建立一个
                CartSkuDTO cartSkuDTO = new CartSkuDTO(dataSku);
                cartSkuDTO.setCartType(cartTypeEnum);
                //检测购物车数据
                checkCart(cartTypeEnum, cartSkuDTO, skuId, num);
                //计算购物车小计
                cartSkuDTO.rebuildSubTotal();
                cartSkuDTOS.add(cartSkuDTO);
            }
            //当购物车商品发生变更时，取消已选择的优惠券
            removeCoupon(tradeDTO);
            //购物车信息写入缓存
            this.resetTradeDTO(tradeDTO);
        } catch (ServiceException serviceException) {
            throw serviceException;
        } catch (Exception e) {
            log.error("购物车渲染异常", e);
            throw new ServiceException(errorMessage);
        }
    }
    @Override
    public void seckill(String skuId, Integer num) {
        if (num <= 0) {
            throw new ServiceException(ResultCode.CART_NUM_ERROR);
        }
        //购物车类型为秒杀
        CartTypeEnum cartTypeEnum = CartTypeEnum.SECKILL;
        //取出当前登录用户
        AuthUser currentUser = UserContext.getCurrentUser();
        //根据当前日期获取秒杀活动信息
        String now = DateUtils.format(LocalDateTime.now(), "yyyy-MM-dd");
        //从缓存中获取当天的秒杀活动列表
        List<SeckillTimelineDTO> seckillTimeline = seckillApplyService.getSeckillTimeline(now);
        //根据skuid从seckillTimeline取出秒杀活动信息
        SeckillTimelineDTO seckillTimelineDTO = seckillTimeline.stream().filter(i -> i.getSeckillGoodsList().stream().anyMatch(j -> j.getSkuId().equals(skuId))).findFirst().orElse(null);
        if(seckillTimelineDTO == null){
            throw new ServiceException(ResultCode.GOODS_NOT_EXIST);
        }
        //从seckillTimelineDTO中取出秒杀商品
        SeckillGoodsDTO seckillGoodsDTO = seckillTimelineDTO.getSeckillGoodsList().stream().filter(i -> i.getSkuId().equals(skuId)).findFirst().orElse(null);
        if(seckillGoodsDTO == null || seckillGoodsDTO.getPromotionGoods() == null){
            throw new ServiceException(ResultCode.GOODS_NOT_EXIST);
        }
        //活动id
        String promotionId = seckillGoodsDTO.getPromotionGoods().getPromotionId();


        //判断商品是否为上架状态，否则不能加入购物车
        GoodsSku dataSku = checkGoods(skuId);
        try {
            //获取当前商品的促销活动信息(优惠券)
//            Map<String, Object> promotionMap = promotionGoodsService.getCurrentGoodsPromotion(dataSku, cartTypeEnum.name());
//            Map<String, Object> promotionMap = null;
            //购物车方式购买需要保存之前的选择，其他方式购买，则直接抹除掉之前的记录
            TradeDTO tradeDTO = new TradeDTO(cartTypeEnum);

            tradeDTO.setMemberId(currentUser.getIdString());
            tradeDTO.setMemberName(currentUser.getUsername());
            List<CartSkuDTO> cartSkuVOS = tradeDTO.getSkuList();

            //购物车中不存在此商品，则新建立一个
            CartSkuDTO cartSkuDTO = new CartSkuDTO(dataSku);
            cartSkuDTO.setCartType(cartTypeEnum);
            //检测购物车数据
            checkSeckillGoods(promotionId,cartSkuDTO, skuId, num);
            //设置商品促销价格
            cartSkuDTO.getGoodsSku().setPromotionPrice(seckillGoodsDTO.getPrice());
            cartSkuDTO.getGoodsSku().setPrice(seckillGoodsDTO.getPrice());
            cartSkuDTO.setPurchasePrice(seckillGoodsDTO.getPrice());
            //计算购物车小计
            cartSkuDTO.rebuildSubTotal();
            cartSkuVOS.add(cartSkuDTO);

            //当购物车商品发生变更时，取消已选择的优惠券
            removeCoupon(tradeDTO);
            //购物车信息写入缓存
            this.resetTradeDTO(tradeDTO);
        } catch (ServiceException serviceException) {
            throw serviceException;
        } catch (Exception e) {
            log.error("购物车渲染异常", e);
            throw new ServiceException(errorMessage);
        }
    }

    /**
     * 读取当前会员购物原始数据key
     *
     * @param cartTypeEnum 获取方式
     * @return 当前会员购物原始数据key
     */
    private String getOriginKey(CartTypeEnum cartTypeEnum) {

        //缓存key，默认使用购物车
        if (cartTypeEnum != null) {
            AuthUser currentUser = UserContext.getCurrentUser();
            return cartTypeEnum.getPrefix() + currentUser.getIdString();
        }
        throw new ServiceException(ResultCode.ERROR);
    }

    @Override
    public TradeDTO readDTO(CartTypeEnum checkedWay) {
        TradeDTO tradeDTO = (TradeDTO) cache.get(this.getOriginKey(checkedWay));
        if (tradeDTO == null) {
            tradeDTO = new TradeDTO(checkedWay);
            AuthUser currentUser = UserContext.getCurrentUser();
            tradeDTO.setMemberId(currentUser.getIdString());
            tradeDTO.setMemberName(currentUser.getUsername());
        }
        if (tradeDTO.getMemberAddress() == null) {
            tradeDTO.setMemberAddress(new MemberAddress());
            tradeDTO.setMemberAddress(this.memberAddressService.getDefaultMemberAddress());
        }
        return tradeDTO;
    }

    @Override
    public void checked(String skuId, boolean checked) {
        TradeDTO tradeDTO = this.readDTO(CartTypeEnum.CART);
//        removeCoupon(tradeDTO);
        List<CartSkuDTO> cartSkuDTOS = tradeDTO.getSkuList();
        for (CartSkuDTO cartSkuDTO : cartSkuDTOS) {
            if (cartSkuDTO.getGoodsSku().getId().equals(skuId)) {
                cartSkuDTO.setChecked(checked);
            }
        }

        this.resetTradeDTO(tradeDTO);
    }

    @Override
    public void checkedStore(String storeId, boolean checked) {
        TradeDTO tradeDTO = this.readDTO(CartTypeEnum.CART);

//        removeCoupon(tradeDTO);

        List<CartSkuDTO> cartSkuDTOS = tradeDTO.getSkuList();
        for (CartSkuDTO cartSkuDTO : cartSkuDTOS) {
            if (cartSkuDTO.getStoreId().equals(storeId)) {
                cartSkuDTO.setChecked(checked);
            }
        }

        resetTradeDTO(tradeDTO);
    }

    @Override
    public void checkedAll(boolean checked) {
        TradeDTO tradeDTO = this.readDTO(CartTypeEnum.CART);

//        removeCoupon(tradeDTO);

        List<CartSkuDTO> cartSkuDTOS = tradeDTO.getSkuList();
        for (CartSkuDTO cartSkuDTO : cartSkuDTOS) {
            cartSkuDTO.setChecked(checked);
        }

        resetTradeDTO(tradeDTO);
    }

    /**
     * 当购物车商品发生变更时，取消已选择当优惠券
     *
     * @param tradeDTO
     */
    private void removeCoupon(TradeDTO tradeDTO) {
        tradeDTO.setPlatformCoupon(null);
        tradeDTO.setStoreCoupons(new HashMap<>());
    }

    @Override
    public void delete(String[] skuIds) {
        TradeDTO tradeDTO = this.readDTO(CartTypeEnum.CART);
        List<CartSkuDTO> cartSkuDTOS = tradeDTO.getSkuList();
        List<CartSkuDTO> deleteDTOS = new ArrayList<>();
        for (CartSkuDTO cartSkuDTO : cartSkuDTOS) {
            for (String skuId : skuIds) {
                if (cartSkuDTO.getGoodsSku().getId().equals(skuId)) {
                    deleteDTOS.add(cartSkuDTO);
                }
            }
        }
        cartSkuDTOS.removeAll(deleteDTOS);
        resetTradeDTO(tradeDTO);
    }

    @Override
    public void clean() {
        cache.remove(this.getOriginKey(CartTypeEnum.CART));
    }

    public void cleanChecked(TradeDTO tradeDTO) {
        List<CartSkuDTO> cartSkuDTOS = tradeDTO.getSkuList();
        List<CartSkuDTO> deleteDTOS = new ArrayList<>();
        for (CartSkuDTO cartSkuDTO : cartSkuDTOS) {
            if (Boolean.TRUE.equals(cartSkuDTO.getChecked())) {
                deleteDTOS.add(cartSkuDTO);
            }
        }
        cartSkuDTOS.removeAll(deleteDTOS);
        //清除选择的优惠券
//        tradeDTO.setPlatformCoupon(null);
//        tradeDTO.setStoreCoupons(null);
        //清除添加过的备注
        tradeDTO.setStoreRemark(null);

        resetTradeDTO(tradeDTO);
    }

    @Override
    public void resetTradeDTO(TradeDTO tradeDTO) {
        cache.put(this.getOriginKey(tradeDTO.getCartTypeEnum()), tradeDTO);
    }

    @Override
    public TradeDTO getCheckedTradeDTO(CartTypeEnum way) {
        return tradeBuilder.buildChecked(way);
    }

//    /**
//     * 获取可使用的优惠券数量
//     *
//     * @param checkedWay 购物车购买：CART/立即购买：BUY_NOW
//     * @return 可使用的优惠券数量
//     */
//    @Override
//    public Long getCanUseCoupon(CartTypeEnum checkedWay) {
//        TradeDTO tradeDTO = this.readDTO(checkedWay);
//        long count = 0L;
//        double totalPrice = tradeDTO.getSkuList().stream().mapToDouble(i -> i.getPurchasePrice() * i.getNum()).sum();
//        if (tradeDTO.getSkuList() != null && !tradeDTO.getSkuList().isEmpty()) {
//            List<String> ids = tradeDTO.getSkuList().stream().filter(i -> Boolean.TRUE.equals(i.getChecked())).map(i -> i.getGoodsSku().getId()).collect(Collectors.toList());
//
//            List<EsGoodsIndex> esGoodsList = esGoodsSearchService.getEsGoodsBySkuIds(ids, null);
//            for (EsGoodsIndex esGoodsIndex : esGoodsList) {
//                if (esGoodsIndex != null && esGoodsIndex.getPromotionMap() != null && !esGoodsIndex.getPromotionMap().isEmpty()) {
//                    List<String> couponIds = esGoodsIndex.getPromotionMap().keySet().stream().filter(i -> i.contains(PromotionTypeEnum.COUPON.name())).map(i -> i.substring(i.lastIndexOf("-") + 1)).collect(Collectors.toList());
//                    if (!couponIds.isEmpty()) {
//                        List<MemberCoupon> currentGoodsCanUse = memberCouponService.getCurrentGoodsCanUse(tradeDTO.getMemberId(), couponIds, totalPrice);
//                        count = currentGoodsCanUse.size();
//                    }
//                }
//            }
//
//            List<String> storeIds = new ArrayList<>();
//            for (CartSkuVO cartSkuVO : tradeDTO.getSkuList()) {
//                if (!storeIds.contains(cartSkuVO.getStoreId())) {
//                    storeIds.add(cartSkuVO.getStoreId());
//                }
//            }
//
//            //获取可操作的优惠券集合
//            List<MemberCoupon> allScopeMemberCoupon = memberCouponService.getAllScopeMemberCoupon(tradeDTO.getMemberId(), storeIds);
//            if (allScopeMemberCoupon != null && !allScopeMemberCoupon.isEmpty()) {
//                //过滤满足消费门槛
//                count += allScopeMemberCoupon.stream().filter(i -> i.getConsumeThreshold() <= totalPrice).count();
//            }
//        }
//        return count;
//    }

    @Override
    public TradeDTO getAllTradeDTO() {
        TradeDTO tradeDTO = tradeBuilder.buildCart(CartTypeEnum.CART);
        return tradeDTO;
    }

    /**
     * 校验商品有效性，判定失效和库存，促销活动价格
     *
     * @param skuId 商品skuId
     */
    private GoodsSku checkGoods(String skuId) {
        GoodsSku dataSku = this.goodsSkuService.getGoodsSkuByIdFromCache(skuId);
        if (dataSku == null) {
            throw new ServiceException(ResultCode.GOODS_NOT_EXIST);
        }
        if (!GoodsAuthEnum.PASS.name().equals(dataSku.getAuthFlag()) || !GoodsStatusEnum.UPPER.name().equals(dataSku.getMarketEnable())) {
            throw new ServiceException(ResultCode.GOODS_NOT_EXIST);
        }
        return dataSku;
    }

    /**
     * 检查并设置购物车商品数量
     *
     * @param cartSkuDTO 购物车商品对象
     * @param skuId     商品id
     * @param num       购买数量
     */
    private void checkSetGoodsQuantity(CartSkuDTO cartSkuDTO, String skuId, Integer num) {
        //库存
        Integer enableStock = goodsSkuService.getStock(skuId);

        //如果sku的可用库存小于等于0或者小于用户购买的数量，则不允许购买
        if (enableStock <= 0 || enableStock < num) {
            throw new ServiceException(ResultCode.GOODS_SKU_QUANTITY_NOT_ENOUGH);
        }

        if (enableStock <= num) {
            cartSkuDTO.setNum(enableStock);
        } else {
            cartSkuDTO.setNum(num);
        }
//        //销售数量限制
//        if (cartSkuDTO.getGoodsSku() != null &&  cartSkuDTO.getNum() > 99) {
//            cartSkuDTO.setNum(99);
//        }
    }

    @Override
    public void shippingAddress(String shippingAddressId, String way) {

        //默认购物车
        CartTypeEnum cartTypeEnum = CartTypeEnum.CART;
        if (CharSequenceUtil.isNotEmpty(way)) {
            cartTypeEnum = CartTypeEnum.valueOf(way);
        }

        TradeDTO tradeDTO = this.readDTO(cartTypeEnum);
        MemberAddress memberAddress = memberAddressService.getById(shippingAddressId);
        tradeDTO.setMemberAddress(memberAddress);
        this.resetTradeDTO(tradeDTO);
    }

//    @Override
//    public void shippingSelfAddress(String shopAddressId, String way) {
//        //默认购物车
//        CartTypeEnum cartTypeEnum = CartTypeEnum.CART;
//        if (CharSequenceUtil.isNotEmpty(way)) {
//            cartTypeEnum = CartTypeEnum.valueOf(way);
//        }
//
//        TradeDTO tradeDTO = this.readDTO(cartTypeEnum);
//        StoreAddress storeAddress = storeAddressService.getById(shopAddressId);
//        tradeDTO.setStoreAddress(storeAddress);
//        this.resetTradeDTO(tradeDTO);
//    }

    /**
     * 选择发票
     *
     * @param receipt 发票信息
     * @param way       购物车类型
     */
    @Override
    public void shippingReceipt(ReceiptInputDTO receipt, String way) {
        CartTypeEnum cartTypeEnum = CartTypeEnum.CART;
        if (CharSequenceUtil.isNotEmpty(way)) {
            cartTypeEnum = CartTypeEnum.valueOf(way);
        }
        TradeDTO tradeDTO = this.readDTO(cartTypeEnum);
        tradeDTO.setNeedReceipt(true);
        tradeDTO.setReceipt(receipt);
        this.resetTradeDTO(tradeDTO);
    }

    /**
     * 选择配送方式
     *
     * @param deliveryMethod 配送方式
     * @param way            购物车类型
     */
    @Override
    public void shippingMethod(String deliveryMethod, String way) {
        CartTypeEnum cartTypeEnum = CartTypeEnum.CART;
        if (CharSequenceUtil.isNotEmpty(way)) {
            cartTypeEnum = CartTypeEnum.valueOf(way);
        }
        TradeDTO tradeDTO = this.readDTO(cartTypeEnum);
        for (CartSkuDTO cartSkuDTO : tradeDTO.getSkuList()) {
            cartSkuDTO.setDeliveryMethod(DeliveryMethodEnum.valueOf(deliveryMethod).name());
        }
        this.resetTradeDTO(tradeDTO);
    }

    /**
     * 获取购物车商品数量
     *
     * @param checked 是否选择
     * @return 购物车商品数量
     */
    @Override
    public Long getCartNum(Boolean checked) {
        //构建购物车
        TradeDTO tradeDTO = this.getAllTradeDTO();
        //过滤sku列表
        List<CartSkuDTO> collect = tradeDTO.getSkuList().stream().filter(i -> Boolean.FALSE.equals(i.getInvalid())).collect(Collectors.toList());
        long count = 0L;
        if (!tradeDTO.getSkuList().isEmpty()) {
            if (checked != null) {
                count = collect.stream().filter(i -> i.getChecked().equals(checked)).count();
            } else {
                count = collect.size();
            }
        }
        return count;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void selectCoupon(String couponId, String way, boolean use) {
        AuthUser currentUser = UserContext.getCurrentUser();
        //获取购物车，然后重新写入优惠券
        CartTypeEnum cartTypeEnum = getCartType(way);

        //积分商品不允许使用优惠券
//        if (cartTypeEnum.equals(CartTypeEnum.POINTS)) {
//            throw new ServiceException(ResultCode.SPECIAL_CANT_USE);
//        }

        TradeDTO tradeDTO = this.readDTO(cartTypeEnum);

        MemberCouponSearchParams searchParams = new MemberCouponSearchParams();
        searchParams.setMemberCouponStatus(MemberCouponStatusEnum.NEW.name());
        searchParams.setMemberId(currentUser.getIdString());
        searchParams.setId(couponId);
        MemberCoupon memberCoupon = memberCouponService.getMemberCoupon(searchParams);
        if (memberCoupon == null) {
            throw new ServiceException(ResultCode.COUPON_EXPIRED);
        }
        //使用优惠券 与否
        if (use) {
            this.useCoupon(tradeDTO, memberCoupon, cartTypeEnum);
        } else {
            if (Boolean.TRUE.equals(memberCoupon.getPlatformFlag())) {
                tradeDTO.setPlatformCoupon(null);
            } else {
                tradeDTO.getStoreCoupons().remove(memberCoupon.getStoreId());
            }
        }
        this.resetTradeDTO(tradeDTO);
    }



    @Override
    public List<String> shippingMethodList(String way) {
        List<String> list = new ArrayList<String>();
        list.add(DeliveryMethodEnum.LOGISTICS.name());
        TradeDTO tradeDTO = this.getCheckedTradeDTO(CartTypeEnum.valueOf(way));
        if (tradeDTO.getCartList().size() == 1) {
            for (CartDTO cartDTO : tradeDTO.getCartList()) {
                Store store = storeService.getById(cartDTO.getStoreId());
                if (store.getSelfPickFlag() != null && store.getSelfPickFlag()) {
                    list.add(DeliveryMethodEnum.SELF_PICK_UP.name());
                }
            }
        }
        return list;
    }


    /**
     * 获取购物车类型
     *
     * @param way
     * @return
     */
    private CartTypeEnum getCartType(String way) {
        //默认购物车
        CartTypeEnum cartTypeEnum = CartTypeEnum.CART;
        if (CharSequenceUtil.isNotEmpty(way)) {
            try {
                cartTypeEnum = CartTypeEnum.valueOf(way);
            } catch (IllegalArgumentException e) {
                log.error("获取购物车类型出现错误：", e);
            }
        }
        return cartTypeEnum;
    }

    /**
     * 使用优惠券判定
     *
     * @param tradeDTO     交易对象
     * @param memberCoupon 会员优惠券
     * @param cartTypeEnum 购物车
     */
    private void useCoupon(TradeDTO tradeDTO, MemberCoupon memberCoupon, CartTypeEnum cartTypeEnum) {

        //截取符合优惠券的商品
        List<CartSkuDTO> cartSkuDTOS = checkCoupon(memberCoupon, tradeDTO);

        //定义使用优惠券的信息商品信息
        Map<String, Double> skuPriceMap = new HashMap<>(1);


        //购物车价格
        double cartPrice = 0d;

        //循环符合优惠券的商品
        for (CartSkuDTO cartSkuDTO : cartSkuDTOS) {
            if (Boolean.FALSE.equals(cartSkuDTO.getChecked())) {
                continue;
            }
            cartPrice = CurrencyUtil.add(cartPrice, CurrencyUtil.mul(cartSkuDTO.getGoodsSku().getPrice(), cartSkuDTO.getNum()));
            skuPriceMap.put(cartSkuDTO.getGoodsSku().getId(), CurrencyUtil.mul(cartSkuDTO.getGoodsSku().getPrice(), cartSkuDTO.getNum()));
//            //有促销金额则用促销金额，否则用商品原价
//            if (cartSkuDTO.getPromotionMap() != null && !cartSkuDTO.getPromotionMap().isEmpty()) {
//                if (cartSkuDTO.getPromotionMap().keySet().stream().anyMatch(i -> i.contains(PromotionTypeEnum.PINTUAN.name()) || i.contains(PromotionTypeEnum.SECKILL.name()))) {
//                    cartPrice = CurrencyUtil.add(cartPrice, CurrencyUtil.mul(cartSkuDTO.getPurchasePrice(), cartSkuDTO.getNum()));
//                    skuPriceMap.put(cartSkuDTO.getGoodsSku().getId(), CurrencyUtil.mul(cartSkuDTO.getPurchasePrice(), cartSkuDTO.getNum()));
//                } else {
//                    cartPrice = CurrencyUtil.add(cartPrice, CurrencyUtil.mul(cartSkuDTO.getGoodsSku().getPrice(), cartSkuDTO.getNum()));
//                    skuPriceMap.put(cartSkuDTO.getGoodsSku().getId(), CurrencyUtil.mul(cartSkuDTO.getGoodsSku().getPrice(), cartSkuDTO.getNum()));
//                }
//            } else {
//                cartPrice = CurrencyUtil.add(cartPrice, CurrencyUtil.mul(cartSkuDTO.getGoodsSku().getPrice(), cartSkuDTO.getNum()));
//                skuPriceMap.put(cartSkuDTO.getGoodsSku().getId(), CurrencyUtil.mul(cartSkuDTO.getGoodsSku().getPrice(), cartSkuDTO.getNum()));
//            }
        }


        //如果购物车金额大于消费门槛则使用
        if (cartPrice >= memberCoupon.getConsumeThreshold()) {
            //如果是平台优惠券
            if (Boolean.TRUE.equals(memberCoupon.getPlatformFlag())) {
                //平台券只允许使用一个优惠券
                tradeDTO.setPlatformCoupon(new MemberCouponDTO(skuPriceMap, memberCoupon));
            } else {
                //同一个店铺只允许一个优惠券
                tradeDTO.getStoreCoupons().put(memberCoupon.getStoreId(), new MemberCouponDTO(skuPriceMap, memberCoupon));
            }
        }

    }

    /**
     * 获取可以使用优惠券的商品信息
     *
     * @param memberCoupon 用于计算优惠券结算详情
     * @param tradeDTO     购物车信息
     * @return 是否可以使用优惠券
     */
    private List<CartSkuDTO> checkCoupon(MemberCoupon memberCoupon, TradeDTO tradeDTO) {
        List<CartSkuDTO> cartSkuDTOS;
        //如果是店铺优惠券，判定的内容
        if (Boolean.FALSE.equals(memberCoupon.getPlatformFlag())) {
            cartSkuDTOS = tradeDTO.getSkuList().stream().filter(i -> i.getStoreId().equals(memberCoupon.getStoreId())).collect(Collectors.toList());
        }
        //否则为平台优惠券，筛选商品为全部商品
        else {
            cartSkuDTOS = tradeDTO.getSkuList();
        }

        //当初购物车商品中是否存在符合优惠券条件的商品sku
        if (memberCoupon.getScopeType().equals(PromotionsScopeTypeEnum.ALL.name())) {
            return cartSkuDTOS;
        } else if (memberCoupon.getScopeType().equals(PromotionsScopeTypeEnum.PORTION_GOODS_CATEGORY.name())) {
            //分类路径是否包含
            return cartSkuDTOS.stream().filter(i -> CharSequenceUtil.contains(memberCoupon.getScopeId(), i.getGoodsSku().getCategoryPath())).collect(Collectors.toList());
        } else if (memberCoupon.getScopeType().equals(PromotionsScopeTypeEnum.PORTION_GOODS.name())) {
            //范围关联ID是否包含
            return cartSkuDTOS.stream().filter(i -> CharSequenceUtil.contains(memberCoupon.getScopeId(), i.getGoodsSku().getId())).collect(Collectors.toList());
        } else if (memberCoupon.getScopeType().equals(PromotionsScopeTypeEnum.PORTION_SHOP_CATEGORY.name())) {
            //店铺分类路径是否包含
            return cartSkuDTOS.stream().filter(i -> CharSequenceUtil.contains(memberCoupon.getScopeId(), i.getGoodsSku().getStoreCategoryPath())).collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    /**
     * 检测购物车
     *
     * @param cartTypeEnum 购物车枚举
     * @param cartSkuDTO    SKUVO
     * @param skuId        SkuId
     * @param num          数量
     */
    private void checkCart(CartTypeEnum cartTypeEnum, CartSkuDTO cartSkuDTO, String skuId, Integer num) {

        this.checkSetGoodsQuantity(cartSkuDTO, skuId, num);
//        //拼团判定
//        if (cartTypeEnum.equals(CartTypeEnum.PINTUAN)) {
//            //砍价判定
//            checkPintuan(cartSkuDTO);
//        } else if (cartTypeEnum.equals(CartTypeEnum.KANJIA)) {
//            //检测购物车的数量
//            checkKanjia(cartSkuDTO);
//        } else if (cartTypeEnum.equals(CartTypeEnum.POINTS)) {
//            //检测购物车的数量
//            checkPoint(cartSkuDTO);
//        }
    }
    /**
     * 检测秒杀商品
     *
     * @param promotionId 活动id
     * @param cartSkuDTO    SKUVO
     * @param skuId        SkuId
     * @param num          数量
     */
    private void checkSeckillGoods(String promotionId, CartSkuDTO cartSkuDTO, String skuId, Integer num) {
        //检查sku库存
        this.checkSetGoodsQuantity(cartSkuDTO, skuId, num);
        //如果是秒杀商品需要检查促销库存
        String promotionGoodsStockCacheKey = PromotionGoodsService.getPromotionGoodsStockCacheKey(PromotionTypeEnum.SECKILL, promotionId, cartSkuDTO.getGoodsSku().getId());
        Integer quantity = (Integer) cache.get(promotionGoodsStockCacheKey);
        //如果sku的可用库存小于等于0或者小于用户购买的数量，则不允许购买
        if (quantity == null || quantity <= 0 || quantity < num) {
            throw new ServiceException(ResultCode.GOODS_SKU_QUANTITY_NOT_ENOUGH);
        }

        if (quantity <= num) {
            cartSkuDTO.setNum(quantity);
        } else {
            cartSkuDTO.setNum(num);
        }
    }


//    private void checkGoodsSaleModel(GoodsSku dataSku, List<CartSkuVO> cartSkuVOS) {
//        if (dataSku.getSalesModel().equals(GoodsSalesModeEnum.WHOLESALE.name())) {
//            int numSum = 0;
//            List<CartSkuVO> sameGoodsIdSkuList = cartSkuVOS.stream().filter(i -> i.getGoodsSku().getGoodsId().equals(dataSku.getGoodsId())).collect(Collectors.toList());
//            if (CollUtil.isNotEmpty(sameGoodsIdSkuList)) {
//                numSum += sameGoodsIdSkuList.stream().mapToInt(CartSkuVO::getNum).sum();
//            }
//            Wholesale match = wholesaleService.match(dataSku.getGoodsId(), numSum);
//            if (match != null) {
//                sameGoodsIdSkuList.forEach(i -> {
//                    i.setPurchasePrice(match.getPrice());
//                    i.setSubTotal(CurrencyUtil.mul(i.getPurchasePrice(), i.getNum()));
//                });
//            }
//        }
//    }

//    /**
//     * 校验拼团信息
//     *
//     * @param cartSkuVO 购物车信息
//     */
//    private void checkPintuan(CartSkuVO cartSkuVO) {
//        //拼团活动，需要对限购数量进行判定
//        //获取拼团信息
//        if (cartSkuVO.getPromotionMap() != null && !cartSkuVO.getPromotionMap().isEmpty()) {
//            Optional<Map.Entry<String, Object>> pintuanPromotions = cartSkuVO.getPromotionMap().entrySet().stream().filter(i -> i.getKey().contains(PromotionTypeEnum.PINTUAN.name())).findFirst();
//            if (pintuanPromotions.isPresent()) {
//                JSONObject promotionsObj = JSONUtil.parseObj(pintuanPromotions.get().getValue());
//                //写入拼团信息
//                cartSkuVO.setPintuanId(promotionsObj.get("id").toString());
//                //检测拼团限购数量
//                Integer limitNum = promotionsObj.get("limitNum", Integer.class);
//                if (limitNum != 0 && cartSkuVO.getNum() > limitNum) {
//                    throw new ServiceException(ResultCode.CART_PINTUAN_LIMIT_ERROR);
//                }
//            }
//        }
//    }

//    /**
//     * 校验砍价信息
//     *
//     * @param cartSkuVO 购物车信息
//     */
//    private void checkKanjia(CartSkuVO cartSkuVO) {
//        if (cartSkuVO.getPromotionMap() != null && !cartSkuVO.getPromotionMap().isEmpty()) {
//            Optional<Map.Entry<String, Object>> kanjiaPromotions = cartSkuVO.getPromotionMap().entrySet().stream().filter(i -> i.getKey().contains(PromotionTypeEnum.KANJIA.name())).findFirst();
//            if (kanjiaPromotions.isPresent()) {
//                JSONObject promotionsObj = JSONUtil.parseObj(kanjiaPromotions.get().getValue());
//                //查找当前会员的砍价商品活动
//                KanjiaActivitySearchParams kanjiaActivitySearchParams = new KanjiaActivitySearchParams();
//                kanjiaActivitySearchParams.setKanjiaActivityGoodsId(promotionsObj.get("id", String.class));
//                kanjiaActivitySearchParams.setMemberId(UserContext.getCurrentUser().getId());
//                kanjiaActivitySearchParams.setStatus(KanJiaStatusEnum.SUCCESS.name());
//                KanjiaActivity kanjiaActivity = kanjiaActivityService.getKanjiaActivity(kanjiaActivitySearchParams);
//
//                //校验砍价活动是否满足条件
//                //判断发起砍价活动
//                if (kanjiaActivity == null) {
//                    throw new ServiceException(ResultCode.KANJIA_ACTIVITY_NOT_FOUND_ERROR);
//                    //判断砍价活动是否已满足条件
//                } else if (!KanJiaStatusEnum.SUCCESS.name().equals(kanjiaActivity.getStatus())) {
//                    cartSkuVO.setKanjiaId(kanjiaActivity.getId());
//                    cartSkuVO.setPurchasePrice(0D);
//                    throw new ServiceException(ResultCode.KANJIA_ACTIVITY_NOT_PASS_ERROR);
//                }
//                //砍价商品默认一件货物
//                cartSkuVO.setKanjiaId(kanjiaActivity.getId());
//                cartSkuVO.setNum(1);
//            }
//        }
//    }

//    /**
//     * 校验积分商品信息
//     *
//     * @param cartSkuVO 购物车信息
//     */
//    private void checkPoint(CartSkuVO cartSkuVO) {
//
//        PointsGoodsVO pointsGoodsVO = pointsGoodsService.getPointsGoodsDetailBySkuId(cartSkuVO.getGoodsSku().getId());
//
//        if (pointsGoodsVO != null) {
//            Member userInfo = memberService.getUserInfo();
//            if (userInfo.getPoint() < pointsGoodsVO.getPoints()) {
//                throw new ServiceException(ResultCode.POINT_NOT_ENOUGH);
//            }
//            if (pointsGoodsVO.getActiveStock() < 1) {
//                throw new ServiceException(ResultCode.POINT_GOODS_ACTIVE_STOCK_INSUFFICIENT);
//            }
//            cartSkuVO.setPoint(pointsGoodsVO.getPoints());
//            cartSkuVO.setPurchasePrice(0D);
//            cartSkuVO.setPointsId(pointsGoodsVO.getId());
//        }
//    }
}
