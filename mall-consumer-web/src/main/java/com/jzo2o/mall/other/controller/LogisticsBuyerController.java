package com.jzo2o.mall.other.controller;

import com.jzo2o.mall.common.enums.ResultUtil;
import com.jzo2o.mall.common.model.ResultMessage;
import com.jzo2o.mall.order.model.domain.Logistics;
import com.jzo2o.mall.order.service.LogisticsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 买家端,物流公司接口
 *
 */
@RestController
@Api(tags = "买家端,物流公司接口")
@RequestMapping("/other/logistics")
public class LogisticsBuyerController {

    @Autowired
    private LogisticsService logisticsService;


    @ApiOperation(value = "分页获取物流公司")
    @GetMapping
    public List<Logistics> getByPage() {
        List<Logistics> openLogistics = logisticsService.getOpenLogistics();
        return openLogistics;
    }

}
