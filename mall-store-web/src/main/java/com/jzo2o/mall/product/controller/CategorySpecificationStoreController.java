package com.jzo2o.mall.product.controller;

import com.jzo2o.mall.product.model.domain.Specification;
import com.jzo2o.mall.product.service.CategorySpecificationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 店铺端,商品分类绑定规格接口
 */
@RestController
@Api(tags = "店铺端,商品分类规格接口")
@RequestMapping("/goods/categorySpec")
public class CategorySpecificationStoreController {
    @Autowired
    private CategorySpecificationService categorySpecificationService;


    @ApiOperation(value = "查询某分类下绑定的规格信息")
    @GetMapping(value = "/{category_id}")
    @ApiImplicitParam(name = "category_id", value = "分类id", required = true, dataType = "String", paramType = "path")
    public List<Specification> getCategorySpec(@PathVariable("category_id") String categoryId) {
        return categorySpecificationService.getCategorySpecList(categoryId);
    }


}
