package com.jzo2o.mall.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jzo2o.mall.product.model.domain.CategoryBrand;
import com.jzo2o.mall.product.model.dto.CategoryBrandDTO;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 商品分类品牌数据处理层
 */
public interface CategoryBrandMapper extends BaseMapper<CategoryBrand> {

    /**
     * 根据分类id查分类绑定品牌
     *
     * @param categoryId 分类id
     * @return 分类绑定的品牌列表
     */
    @Select("SELECT b.id,b.name FROM pms_brand b INNER join pms_category_brand cb on b.id = cb.brand_id and cb.category_id = #{categoryId} where b.delete_flag = 0")
    List<CategoryBrandDTO> getCategoryBrandList(String categoryId);
}