package com.jzo2o.mall.order.job;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.jzo2o.common.utils.DateUtils;
import com.jzo2o.mall.common.enums.RefundStatusEnum;
import com.jzo2o.mall.member.model.dto.StoreSettlementDay;
import com.jzo2o.mall.member.service.StoreDetailService;
import com.jzo2o.mall.order.service.BillService;
import com.jzo2o.mall.payment.model.domain.RefundLog;
import com.jzo2o.mall.payment.model.dto.RefundResultResDTO;
import com.jzo2o.mall.payment.service.PayDelegate;
import com.jzo2o.mall.payment.service.RefundLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

/**
 * @author Mr.M
 * @version 1.0
 * @description 生成结算单
 * @date 2024/5/26 7:15
 */
@Component
@Slf4j
public class BillExecuteTask {
    /**
     * 结算单
     */
    @Autowired
    private BillService billService;
    /**
     * 店铺详情
     */
    @Autowired
    private StoreDetailService storeDetailService;

    /**
     * 1.查询今日待结算的商家
     * 2.查询商家上次结算日期，生成本次结算单
     * 3.记录商家结算日
     */
    @Scheduled(cron = "0/10 * * * * ?")
    public void billExecute() {
        //获取当前天数
        int day = DateUtil.date().dayOfMonth();
        //获取当前结算日开始时间
        LocalDateTime dayStartTime = DateUtils.getDayStartTime(LocalDateTime.now());
        //获取待结算商家列表，根据结算日查询小于当前结算日的商家
        List<StoreSettlementDay> storeList = storeDetailService.getSettlementStore(day,dayStartTime);
        //账单的结束日期为结算日上一天的结束时间
        LocalDateTime settlementEndTime = DateUtils.getDayEndTime(LocalDateTime.now().minusDays(1));
        //批量商家结算
        for (StoreSettlementDay storeSettlementDay : storeList) {

            //生成结算单
            billService.createBill(storeSettlementDay.getStoreId(), storeSettlementDay.getSettlementDay(), settlementEndTime);
            //修改店铺结算时间
            storeDetailService.updateSettlementDay(storeSettlementDay.getStoreId(), dayStartTime);
        }
    }

    public static void main(String[] args) {
        System.out.println(DateUtils.getDayEndTime(LocalDateTime.now().minusDays(1)));
    }

}
