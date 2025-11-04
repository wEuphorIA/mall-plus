package com.jzo2o.mall.product.service.impl;

import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jzo2o.mall.common.cache.CachePrefix;
import com.jzo2o.mall.common.enums.ResultCode;
import com.jzo2o.mall.common.exception.ServiceException;
import com.jzo2o.mall.product.mapper.CategoryMapper;
import com.jzo2o.mall.product.model.domain.Category;
import com.jzo2o.mall.product.model.dto.CategoryDTO;
import com.jzo2o.mall.product.model.dto.CategorySearchParamsDTO;
import com.jzo2o.mall.product.service.CategoryBrandService;
import com.jzo2o.mall.product.service.CategoryParameterGroupService;
import com.jzo2o.mall.product.service.CategoryService;
import com.jzo2o.mall.product.service.CategorySpecificationService;
import com.jzo2o.redis.helper.Cache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;


/**
 * 商品分类业务层实现
 *
 * @author pikachu
 * @since 2020-02-23 15:18:56
 */
@Service
@CacheConfig(cacheNames = "{CATEGORY}")
@Slf4j
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    private static final String DELETE_FLAG_COLUMN = "delete_flag";
    /**
     * 缓存
     */
    @Autowired
    private Cache cache;

    @Autowired
    private CategoryBrandService categoryBrandService;

    @Autowired
    private CategoryParameterGroupService categoryParameterGroupService;

    @Autowired
    private CategorySpecificationService categorySpecificationService;

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    /**
     * rocketMq
     */
//    @Autowired
//    private RocketMQTemplate rocketMQTemplate;
    /**
     * rocketMq配置
     */
