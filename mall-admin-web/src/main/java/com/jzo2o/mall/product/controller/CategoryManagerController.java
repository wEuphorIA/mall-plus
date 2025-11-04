package com.jzo2o.mall.product.controller;

import com.jzo2o.mall.common.enums.ResultCode;
import com.jzo2o.mall.common.exception.ServiceException;
import com.jzo2o.mall.product.model.domain.Category;
import com.jzo2o.mall.product.model.dto.CategoryDTO;
import com.jzo2o.mall.product.model.dto.CategorySearchParamsDTO;
import com.jzo2o.mall.product.service.CategoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 管理端,商品分类接口
 */
@RestController
@Api(tags = "管理端,商品分类接口")
@RequestMapping("/goods/category")
@CacheConfig(cacheNames = "category")
@Slf4j
public class CategoryManagerController {

    /**
     * 分类
     */
    @Autowired
    private CategoryService categoryService;

    /**
     * 商品
     */
//    @Autowired
//    private GoodsService goodsService;

    @ApiOperation(value = "查询某分类下的全部子分类列表")
    @ApiImplicitParam(name = "parentId", value = "父id，顶级为0", required = true, dataType = "String", paramType = "path")
    @GetMapping(value = "/{parentId}/all-children")
    public List<Category> list(@PathVariable String parentId) {
        List<Category> categories = this.categoryService.dbList(parentId);
        return categories;
    }

    @ApiOperation(value = "查询全部分类列表")
    @GetMapping(value = "/allChildren")
    public List<CategoryDTO> list(CategorySearchParamsDTO categorySearchParams) {
        List<CategoryDTO> categoryDTOS = this.categoryService.listAllChildren(categorySearchParams);
        return categoryDTOS;
    }

    @PostMapping
    @ApiOperation(value = "添加商品分类")
    public Category saveCategory(@Valid Category category) {
        //非顶级分类
        if (category.getParentId() != null && !"0".equals(category.getParentId())) {
            Category parent = categoryService.getById(category.getParentId());
            if (parent == null) {
                throw new ServiceException(ResultCode.CATEGORY_PARENT_NOT_EXIST);
            }
            if (category.getLevel() >= 4) {
                throw new ServiceException(ResultCode.CATEGORY_BEYOND_THREE);
            }
        }
        if (categoryService.saveCategory(category)) {
            return category;
        }
        throw new ServiceException(ResultCode.CATEGORY_SAVE_ERROR);
    }

    @PutMapping
    @ApiOperation(value = "修改商品分类")
    public Category updateCategory(@Valid CategoryDTO category) {
        Category catTemp = categoryService.getById(category.getId());
        if (catTemp == null) {
            throw new ServiceException(ResultCode.CATEGORY_NOT_EXIST);
        }

        categoryService.updateCategory(category);
        return category;
    }

    @DeleteMapping(value = "/{id}")
    @ApiImplicitParam(name = "id", value = "分类ID", required = true, paramType = "path", dataType = "String")
    @ApiOperation(value = "通过id删除分类")
    public void delAllByIds(@NotNull @PathVariable String id) {
        Category category = new Category();
        category.setParentId(id);
        List<Category> list = categoryService.findByAllBySortOrder(category);
        if (list != null && !list.isEmpty()) {
            throw new ServiceException(ResultCode.CATEGORY_HAS_CHILDREN);

        }
        //查询某商品分类的商品数量
//        long count = goodsService.getGoodsCountByCategory(id);
//        if (count > 0) {
//            throw new ServiceException(ResultCode.CATEGORY_HAS_GOODS);
//        }
        categoryService.delete(id);
    }

    @PutMapping(value = "/disable/{id}")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "goodsId", value = "分类ID", required = true, paramType = "path", dataType = "String")
    })
    @ApiOperation(value = "后台 禁用/启用 分类")
    public void disable(@PathVariable String id, @RequestParam Boolean enableOperations) {

        Category category = categoryService.getById(id);
        if (category == null) {
            throw new ServiceException(ResultCode.CATEGORY_NOT_EXIST);
        }
        //更新该分类及所有子分类的禁用/启用状态为enableOperations
        categoryService.updateCategoryStatus(id, enableOperations);
    }

}