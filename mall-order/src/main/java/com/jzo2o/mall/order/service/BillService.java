package com.jzo2o.mall.order.service;

import cn.hutool.core.date.DateTime;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jzo2o.mall.order.model.domain.Bill;
import com.jzo2o.mall.order.model.dto.BillListDTO;
import com.jzo2o.mall.order.model.dto.BillSearchParamsDTO;
import org.springframework.cache.annotation.CacheConfig;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * 结算单业务层
 */
@CacheConfig(cacheNames = "bill")
public interface BillService extends IService<Bill> {

    /**
     * 生成结算单
     *
     * @param storeId   商家ID
     * @param startTime 开始时间
     * @param endTime   结束时间
     */
    void createBill(String storeId, LocalDateTime startTime, LocalDateTime endTime);


    /**
     * 立即结算
     * 用于关闭商家，立即结算使用
     *
     * @param storeId
     * @param endTime 结束时间
     */
    void immediatelyBill(String storeId, Long endTime);

    /**
     * 获取结算单分页
     *
     * @param billSearchParams 结算单搜索条件
     * @return 结算单分页
     */
    IPage<BillListDTO> billPage(BillSearchParamsDTO billSearchParams);

    /**
     * 商家核对结算单
     *
     * @param id 结算单ID
     * @return 操作状态
     */
    boolean check(String id);

    /**
     * 平台结算
     *
     * @param id 结算单ID
     * @return 操作状态
     */
    boolean complete(String id);

    /**
     * 下载结算单
     * @response response
     * @param id 结算单ID
     */
    void download(HttpServletResponse response, String id);
}