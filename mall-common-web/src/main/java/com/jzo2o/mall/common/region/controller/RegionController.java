package com.jzo2o.mall.common.region.controller;

import com.jzo2o.mall.common.enums.ResultUtil;
import com.jzo2o.mall.common.model.ResultMessage;
import com.jzo2o.mall.system.model.domain.Region;
import com.jzo2o.mall.system.model.dto.RegionDTO;
import com.jzo2o.mall.system.service.RegionService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


/**
 * 地址信息接口
 */
@RestController
@Api(tags = "地址信息接口")
@RequestMapping("/region")
public class RegionController {

    @Autowired
    private RegionService regionService;

    @ApiOperation(value = "点地图获取地址信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "cityCode", value = "城市code", dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "townName", value = "镇名称", dataType = "Long", paramType = "query")
    })
    @GetMapping(value = "/region")
    public Map<String, Object> getRegion(@RequestParam String cityCode, @RequestParam String townName) {
        Map<String, Object> region = regionService.getRegion(cityCode, townName);
        return region;
    }

    @GetMapping(value = "/name")
    @ApiOperation(value = "根据名字获取地区地址id")
    public String getItemByLastName(String lastName) {
        String itemByLastName = regionService.getItemByLastName(lastName);
        return itemByLastName;
    }

    @GetMapping(value = "/item/{id}")
    @ApiImplicitParam(name = "id", value = "地区ID", required = true, dataType = "String", paramType = "path")
    @ApiOperation(value = "通过id获取子地区")
    public List<Region> getItem(@PathVariable String id) {
        List<Region> item = regionService.getItem(id);
        return item;
    }

    @GetMapping(value = "/allCity")
    @ApiOperation(value = "获取所有的省-市")
    public List<RegionDTO> getAllCity() {
        List<RegionDTO> allCity = regionService.getAllCity();
        return allCity;
    }


}