//    @Autowired
//    private RocketmqCustomProperties rocketmqCustomProperties;

    @Override
    public List<Category> dbList(String parentId) {
        return this.list(new LambdaQueryWrapper<Category>().eq(Category::getParentId, parentId));
    }

    @Override
    @Cacheable(key = "#id")
    public Category getCategoryById(String id) {
        return this.getById(id);
    }

    /**
     * 根据分类id集合获取所有分类根据层级排序
     *
     * @param ids 分类ID集合
     * @return 商品分类列表
     */
    @Override
    public List<Category> listByIdsOrderByLevel(List<String> ids) {
        return this.list(new LambdaQueryWrapper<Category>().in(Category::getId, ids).orderByAsc(Category::getLevel));
    }

    @Override
    public List<Map<String, Object>> listMapsByIdsOrderByLevel(List<String> ids, String columns) {
        QueryWrapper<Category> queryWrapper = new QueryWrapper<>();
        queryWrapper.select(columns);
        queryWrapper.in("id", ids).orderByAsc("level");
        return this.listMaps(queryWrapper);
    }

    @Override
    public List<CategoryDTO> categoryTree() {
        List<CategoryDTO> categoryVOList = (List<CategoryDTO>) cache.get(CachePrefix.CATEGORY.getPrefix());
        if (categoryVOList != null) {
            return categoryVOList;
        }

        //获取全部分类
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Category::getDeleteFlag, false);
        List<Category> list = this.list(queryWrapper);

        //构造分类树
        categoryVOList = new ArrayList<>();
        for (Category category : list) {
            if ("0".equals(category.getParentId())) {
                CategoryDTO categoryDTO = new CategoryDTO(category);
                categoryDTO.setChildren(findChildren(list, categoryDTO));
                categoryVOList.add(categoryDTO);
            }
        }
        categoryVOList.sort(Comparator.comparing(Category::getSortOrder));
        if (!categoryVOList.isEmpty()) {
            cache.put(CachePrefix.CATEGORY.getPrefix(), categoryVOList);
            cache.put(CachePrefix.CATEGORY_ARRAY.getPrefix(), list);
        }
        return categoryVOList;
    }

    @Override
    public List<CategoryDTO> getStoreCategory(String[] categories) {
        List<String> arr = Arrays.asList(categories.clone());
        return categoryTree().stream()
                .filter(item -> arr.contains(item.getId())).collect(Collectors.toList());
    }

    @Override
    public List<Category> firstCategory() {
        QueryWrapper<Category> queryWrapper = Wrappers.query();
        queryWrapper.eq("level", 0);
        return list(queryWrapper);
    }

    @Override
    public List<CategoryDTO> listAllChildren(String parentId) {
        if ("0".equals(parentId)) {
            return categoryTree();
        }
        //循环代码，找到对象，把他的子分类返回
        List<CategoryDTO> topCatList = categoryTree();
        for (CategoryDTO item : topCatList) {
            if (item.getId().equals(parentId)) {
                return item.getChildren();
            } else {
                return getChildren(parentId, item.getChildren());
            }
        }
        return new ArrayList<>();
    }

    @Override
    public List<CategoryDTO> listAllChildren(CategorySearchParamsDTO categorySearchParams) {

        // 1. 查询所有分类数据
        List<Category> categories = this.list(categorySearchParams.queryWrapper());
        // 2. 找到所有根节点（parentId = "0"）
        List<Category> rootCategories = categories.stream()
                .filter(item -> "0".equals(item.getParentId()))
                .collect(Collectors.toList());
        return rootCategories.stream()
                .map(root -> {
                    CategoryDTO rootDTO = new CategoryDTO(root);
                    rootDTO.setChildren(findChildren(categories, rootDTO));
                    return rootDTO;
                })
                .sorted(Comparator.comparing(CategoryDTO::getSortOrder))
                .collect(Collectors.toList());
    }

    /**
     * 获取指定分类的分类名称
     *
     * @param ids 指定分类id集合
     * @return 分类名称集合
     */
    @Override
    public List<String> getCategoryNameByIds(List<String> ids) {
        List<String> categoryName = new ArrayList<>();
        List<Category> categoryVOList = (List<Category>) cache.get(CachePrefix.CATEGORY_ARRAY.getPrefix());
        //如果缓存中为空，则重新获取缓存
        if (categoryVOList == null) {
            categoryTree();
            categoryVOList = (List<Category>) cache.get(CachePrefix.CATEGORY_ARRAY.getPrefix());
        }
        //还为空的话，直接返回
        if (categoryVOList == null) {
            return new ArrayList<>();
        }
        //循环顶级分类
        for (Category category : categoryVOList) {
            //循环查询的id匹配
            for (String id : ids) {
                if (category.getId().equals(id)) {
                    //写入商品分类
                    categoryName.add(category.getName());
                }
            }
        }
        return categoryName;
    }

    @Override
    public List<Category> findByAllBySortOrder(Category category) {
        QueryWrapper<Category> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(category.getLevel() != null, "level", category.getLevel())
                .eq(CharSequenceUtil.isNotBlank(category.getName()), "name", category.getName())
                .eq(category.getParentId() != null, "parent_id", category.getParentId())
                .ne(category.getId() != null, "id", category.getId())
                .eq(DELETE_FLAG_COLUMN, false)
                .orderByAsc("sort_order");
        return this.baseMapper.selectList(queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveCategory(Category category) {
        //判断分类佣金是否正确
        if (category.getCommissionRate() < 0) {
            throw new ServiceException(ResultCode.CATEGORY_COMMISSION_RATE_ERROR);
        }
        //子分类与父分类的状态一致
        if (category.getParentId() != null && !("0").equals(category.getParentId())) {
            Category parentCategory = this.getById(category.getParentId());
            category.setDeleteFlag(parentCategory.getDeleteFlag());
        }
        this.save(category);
        removeCache();
        return true;
    }

    @Override
    @CacheEvict(key = "#category.id")
    @Transactional(rollbackFor = Exception.class)
    public void updateCategory(Category category) {
        //判断分类佣金是否正确
        if (category.getCommissionRate() < 0) {
            throw new ServiceException(ResultCode.CATEGORY_COMMISSION_RATE_ERROR);
        }
        //判断父分类与子分类的状态是否一致
        if (category.getParentId() != null && !"0".equals(category.getParentId())) {
            Category parentCategory = this.getById(category.getParentId());
            if (!parentCategory.getDeleteFlag().equals(category.getDeleteFlag())) {
                throw new ServiceException(ResultCode.CATEGORY_DELETE_FLAG_ERROR);
            }
        }
        UpdateWrapper<Category> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", category.getId());
        this.baseMapper.update(category, updateWrapper);
        removeCache();
//        applicationEventPublisher.publishEvent(new TransactionCommitSendMQEvent("同步商品分类名称",
//                rocketmqCustomProperties.getGoodsTopic(), GoodsTagsEnum.CATEGORY_GOODS_NAME.name(), category.getId()));
    }


    @Override
    @CacheEvict(key = "#id")
    @Transactional(rollbackFor = Exception.class)
    public void delete(String id) {
        this.removeById(id);
        removeCache();
        //删除关联关系
        categoryBrandService.deleteByCategoryId(id);
        categoryParameterGroupService.deleteByCategoryId(id);
        categorySpecificationService.deleteByCategoryId(id);
    }

    @Override
    @CacheEvict(key = "#categoryId")
    @Transactional(rollbackFor = Exception.class)
    public void updateCategoryStatus(String categoryId, Boolean enableOperations) {
        //禁用子分类
        CategoryDTO categoryDTO = new CategoryDTO(this.getById(categoryId));
        List<String> ids = new ArrayList<>();
        ids.add(categoryDTO.getId());
        this.findAllChild(categoryDTO);
        this.findAllChildIds(categoryDTO, ids);
        LambdaUpdateWrapper<Category> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.in(Category::getId, ids);
        updateWrapper.set(Category::getDeleteFlag, enableOperations);
        this.update(updateWrapper);
        removeCache();
    }

    /**
     * 递归树形VO
     *
     * @param categories 分类列表
     * @param categoryDTO 分类VO
     * @return 分类VO列表
     */
    private List<CategoryDTO> findChildren(List<Category> categories, CategoryDTO categoryDTO) {
        List<CategoryDTO> children = new ArrayList<>();
        categories.forEach(item -> {
            if (item.getParentId().equals(categoryDTO.getId())) {
                CategoryDTO temp = new CategoryDTO(item);
                temp.setChildren(findChildren(categories, temp));
                children.add(temp);
            }
        });

        return children;
    }

    /**
     * 条件查询分类
     *
     * @param category 分类VO
     */
    private void findAllChild(CategoryDTO category) {
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Category::getParentId, category.getId());
        List<Category> categories = this.list(queryWrapper);
        List<CategoryDTO> categoryVOList = new ArrayList<>();
        for (Category category1 : categories) {
            categoryVOList.add(new CategoryDTO(category1));
        }
        category.setChildren(categoryVOList);
        if (!categoryVOList.isEmpty()) {
            categoryVOList.forEach(this::findAllChild);
        }
    }

    /**
     * 获取所有的子分类ID
     *
     * @param category 分类
     * @param ids      ID列表
     */
    private void findAllChildIds(CategoryDTO category, List<String> ids) {
        if (category.getChildren() != null && !category.getChildren().isEmpty()) {
            for (CategoryDTO child : category.getChildren()) {
                ids.add(child.getId());
                this.findAllChildIds(child, ids);
            }
        }
    }

    /**
     * 递归自身，找到id等于parentId的对象，获取他的children 返回
     *
     * @param parentId       父ID
     * @param categoryVOList 分类VO
     * @return 子分类列表VO
     */
    private List<CategoryDTO> getChildren(String parentId, List<CategoryDTO> categoryVOList) {
        for (CategoryDTO item : categoryVOList) {
            if (item.getId().equals(parentId)) {
                return item.getChildren();
            }
            if (item.getChildren() != null && !item.getChildren().isEmpty()) {
                return getChildren(parentId, item.getChildren());
            }
        }
        return categoryVOList;
    }

    /**
     * 清除缓存
     */
    private void removeCache() {
        cache.remove(CachePrefix.CATEGORY.getPrefix());
        cache.remove(CachePrefix.CATEGORY_ARRAY.getPrefix());
    }
}