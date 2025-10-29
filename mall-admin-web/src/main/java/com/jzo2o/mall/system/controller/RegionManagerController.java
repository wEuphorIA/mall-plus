package com.jzo2o.mall.system.controller;

import com.jzo2o.mall.common.enums.ResultUtil;
import com.jzo2o.mall.common.model.ResultMessage;
import com.jzo2o.mall.system.model.domain.Region;
import com.jzo2o.mall.system.service.RegionService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;


/**
 * 管理端,行政地区管理接口
 */
@RestController
@Api(tags = "管理端,行政地区管理接口")
@RequestMapping("/setting/region")
public class RegionManagerController {
    @Autowired
    private RegionService regionService;

    @PostMapping(value = "/sync")
    @ApiOperation(value = "同步高德行政地区数据")
    public void synchronizationData(String url) {
        regionService.synchronizationData(url);
    }

    @GetMapping(value = "/{id}")
    @ApiImplicitParam(name = "id", value = "地区ID", required = true, dataType = "String", paramType = "path")
    @ApiOperation(value = "通过id获取地区详情")
    public Region get(@PathVariable String id) {
        Region region = regionService.getById(id);
        return region;
    }

    @GetMapping(value = "/item/{id}")
    @ApiImplicitParam(name = "id", value = "地区ID", required = true, dataType = "String", paramType = "path")
    @ApiOperation(value = "通过id获取子地区")
    public List<Region> getItem(@PathVariable String id) {
        List<Region> item = regionService.getItem(id);
        return item;
    }

    @PutMapping(value = "/{id}")
    @ApiImplicitParam(name = "id", value = "地区ID", required = true, dataType = "String", paramType = "path")
    @ApiOperation(value = "更新地区")
    public Region update(@PathVariable String id, @Valid Region region) {
        region.setId(id);
        regionService.updateById(region);
        return region;
    }


    @PostMapping
    @ApiOperation(value = "增加地区")
    public Region save(@Valid Region region) {
        regionService.save(region);
        return region;
    }

    @DeleteMapping(value = "{ids}")
    @ApiImplicitParam(name = "id", value = "地区ID", required = true, dataType = "String", allowMultiple = true, paramType = "path")
    @ApiOperation(value = "批量通过id删除")
    public void delAllByIds(@PathVariable List<String> ids) {
        regionService.removeByIds(ids);
    }
}
