package com.jzo2o.mall.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jzo2o.mall.product.model.domain.CategorySpecification;
import com.jzo2o.mall.product.model.domain.Specification;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 商品分类规格数据处理层
 */
public interface CategorySpecificationMapper extends BaseMapper<CategorySpecification> {
    /**
     * 根据分类id查分类绑定规格
     *
     * @param categoryId 分类id
     * @return 分类绑定规格列表
     */
    @Select("select s.* from  pms_specification s INNER join pms_category_specification cs " +
            "on s.id = cs.specification_id and cs.category_id = #{categoryId} ")
    List<Specification> getCategorySpecList(String categoryId);
}