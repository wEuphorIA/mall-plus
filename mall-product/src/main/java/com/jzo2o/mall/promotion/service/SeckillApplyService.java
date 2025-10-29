package com.jzo2o.mall.promotion.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jzo2o.mall.common.constants.RedisConstants;
import com.jzo2o.mall.promotion.model.domain.Seckill;
import com.jzo2o.mall.promotion.model.domain.SeckillApply;
import com.jzo2o.mall.promotion.model.dto.SeckillApplyDTO;
import com.jzo2o.mall.promotion.model.dto.SeckillGoodsDTO;
import com.jzo2o.mall.promotion.model.dto.SeckillSearchParams;
import com.jzo2o.mall.promotion.model.dto.SeckillTimelineDTO;
import com.jzo2o.mysql.domain.PageVO;
import org.springframework.cache.annotation.Cacheable;

import java.util.Date;
import java.util.List;

/**
 * 秒杀申请业务层
 *
 * @author Chopper
 * @since 2020/11/18 9:45 上午
 */
public interface SeckillApplyService extends IService<SeckillApply> {


    /**
     * 获取当天秒杀活动信息列表（时刻及对应时刻下的商品）
     *
     * @return 秒杀活动信息列表
     */
    List<SeckillTimelineDTO> getSeckillTimeline(String date);

    /**
     * 获取秒杀活动信息
     * @param date 日期 (yyyy-MM-dd格式字符串)
     * @return 秒杀活动信息
     */
    List<SeckillTimelineDTO> getSeckillTimelineInfoAll(String date);

    /**
     * 获取当天某个时刻的秒杀活动商品列表
     *
     * @param timeline 指定时刻
     * @return 秒杀活动商品列表
     */
    List<SeckillGoodsDTO> getSeckillGoods(Integer timeline);

    /**
     * 分页查询限时请购申请列表
     *
     * @param queryParam 秒杀活动申请查询参数
     * @param pageVo     分页参数
     * @return 限时请购申请列表
     */
    IPage<SeckillApply> getSeckillApplyPage(SeckillSearchParams queryParam, PageVO pageVo);

    /**
     * 查询限时请购申请列表
     *
     * @param queryParam 秒杀活动申请查询参数
     * @return 限时请购申请列表
     */
    List<SeckillApply> getSeckillApplyList(SeckillSearchParams queryParam);

    /**
     * 查询限时请购申请列表总数
     *
     * @param queryParam 查询条件
     * @return 限时请购申请列表总数
     */
    long getSeckillApplyCount(SeckillSearchParams queryParam);

    /**
     * 查询限时请购申请
     *
     * @param queryParam 秒杀活动申请查询参数
     * @return 限时请购申请
     */
    SeckillApply getSeckillApply(SeckillSearchParams queryParam);

    /**
     * 添加秒杀活动申请
     * 检测是否商品是否同时参加多个活动
     * 将秒杀商品信息存入秒杀活动中
     * 保存秒杀活动商品，促销商品信息
     *
     * @param seckillId        秒杀活动编号
     * @param storeId          商家id
     * @param seckillApplyList 秒杀活动申请列表
     */
    void addSeckillApply(String seckillId, String storeId, List<SeckillApplyDTO> seckillApplyList);

    /**
     * 批量删除秒杀活动商品
     *
     * @param seckillId 秒杀活动活动id
     * @param id        秒杀活动商品
     */
    void removeSeckillApply(String seckillId, String id);

    /**
     * 更新秒杀商品出售数量
     *
     * @param seckillId 秒杀活动id
     * @param skuId 商品skuId
     * @param saleNum 出售数量
     */
    void updateSeckillApplySaleNum(String seckillId, String skuId, Integer saleNum);

    /**
     * 更新秒杀活动时间
     *
     * @param seckill 秒杀活动
     * @return 是否更新成功
     */
    boolean updateSeckillApplyTime(Seckill seckill);

}