package com.jzo2o.mall.product.controller;

import com.jzo2o.common.model.CurrentUserInfo;
import com.jzo2o.common.utils.UserContext;
import com.jzo2o.mall.common.enums.ResultUtil;
import com.jzo2o.mall.common.model.AuthUser;
import com.jzo2o.mall.common.model.ResultMessage;
import com.jzo2o.mall.member.service.StoreDetailService;
import com.jzo2o.mall.product.model.dto.CategoryBrandDTO;
import com.jzo2o.mall.product.model.dto.CategoryDTO;
import com.jzo2o.mall.product.service.CategoryBrandService;
import com.jzo2o.mall.product.service.CategoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;

/**
 * 店铺端,商品分类接口
 */
@RestController
@Api(tags = "店铺端,商品分类接口")
@RequestMapping("/goods/category")
@CacheConfig(cacheNames = "category")
public class CategoryStoreController {

    /**
     * 分类
     */
    @Autowired
    private CategoryService categoryService;
    /**
     * 分类品牌
     */
    @Autowired
    private CategoryBrandService categoryBrandService;
    /**
     * 店铺详情
     */
    @Autowired
    private StoreDetailService storeDetailService;

    @ApiOperation(value = "获取店铺经营的分类")
    @GetMapping(value = "/all")
    public List<CategoryDTO> getListAll() {
        AuthUser authUser =  UserContext.getCurrentUser();
        String storeId = authUser.getStoreId();
        //获取店铺经营范围
        String goodsManagementCategory = storeDetailService.getStoreDetail(storeId).getGoodsManagementCategory();
        List<CategoryDTO> storeCategory = this.categoryService.getStoreCategory(goodsManagementCategory.split(","));
        return storeCategory;
    }

    @ApiOperation(value = "获取所选分类关联的品牌信息")
    @GetMapping(value = "/{categoryId}/brands")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "categoryId", value = "分类id", required = true, paramType = "path"),
    })
    public List<CategoryBrandDTO> queryBrands(@PathVariable String categoryId) {
        List<CategoryBrandDTO> categoryBrandList = this.categoryBrandService.getCategoryBrandList(categoryId);
        return categoryBrandList;
    }

}
