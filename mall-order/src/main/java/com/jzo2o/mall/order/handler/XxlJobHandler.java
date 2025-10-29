package com.jzo2o.mall.order.handler;

import com.jzo2o.redis.annotations.Lock;
import com.jzo2o.redis.constants.RedisSyncQueueConstants;
import com.jzo2o.redis.sync.SyncManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;


@Component
@Slf4j
public class XxlJobHandler {

    @Resource
    private SyncManager syncManager;



    /**
     * 秒杀同步队列
     * 5s一次
     */
//    @XxlJob("seizeCouponSyncJob")
    @Scheduled(cron = "0/5 * * * * ?")
    @Lock(formatter = "SECKILL:RESULT_PROCESS", startDog = true)
    public void seizeCouponSyncJob() {
        log.info("开始同步秒杀结果");
        syncManager.start("SECKILL:SYNC", RedisSyncQueueConstants.STORAGE_TYPE_HASH, RedisSyncQueueConstants.MODE_SINGLE);
    }


}
