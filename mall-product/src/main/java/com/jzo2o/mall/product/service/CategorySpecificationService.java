package com.jzo2o.mall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jzo2o.mall.product.model.domain.CategorySpecification;
import com.jzo2o.mall.product.model.domain.Specification;

import java.util.List;

/**
 * 商品分类规格业务层
 */
public interface CategorySpecificationService extends IService<CategorySpecification> {
    /**
     * 根据分类id查询规格信息
     *
     * @param categoryId 分类id
     * @return 分类规格关联信息
     */
    List<Specification> getCategorySpecList(String categoryId);


    /**
     * 通过分类ID删除关联规格
     *
     * @param categoryId 分类ID
     */
    void deleteByCategoryId(String categoryId);
}