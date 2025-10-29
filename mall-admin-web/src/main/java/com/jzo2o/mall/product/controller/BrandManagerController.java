package com.jzo2o.mall.product.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jzo2o.mall.common.enums.ResultCode;
import com.jzo2o.mall.common.exception.ServiceException;
import com.jzo2o.mall.product.model.domain.Brand;
import com.jzo2o.mall.product.model.dto.BrandDTO;
import com.jzo2o.mall.product.model.dto.BrandPageDTO;
import com.jzo2o.mall.product.service.BrandService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;


/**
 * 管理端,品牌接口
 */
@RestController
@Api(tags = "管理端,品牌接口")
@RequestMapping("/goods/brand")
public class BrandManagerController {

    /**
     * 品牌
     */
    @Autowired
    private BrandService brandService;

    @ApiOperation(value = "通过id获取")
    @ApiImplicitParam(name = "id", value = "品牌ID", required = true, dataType = "String", paramType = "path")
    @GetMapping(value = "/get/{id}")
    public Brand get(@NotNull @PathVariable String id) {
        return brandService.getById(id);
    }

    @GetMapping(value = "/all")
    @ApiOperation(value = "获取所有可用品牌")
    public List<Brand> getAll() {
        List<Brand> list = brandService.list(new QueryWrapper<Brand>().eq("delete_flag", 0));
        return list;
    }

    @ApiOperation(value = "分页获取")
    @GetMapping(value = "/getByPage")
    public IPage<Brand> getByPage(BrandPageDTO page) {
        return brandService.getBrandsByPage(page);
    }

    @ApiOperation(value = "新增品牌")
    @PostMapping
    public BrandDTO save(@Valid BrandDTO brand) {
        if (brandService.addBrand(brand)) {
            return brand;
        }
        throw new ServiceException(ResultCode.BRAND_SAVE_ERROR);
    }

    @ApiOperation(value = "更新数据")
    @ApiImplicitParam(name = "id", value = "品牌ID", required = true, dataType = "String", paramType = "path")
    @PutMapping("/{id}")
    public BrandDTO update(@PathVariable String id, @Valid BrandDTO brand) {
        brand.setId(id);
        if (brandService.updateBrand(brand)) {
            return brand;
        }
        throw new ServiceException(ResultCode.BRAND_UPDATE_ERROR);
    }

    @ApiOperation(value = "后台禁用品牌")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "brandId", value = "品牌ID", required = true, dataType = "String", paramType = "path"),
            @ApiImplicitParam(name = "disable", value = "是否可用", required = true, dataType = "boolean", paramType = "query")
    })
    @PutMapping(value = "/disable/{brandId}")
    public void disable(@PathVariable String brandId, @RequestParam Boolean disable) {
        if (brandService.brandDisable(brandId, disable)) {
            return ;
        }
        throw new ServiceException(ResultCode.BRAND_DISABLE_ERROR);
    }

    @ApiOperation(value = "批量删除")
    @ApiImplicitParam(name = "ids", value = "品牌ID", required = true, dataType = "String", allowMultiple = true, paramType = "path")
    @DeleteMapping(value = "/delByIds/{ids}")
    public void delAllByIds(@PathVariable List<String> ids) {
        brandService.deleteBrands(ids);
    }

}
