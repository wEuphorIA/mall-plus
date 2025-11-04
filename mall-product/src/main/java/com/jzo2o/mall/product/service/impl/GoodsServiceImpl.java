package com.jzo2o.mall.product.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jzo2o.common.utils.UserContext;
import com.jzo2o.mall.common.cache.CachePrefix;
import com.jzo2o.mall.common.constants.MallMqConstants;
import com.jzo2o.mall.common.enums.ResultCode;
import com.jzo2o.mall.common.enums.UserEnums;
import com.jzo2o.mall.common.exception.ServiceException;
import com.jzo2o.mall.common.model.AuthUser;
import com.jzo2o.mall.common.model.message.ProductStatusMessage;
import com.jzo2o.mall.member.model.domain.FreightTemplate;
import com.jzo2o.mall.member.model.dto.StoreDTO;
import com.jzo2o.mall.member.service.FreightTemplateService;
import com.jzo2o.mall.member.service.StoreService;
import com.jzo2o.mall.product.mapper.GoodsMapper;
import com.jzo2o.mall.product.model.domain.Category;
import com.jzo2o.mall.product.model.domain.Goods;
import com.jzo2o.mall.product.model.domain.GoodsGallery;
import com.jzo2o.mall.product.model.domain.GoodsSku;
import com.jzo2o.mall.product.model.dto.*;
import com.jzo2o.mall.common.enums.GoodsAuthEnum;
import com.jzo2o.mall.common.enums.GoodsStatusEnum;
import com.jzo2o.mall.product.service.CategoryService;
import com.jzo2o.mall.product.service.GoodsGalleryService;
import com.jzo2o.mall.product.service.GoodsService;
import com.jzo2o.mall.product.service.GoodsSkuService;
import com.jzo2o.mall.search.service.EsGoodsIndexService;
import com.jzo2o.mall.system.enums.SettingEnum;
import com.jzo2o.mall.system.model.domain.Setting;
import com.jzo2o.mall.system.service.SettingService;
import com.jzo2o.mysql.utils.PageUtils;
import com.jzo2o.rabbitmq.client.RabbitClient;
import com.jzo2o.redis.helper.Cache;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 商品业务层实现
 */
@Service
public class GoodsServiceImpl extends ServiceImpl<GoodsMapper, Goods> implements GoodsService {

    /**
     * 分类
     */
    @Autowired
    private CategoryService categoryService;
    /**
     * 设置
     */
    @Autowired
    private SettingService settingService;
    /**
     * 商品相册
     */
    @Autowired
    private GoodsGalleryService goodsGalleryService;
    /**
     * 商品规格
     */
    @Autowired
    private GoodsSkuService goodsSkuService;
    /**
     * 店铺详情
     */
    @Autowired
    private StoreService storeService;

    @Autowired
    private RabbitClient rabbitClient;

    @Autowired
    private EsGoodsIndexService esGoodsIndexService;
//    /**
//     * 会员评价
//     */
//    @Autowired
//    private MemberEvaluationService memberEvaluationService;
//    /**
//     * rocketMq
//     */
//    @Autowired
//    private RocketMQTemplate rocketMQTemplate;
//    /**
//     * rocketMq配置
//     */
//    @Autowired
//    private RocketmqCustomProperties rocketmqCustomProperties;


    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;
    @Autowired
    private FreightTemplateService freightTemplateService;
//
//    @Autowired
//    private WholesaleService wholesaleService;
//
    @Autowired
    private Cache<GoodsDTO> cache;

