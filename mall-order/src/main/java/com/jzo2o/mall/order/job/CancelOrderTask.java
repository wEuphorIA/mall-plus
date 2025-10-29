package com.jzo2o.mall.order.job;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jzo2o.mall.common.enums.OrderStatusEnum;
import com.jzo2o.mall.common.enums.RefundStatusEnum;
import com.jzo2o.mall.order.model.domain.Order;
import com.jzo2o.mall.order.service.OrderService;
import com.jzo2o.mall.payment.model.domain.RefundLog;
import com.jzo2o.mall.payment.model.dto.RefundResultResDTO;
import com.jzo2o.mall.payment.service.PayDelegate;
import com.jzo2o.mall.payment.service.RefundLogService;
import com.jzo2o.mall.system.enums.SettingEnum;
import com.jzo2o.mall.system.model.domain.Setting;
import com.jzo2o.mall.system.model.dto.OrderSetting;
import com.jzo2o.mall.system.service.SettingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Mr.M
 * @version 1.0
 * @description 订单超时自动取消
 * @date 2024/5/26 7:15
 */
@Component
@Slf4j
public class CancelOrderTask {
    /**
     * 订单
     */
    @Autowired
    private OrderService orderService;
    /**
     * 设置
     */
    @Autowired
    private SettingService settingService;

    /**
     * 使用 SpringTask实现每隔10分钟扫描待支付订单信息
     */
    @Scheduled(cron = "0/10 * * * * ?")
    public void cancelOrder() {
        Setting setting = settingService.get(SettingEnum.ORDER_SETTING.name());
        OrderSetting orderSetting = JSONUtil.toBean(setting.getSettingValue(), OrderSetting.class);
        if (orderSetting != null && orderSetting.getAutoCancel() != null) {
            //订单自动取消时间 = 当前时间 - 自动取消时间分钟数
            DateTime cancelTime = DateUtil.offsetMinute(DateUtil.date(), -orderSetting.getAutoCancel());
            LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Order::getOrderStatus, OrderStatusEnum.UNPAID.name());
            //订单创建时间 <= 订单自动取消时间
            queryWrapper.le(Order::getCreateTime, cancelTime);
            List<Order> list = orderService.list(queryWrapper);
            List<String> cancelSnList = list.stream().map(Order::getSn).collect(Collectors.toList());
            for (String sn : cancelSnList) {
                orderService.systemCancel(sn, "超时未支付自动取消",false);
            }
        }
    }

}
