package com.jzo2o.mall.common.constants;

/**
 * redis相关常量
 **/
public class RedisConstants {

    public static final class PromotionCacheName {
        /**
         * seckill 活动缓存
         */
        public static final String SECKILL_CACHE = "SECKILL_CACHE";


    }

    public static final class CacheManager {
        /**
         * 缓存时间永久
         */
        public static final String FOREVER = "cacheManagerForever";

        /**
         * 缓存时间永久
         */
        public static final String THIRTY_MINUTES = "cacheManager30Minutes";

        /**
         * 缓存时间1天
         */
        public static final String ONE_DAY = "cacheManagerOneDay";
    }

    public static final class Ttl {
        /**
         * 缓存时间30分钟
         */
        public static final int THIRTY = 30;
    }

}