    @Override
    public List<Goods> getByBrandIds(List<String> brandIds) {
        LambdaQueryWrapper<Goods> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.in(Goods::getBrandId, brandIds);
        return list(lambdaQueryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void underStoreGoods(String storeId) {
        //获取商品ID列表
        List<String> list = this.baseMapper.getGoodsIdByStoreId(storeId);
        //下架店铺下的商品
        this.updateGoodsMarketAbleByStoreId(storeId, GoodsStatusEnum.DOWN, "店铺关闭");

//        applicationEventPublisher.publishEvent(new TransactionCommitSendMQEvent("下架商品",
//                rocketmqCustomProperties.getGoodsTopic(), GoodsTagsEnum.DOWN.name(), JSONUtil.toJsonStr(list)));

    }

    /**
     * 更新商品参数
     *
     * @param goodsId 商品id
     * @param params  商品参数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateGoodsParams(String goodsId, String params) {
        LambdaUpdateWrapper<Goods> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Goods::getId, goodsId);
        updateWrapper.set(Goods::getParams, params);
        this.update(updateWrapper);
    }

    @Override
    public final long getGoodsCountByCategory(String categoryId) {
        QueryWrapper<Goods> queryWrapper = Wrappers.query();
        queryWrapper.like("category_path", categoryId);
        queryWrapper.eq("delete_flag", false);
        return this.count(queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addGoods(GoodsOperationDTO goodsOperationDTO) {
        Goods goods = new Goods(goodsOperationDTO);
        //检查商品
        this.checkGoods(goods);
        //向goods加入图片
        if (goodsOperationDTO.getGoodsGalleryList()!=null && goodsOperationDTO.getGoodsGalleryList().size() > 0) {
            this.setGoodsGalleryParam(goodsOperationDTO.getGoodsGalleryList().get(0), goods);
        }
        //添加商品参数
        if (goodsOperationDTO.getGoodsParamsDTOList() != null && !goodsOperationDTO.getGoodsParamsDTOList().isEmpty()) {
            //给商品参数填充值
            goods.setParams(JSONUtil.toJsonStr(goodsOperationDTO.getGoodsParamsDTOList()));
        }
        //添加商品
        this.save(goods);
        //添加商品sku信息
        this.goodsSkuService.add(goods, goodsOperationDTO);
        //添加相册
        if (goodsOperationDTO.getGoodsGalleryList() != null && !goodsOperationDTO.getGoodsGalleryList().isEmpty()) {
            this.goodsGalleryService.add(goodsOperationDTO.getGoodsGalleryList(), goods.getId());
        }
        //添加es索引
//        this.generateEs(goods);
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void editGoods(GoodsOperationDTO goodsOperationDTO, String goodsId) {
        Goods goods = new Goods(goodsOperationDTO);
        goods.setId(goodsId);
        //检查商品信息
        this.checkGoods(goods);
        if(goodsOperationDTO.getGoodsGalleryList() != null && !goodsOperationDTO.getGoodsGalleryList().isEmpty()){
            //向goods加入图片
            this.setGoodsGalleryParam(goodsOperationDTO.getGoodsGalleryList().get(0), goods);
        }

        //添加商品参数
        if (goodsOperationDTO.getGoodsParamsDTOList() != null && !goodsOperationDTO.getGoodsParamsDTOList().isEmpty()) {
            goods.setParams(JSONUtil.toJsonStr(goodsOperationDTO.getGoodsParamsDTOList()));
        }
        //修改商品
        this.updateById(goods);
        //修改商品sku信息
        this.goodsSkuService.update(goods, goodsOperationDTO);
        //添加相册
        if (goodsOperationDTO.getGoodsGalleryList() != null && !goodsOperationDTO.getGoodsGalleryList().isEmpty()) {
            this.goodsGalleryService.add(goodsOperationDTO.getGoodsGalleryList(), goods.getId());
        }
//        if (GoodsAuthEnum.TOBEAUDITED.name().equals(goods.getAuthFlag())) {
//            //删除es索引
//            this.deleteEsGoods(Collections.singletonList(goodsId));
//        }
        cache.remove(CachePrefix.GOODS.getPrefix() + goodsId);
        //更新es索引
       new Thread(()->{
           this.sendUpdateStatusMessage(goodsId,GoodsAuthEnum.valueOf(goods.getAuthFlag()),GoodsStatusEnum.valueOf(goods.getMarketEnable()));
       }).start();
    }


    @Override
    public GoodsDTO getGoodsVO(String goodsId) {
        Goods goods = this.getById(goodsId);
        GoodsDTO goodsDTO = BeanUtil.toBean(goods, GoodsDTO.class);
        List<GoodsGallery> goodsGalleries = goodsGalleryService.goodsGalleryList(goodsId);
        goodsDTO.setGoodsGalleryList(goodsGalleries.stream().map(GoodsGallery::getThumbnail).collect(Collectors.toList()));
        List<GoodsParamsDTO> list = JSONUtil.toList(goods.getParams(), GoodsParamsDTO.class);
        goodsDTO.setGoodsParamsDTOList(list);
        List<GoodsSkuDTO> goodsListByGoodsId = goodsSkuService.getGoodsListByGoodsId(goodsId);
        goodsDTO.setSkuList(goodsListByGoodsId);
        for (GoodsSkuDTO goodsSkuDTO : goodsListByGoodsId) {
            String[] split = goodsSkuDTO.getCategoryPath().split(",");
            //把这个转化为集合
            List<String> categoryList = Arrays.asList(split);
            List<String> categoryNameByIds = categoryService.getCategoryNameByIds(categoryList);
            goodsDTO.setCategoryName(categoryNameByIds);
        }
        return goodsDTO;
    }

    @Override
    public IPage<Goods> queryByParams(GoodsSearchParamsDTO goodsSearchParams) {
        return this.page(PageUtils.initPage(goodsSearchParams), goodsSearchParams.queryWrapper());
    }

    /**
     * 商品查询
     *
     * @param goodsSearchParams 查询参数
     * @return 商品信息
     */
    @Override
    public List<Goods> queryListByParams(GoodsSearchParamsDTO goodsSearchParams) {
        List<Goods> list = this.list(goodsSearchParams.queryWrapper());
        return list;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean auditGoods(List<String> goodsIds, GoodsAuthEnum goodsAuthEnum) {
        List<String> goodsCacheKeys = new ArrayList<>();
        boolean result = false;
        for (String goodsId : goodsIds) {
            Goods goods = this.checkExist(goodsId);
            goods.setAuthFlag(goodsAuthEnum.name());
            result = this.updateById(goods);
            goodsSkuService.updateGoodsSkuStatus(goods);
            //删除之前的缓存
            goodsCacheKeys.add(CachePrefix.GOODS.getPrefix() + goodsId);
//            //商品审核消息
//            String destination = rocketmqCustomProperties.getGoodsTopic() + ":" + GoodsTagsEnum.GOODS_AUDIT.name();
//            //发送mq消息
//            rocketMQTemplate.asyncSend(destination, JSONUtil.toJsonStr(goods), RocketmqSendCallbackBuilder.commonCallback());
            //更新es索引
            new Thread(()->{
                this.sendUpdateStatusMessage(goodsId,GoodsAuthEnum.valueOf(goods.getAuthFlag()),GoodsStatusEnum.valueOf(goods.getMarketEnable()));
            }).start();
        }
        cache.multiDel(goodsCacheKeys);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateGoodsMarketAble(List<String> goodsIds, GoodsStatusEnum goodsStatusEnum, String underReason) {
        boolean result;

        //如果商品为空，直接返回
        if (goodsIds == null || goodsIds.isEmpty()) {
            return true;
        }
        //修改商品的状态
        LambdaUpdateWrapper<Goods> updateWrapper = this.getUpdateWrapperByStoreAuthority();
        updateWrapper.set(Goods::getMarketEnable, goodsStatusEnum.name());
        updateWrapper.set(Goods::getUnderMessage, underReason);
        updateWrapper.in(Goods::getId, goodsIds);
        result = this.update(updateWrapper);

        //修改sku的状态
        LambdaQueryWrapper<Goods> queryWrapper = this.getQueryWrapperByStoreAuthority();
        queryWrapper.in(Goods::getId, goodsIds);
        List<Goods> goodsList = this.list(queryWrapper);
        this.updateGoodsStatus(goodsIds, goodsStatusEnum, goodsList);
        return result;
    }

    /**
     * 更新商品上架状态状态
     *
     * @param storeId         店铺ID
     * @param goodsStatusEnum 更新的商品状态
     * @param underReason     下架原因
     * @return 更新结果
     */
    @Override
    public Boolean updateGoodsMarketAbleByStoreId(String storeId, GoodsStatusEnum goodsStatusEnum, String underReason) {


        LambdaUpdateWrapper<Goods> updateWrapper = this.getUpdateWrapperByStoreAuthority();
        updateWrapper.set(Goods::getMarketEnable, goodsStatusEnum.name());
        updateWrapper.set(Goods::getUnderMessage, underReason);
        updateWrapper.eq(Goods::getStoreId, storeId);
        boolean result = this.update(updateWrapper);

        //修改规格商品
        this.goodsSkuService.updateGoodsSkuStatusByStoreId(storeId, goodsStatusEnum.name(), null);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)

    public Boolean managerUpdateGoodsMarketAble(List<String> goodsIds, GoodsStatusEnum goodsStatusEnum, String underReason) {
        boolean result;

        //如果商品为空，直接返回
        if (goodsIds == null || goodsIds.isEmpty()) {
            return true;
        }

        //检测管理员权限
        this.checkManagerAuthority();

        LambdaUpdateWrapper<Goods> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(Goods::getMarketEnable, goodsStatusEnum.name());
        updateWrapper.set(Goods::getUnderMessage, underReason);
        updateWrapper.in(Goods::getId, goodsIds);
        result = this.update(updateWrapper);

        //修改规格商品
        LambdaQueryWrapper<Goods> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Goods::getId, goodsIds);
        List<Goods> goodsList = this.list(queryWrapper);
        this.updateGoodsStatus(goodsIds, goodsStatusEnum, goodsList);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteGoods(List<String> goodsIds) {

        LambdaUpdateWrapper<Goods> updateWrapper = this.getUpdateWrapperByStoreAuthority();
        updateWrapper.set(Goods::getMarketEnable, GoodsStatusEnum.DOWN.name());
        updateWrapper.set(Goods::getDeleteFlag, true);
        updateWrapper.in(Goods::getId, goodsIds);
        this.update(updateWrapper);

        //修改规格商品
        LambdaQueryWrapper<Goods> queryWrapper = this.getQueryWrapperByStoreAuthority();
        queryWrapper.in(Goods::getId, goodsIds);
        List<Goods> goodsList = this.list(queryWrapper);
        List<String> goodsCacheKeys = new ArrayList<>();
        for (Goods goods : goodsList) {
            //修改SKU状态
            goodsSkuService.updateGoodsSkuStatus(goods);
            goodsCacheKeys.add(CachePrefix.GOODS.getPrefix() + goods.getId());
        }
        //删除缓存
        cache.multiDel(goodsCacheKeys);
//        this.deleteEsGoods(goodsIds);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean freight(List<String> goodsIds, String templateId) {

        AuthUser authUser = this.checkStoreAuthority();

        FreightTemplate freightTemplate = freightTemplateService.getById(templateId);
        if (freightTemplate == null) {
            throw new ServiceException(ResultCode.FREIGHT_TEMPLATE_NOT_EXIST);
        }
        if (authUser != null && !freightTemplate.getStoreId().equals(authUser.getStoreId())) {
            throw new ServiceException(ResultCode.USER_AUTHORITY_ERROR);
        }
        LambdaUpdateWrapper<Goods> lambdaUpdateWrapper = Wrappers.lambdaUpdate();
        lambdaUpdateWrapper.set(Goods::getTemplateId, templateId);
        lambdaUpdateWrapper.in(Goods::getId, goodsIds);
        List<String> goodsCache = goodsIds.stream().map(item -> CachePrefix.GOODS.getPrefix() + item).collect(Collectors.toList());
        cache.multiDel(goodsCache);
        return this.update(lambdaUpdateWrapper);
    }

    @Override
    public void updateStock(String goodsId, Integer quantity) {
        LambdaUpdateWrapper<Goods> lambdaUpdateWrapper = Wrappers.lambdaUpdate();
        lambdaUpdateWrapper.set(Goods::getQuantity, quantity);
        lambdaUpdateWrapper.eq(Goods::getId, goodsId);
        cache.remove(CachePrefix.GOODS.getPrefix() + goodsId);
        this.update(lambdaUpdateWrapper);
    }

    @Override
    public void sendUpdateStatusMessage(String goodsId,GoodsAuthEnum goodsAuthEnum,GoodsStatusEnum goodsStatusEnum){
        ProductStatusMessage productStatusMessage = new ProductStatusMessage(goodsId,goodsAuthEnum,goodsStatusEnum);
        //发送消息
        rabbitClient.sendMsg(MallMqConstants.Exchanges.EXCHANGE_PRODUCT, MallMqConstants.RoutingKeys.PRODUCT_STATUS_UPDATE_ROUTINGKEY, productStatusMessage);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateGoodsCommentNum(String goodsId, String skuId) {
        GoodsSku goodsSku = goodsSkuService.getGoodsSkuByIdFromCache(skuId);
        if (goodsSku == null) {
            return;
        }

        //获取商品信息
        Goods goods = this.getById(goodsId);

        if (goods == null) {
            return;
        }

//        //修改商品评价数量
//        long commentNum = memberEvaluationService.getEvaluationCount(EvaluationQueryParams.builder().goodsId(goodsId).status("OPEN").build());
//        goods.setCommentNum((int) (commentNum));
//
//        //好评数量
//        long highPraiseNum = memberEvaluationService.getEvaluationCount(EvaluationQueryParams.builder().goodsId(goodsId).status("OPEN").grade(EvaluationGradeEnum.GOOD.name()).build());
//        //好评率
//        double grade = NumberUtil.mul(NumberUtil.div(highPraiseNum, goods.getCommentNum().doubleValue(), 2), 100);
//        goods.setGrade(grade);
        LambdaUpdateWrapper<Goods> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Goods::getId, goodsId);
        updateWrapper.set(Goods::getCommentNum, goods.getCommentNum());
        updateWrapper.set(Goods::getGrade, goods.getGrade());
        this.update(updateWrapper);

        cache.remove(CachePrefix.GOODS.getPrefix() + goodsId);


        // 修改商品sku评价数量
//        this.goodsSkuService.updateGoodsSkuGrade(goodsId, grade, goods.getCommentNum());

//        Map<String, Object> updateIndexFieldsMap = EsIndexUtil.getUpdateIndexFieldsMap(MapUtil.builder(new HashMap<String, Object>()).put("goodsId", goodsId).build(), MapUtil.builder(new HashMap<String, Object>()).put("commentNum", goods.getCommentNum()).put("highPraiseNum", highPraiseNum).put("grade", grade).build());
//        applicationEventPublisher.publishEvent(new TransactionCommitSendMQEvent("更新商品索引信息", rocketmqCustomProperties.getGoodsTopic(), GoodsTagsEnum.UPDATE_GOODS_INDEX_FIELD.name(), JSONUtil.toJsonStr(updateIndexFieldsMap)));
    }

    /**
     * 更新商品的购买数量
     *
     * @param goodsId  商品ID
     * @param buyCount 购买数量
     */
    @Override
    public void updateGoodsBuyCount(String goodsId, int buyCount) {
        LambdaUpdateWrapper<Goods> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Goods::getId, goodsId);
        updateWrapper.setSql("buy_count = buy_count + " + buyCount);
        this.update(updateWrapper);
//        this.update(new LambdaUpdateWrapper<Goods>()
//                .eq(Goods::getId, goodsId)
//                .set(Goods::getBuyCount, buyCount));
    }

//    @Override
//    @Transactional(rollbackFor = Exception.class)
//    public void updateStoreDetail(Store store) {
//        UpdateWrapper updateWrapper = new UpdateWrapper<>()
//                .eq("store_id", store.getId())
//                .set("store_name", store.getStoreName())
//                .set("self_operated", store.getSelfOperated());
//        this.update(updateWrapper);
//        goodsSkuService.update(updateWrapper);
//    }

    @Override
    public long countStoreGoodsNum(String storeId) {
        return this.count(
                new LambdaQueryWrapper<Goods>()
                        .eq(Goods::getStoreId, storeId)
                        .eq(Goods::getDeleteFlag, Boolean.FALSE)
                        .eq(Goods::getAuthFlag, GoodsAuthEnum.PASS.name())
                        .eq(Goods::getMarketEnable, GoodsStatusEnum.UPPER.name()));
    }

    @Override
    public void categoryGoodsName(String categoryId) {
        //获取分类下的商品
        List<Goods> list = this.list(new LambdaQueryWrapper<Goods>().like(Goods::getCategoryPath, categoryId));
        list.parallelStream().forEach(goods -> {
            //移除redis中商品缓存
            cache.remove(CachePrefix.GOODS.getPrefix() + goods.getId());
        });
    }

    @Override
    public void addGoodsCommentNum(Integer commentNum, String goodsId) {
        this.baseMapper.addGoodsCommentNum(commentNum, goodsId);
    }

    /**
     * 更新商品状态
     *
     * @param goodsIds        商品ID
     * @param goodsStatusEnum 商品状态
     * @param goodsList       商品列表
     */
    private void updateGoodsStatus(List<String> goodsIds, GoodsStatusEnum goodsStatusEnum, List<Goods> goodsList) {
        List<String> goodsCacheKeys = new ArrayList<>();
        for (Goods goods : goodsList) {
            goodsCacheKeys.add(CachePrefix.GOODS.getPrefix() + goods.getId());
            //将商品下的所有sku的状态，如果上架则添加sku缓存
            goodsSkuService.updateGoodsSkuStatus(goods);
            //发送商品状态变更消息
            sendUpdateStatusMessage(goods.getId(), GoodsAuthEnum.valueOf(goods.getAuthFlag()),goodsStatusEnum);
        }
        //删除商品缓存信息，在getGoodsVO中查询商品信息时先查缓存如果缓存没有查询数据库再缓存
        cache.multiDel(goodsCacheKeys);


//        if (GoodsStatusEnum.DOWN.equals(goodsStatusEnum)) {
//            //删除es中的商品信息
//            this.deleteEsGoods(goodsIds);
//        } else {
//            this.updateEsGoods(goodsIds);
//        }


        //下架商品发送消息，监听方删除商品相关的促销信息，砍价业务
//        if (goodsStatusEnum.equals(GoodsStatusEnum.DOWN)) {
//            applicationEventPublisher.publishEvent(new TransactionCommitSendMQEvent("下架商品",
//                    rocketmqCustomProperties.getGoodsTopic(), GoodsTagsEnum.DOWN.name(), JSONUtil.toJsonStr(goodsIds)));
//        }
    }

    /**
     * 发送生成ES商品索引
     *
     * @param goods 商品信息
     */
//    private void generateEs(Goods goods) {
//        // 不生成没有审核通过且没有上架的商品
//        if (!GoodsStatusEnum.UPPER.name().equals(goods.getMarketEnable()) || !GoodsAuthEnum.PASS.name().equals(goods.getAuthFlag())) {
//            return;
//        }
//        //更新es索引
//        esGoodsIndexService.updateGoodsIndex(goods.getId());
////        applicationEventPublisher.publishEvent(new TransactionCommitSendMQEvent("生成商品", rocketmqCustomProperties.getGoodsTopic(),
////                GoodsTagsEnum.GENERATOR_GOODS_INDEX.name(), goods.getId()));
//    }

//    /**
//     * 发送生成ES商品索引
//     *
//     * @param goodsIds 商品id
//     */
//    @Transactional
//    public void updateEsGoods(List<String> goodsIds) {
//        applicationEventPublisher.publishEvent(new TransactionCommitSendMQEvent("更新商品", rocketmqCustomProperties.getGoodsTopic(),
//                GoodsTagsEnum.UPDATE_GOODS_INDEX.name(), goodsIds));
//    }

//    /**
//     * 发送删除es索引的信息
//     *
//     * @param goodsIds 商品id
//     */
//    @Transactional
//    public void deleteEsGoods(List<String> goodsIds) {
//        applicationEventPublisher.publishEvent(new TransactionCommitSendMQEvent("删除商品", rocketmqCustomProperties.getGoodsTopic(),
//                GoodsTagsEnum.GOODS_DELETE.name(), JSONUtil.toJsonStr(goodsIds)));
//    }

    /**
     * 添加商品默认图片
     *
     * @param origin 图片
     * @param goods  商品
     */
    private void setGoodsGalleryParam(String origin, Goods goods) {
        GoodsGallery goodsGallery = goodsGalleryService.getGoodsGallery(origin);
        goods.setOriginal(goodsGallery.getOriginal());
        goods.setSmall(goodsGallery.getSmall());
        goods.setThumbnail(goodsGallery.getThumbnail());
    }

    /**
     * 检查商品信息
     * 如果商品是虚拟商品则无需配置配送模板
     * 如果商品是实物商品需要配置配送模板
     * 判断商品是否存在
     * 判断商品是否需要审核
     * 判断当前用户是否为店铺
     *
     * @param goods 商品
     */
    private void checkGoods(Goods goods) {
        //根据商品类型判断是否选择配送模板
        switch (goods.getGoodsType()) {
            case "PHYSICAL_GOODS":
                if ("0".equals(goods.getTemplateId())) {
                    throw new ServiceException(ResultCode.PHYSICAL_GOODS_NEED_TEMP);
                }
                break;
            case "VIRTUAL_GOODS":
                if (!"0".equals(goods.getTemplateId())) {
                    goods.setTemplateId("0");
                }
                break;
            default:
                throw new ServiceException(ResultCode.GOODS_TYPE_ERROR);
        }
        //检查商品是否存在--修改商品时使用
        if (goods.getId() != null) {
            this.checkExist(goods.getId());
        } else {
            //评论次数
            goods.setCommentNum(0);
            //购买次数
            goods.setBuyCount(0);
            //购买次数
            goods.setQuantity(0);
            //商品评分
            goods.setGrade(100.0);
        }

        //获取商品系统配置决定是否审核
        Setting setting = settingService.get(SettingEnum.GOODS_SETTING.name());
        GoodsSetting goodsSetting = JSONUtil.toBean(setting.getSettingValue(), GoodsSetting.class);
        //是否需要审核
        goods.setAuthFlag(Boolean.TRUE.equals(goodsSetting.getGoodsCheck()) ? GoodsAuthEnum.TOBEAUDITED.name() : GoodsAuthEnum.PASS.name());
        //判断当前用户是否为店铺
        AuthUser authUser =  UserContext.getCurrentUser();
        if (authUser.getRole().equals(UserEnums.STORE)) {//如果为店铺
            //获取店铺详情
            StoreDTO storeDetail = this.storeService.getStoreDetail();
            if (storeDetail.getSelfOperated() != null) {
                goods.setSelfOperated(storeDetail.getSelfOperated());
            }
            goods.setStoreId(storeDetail.getId());
            goods.setStoreName(storeDetail.getStoreName());
            goods.setSelfOperated(storeDetail.getSelfOperated());
        } else {
            throw new ServiceException(ResultCode.STORE_NOT_LOGIN_ERROR);
        }
    }

    /**
     * 判断商品是否存在
     *
     * @param goodsId 商品id
     * @return 商品信息
     */
    private Goods checkExist(String goodsId) {
        Goods goods = getById(goodsId);
        if (goods == null) {
            log.error("商品ID为" + goodsId + "的商品不存在");
            throw new ServiceException(ResultCode.GOODS_NOT_EXIST);
        }
        return goods;
    }


    /**
     * 获取UpdateWrapper（检查用户越权）
     * @return updateWrapper
     */
    private LambdaUpdateWrapper<Goods> getUpdateWrapperByStoreAuthority() {
        LambdaUpdateWrapper<Goods> updateWrapper = new LambdaUpdateWrapper<>();
        AuthUser authUser = this.checkStoreAuthority();
        if (authUser != null) {
            updateWrapper.eq(Goods::getStoreId, authUser.getStoreId());
        }
        return updateWrapper;
    }


    /**
     * 检查当前登录的店铺
     *
     * @return 当前登录的店铺
     */
    private AuthUser checkStoreAuthority() {
        AuthUser currentUser =  UserContext.getCurrentUser();
        //如果当前会员不为空，且为店铺角色
        if (currentUser != null && (currentUser.getRole().equals(UserEnums.STORE) && currentUser.getStoreId() != null)) {
            return currentUser;
        }
        return null;
    }

    /**
     * 检查当前登录的店铺
     *
     * @return 当前登录的店铺
     */
    private AuthUser checkManagerAuthority() {
        AuthUser currentUser =  UserContext.getCurrentUser();
        //如果当前会员不为空，且为店铺角色
        if (currentUser != null && (currentUser.getRole().equals(UserEnums.MANAGER))) {
            return currentUser;
        } else {
            throw new ServiceException(ResultCode.USER_AUTHORITY_ERROR);
        }
    }

    /**
     * 获取QueryWrapper（检查用户越权）
     *
     * @return queryWrapper
     */
    private LambdaQueryWrapper<Goods> getQueryWrapperByStoreAuthority() {
        LambdaQueryWrapper<Goods> queryWrapper = new LambdaQueryWrapper<>();
        AuthUser authUser = this.checkStoreAuthority();
        if (authUser != null) {
            queryWrapper.eq(Goods::getStoreId, authUser.getStoreId());
        }
        return queryWrapper;
    }

}
