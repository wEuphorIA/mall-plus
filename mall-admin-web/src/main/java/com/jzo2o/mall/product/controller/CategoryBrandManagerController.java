package com.jzo2o.mall.product.controller;

import com.jzo2o.mall.product.model.dto.CategoryBrandDTO;
import com.jzo2o.mall.product.service.CategoryBrandService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理端,分类品牌接口
 */
@RestController
@Api(tags = "管理端,分类品牌接口")
@RequestMapping("/goods/categoryBrand")
public class CategoryBrandManagerController {

    /**
     * 规格品牌管理
     */
    @Autowired
    private CategoryBrandService categoryBrandService;

    @ApiOperation(value = "查询某分类下绑定的品牌信息")
    @ApiImplicitParam(name = "categoryId", value = "分类id", required = true, dataType = "String", paramType = "path")
    @GetMapping(value = "/{categoryId}")
    public List<CategoryBrandDTO> getCategoryBrand(@PathVariable String categoryId) {
        List<CategoryBrandDTO> categoryBrandList = categoryBrandService.getCategoryBrandList(categoryId);
        return categoryBrandList;
    }

    @ApiOperation(value = "保存某分类下绑定的品牌信息")
    @PostMapping(value = "/{categoryId}")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "categoryId", value = "分类id", required = true, paramType = "path", dataType = "String"),
            @ApiImplicitParam(name = "categoryBrands", value = "品牌id数组", required = true, paramType = "query", dataType = "String[]")
    })
    public void saveCategoryBrand(@PathVariable String categoryId, @RequestParam List<String> categoryBrands) {
        categoryBrandService.saveCategoryBrandList(categoryId,categoryBrands);
    }

}
