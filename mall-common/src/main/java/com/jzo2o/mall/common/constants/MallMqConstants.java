package com.jzo2o.mall.common.constants;

/**
 * 静态变量
 *
 * @author zzj
 * @version 1.0
 */
public interface MallMqConstants {
    /**
     * 默认延时时间为-1
     */
    int DEFAULT_DELAY = -1;

    /**
     * 低延迟时间：5秒
     */
    int LOW_DELAY = 5000;

    /**
     * 标准延迟时间：10秒
     */
    int NORMAL_DELAY = 10000;

    /**
     * 延迟交换机关键字
     */
    String DELAYED_KEYWORD = "delayed";

    /**
     * 表明是延迟队列
     */
    String DELAYED = "true";

    /**
     * 定义消息交换机，约定：1：类型都为topic，2：延迟队列命名由.delayed结尾
     */
    interface Exchanges {

        /**
         * 订单
         */
        String EXCHANGE_ORDER = "mall.exchange.order";
        /**
         * 商品
         */
        String EXCHANGE_PRODUCT = "mall.exchange.product";
    }

    /**
     * 定义消息队列
     */
    interface Queues {

        /**
         * 商品库存更新队列
         */
        String PRODUCT_STOCK_UPDATE_QUEUE = "mall.product.stock.update.queue";

        /**
         * 商品索引更新队列
         */
        String PRODUCT_INDEX_UPDATE_QUEUE = "mall.product.index.update.queue";

        /**
         * 支付状态更新队列
         */
        String ORDER_PAYMENT_STATUS_UPDATE_QUEUE = "mall.order.payment.status.update.queue";
        /**
         * 退款队列
         */
        String ORDER_PAYMENT_REFUND_QUEUE = "mall.order.payment.refund.queue";
        /**
         * 优惠券退回队列
         */
        String COUPON_REFUND_QUEUE = "mall.promotion.coupon.refund.queue";

        /**
         *  订单确认队列
         */
        String ORDER_CONFIRM_QUEUE = "mall.order.confirm.queue";

    }

    /**
     * 定义路由key，约定：1：路由key与交换机类型一致，2：路由key与队列名称一致
     */
    interface RoutingKeys {


        /**
         * 创建订单
         */
        String ORDER_CREATE_ROUTINGKEY = "ORDER_CREATE_ROUTINGKEY";

        /**
         * 更新订单状态
         */
        String ORDER_STATUS_UPDATE_ROUTINGKEY = "ORDER_STATUS_UPDATE_ROUTINGKEY";

        /**
         * 更新商品状态
         */
        String PRODUCT_STATUS_UPDATE_ROUTINGKEY = "PRODUCT_STATUS_UPDATE_ROUTINGKEY";

        /**
         * 库存更新
         */
        String PRODUCT_STOCK_UPDATE_ROUTINGKEY = "PRODUCT_STOCK_UPDATE_ROUTINGKEY";

    }
}
