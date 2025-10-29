package com.jzo2o.mall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jzo2o.mall.product.mapper.CategorySpecificationMapper;
import com.jzo2o.mall.product.model.domain.CategorySpecification;
import com.jzo2o.mall.product.model.domain.Specification;
import com.jzo2o.mall.product.service.CategorySpecificationService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 商品分类规格业务层实现
 *
 * @author pikachu
 * @since 2020-02-23 15:18:56
 */
@Service
public class CategorySpecificationServiceImpl extends ServiceImpl<CategorySpecificationMapper, CategorySpecification> implements CategorySpecificationService {

    @Override
    public List<Specification> getCategorySpecList(String categoryId) {
        return this.baseMapper.getCategorySpecList(categoryId);
    }

    @Override
    public void deleteByCategoryId(String categoryId) {
        this.baseMapper.delete(new LambdaQueryWrapper<CategorySpecification>().eq(CategorySpecification::getCategoryId, categoryId));
    }
}