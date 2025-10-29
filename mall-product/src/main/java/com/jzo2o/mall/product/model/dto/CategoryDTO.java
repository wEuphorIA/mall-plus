package com.jzo2o.mall.product.model.dto;

import cn.hutool.core.bean.BeanUtil;
import com.jzo2o.mall.product.model.domain.Brand;
import com.jzo2o.mall.product.model.domain.Category;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * 分类VO
 *
 * @author paulG
 * @since 2020/12/1
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDTO extends Category {

    private static final long serialVersionUID = 3775766246075838410L;

    @ApiModelProperty(value = "父节点名称")
    private String parentTitle;

    @ApiModelProperty("子分类列表")
    private List<CategoryDTO> children;

    @ApiModelProperty("分类关联的品牌列表")
    private List<Brand> brandList;

    public CategoryDTO(Category category) {
        BeanUtil.copyProperties(category, this);
    }

    public CategoryDTO(String id, String createBy, LocalDateTime createTime, String updateBy, LocalDateTime updateTime, Boolean deleteFlag, String name, String parentId, Integer level, BigDecimal sortOrder, Double commissionRate, String image, Boolean supportChannel) {
        super(id, createBy, createTime, updateBy, updateTime, deleteFlag, name, parentId, level, sortOrder, commissionRate, image, supportChannel);
    }

    public List<CategoryDTO> getChildren() {

        if (children != null) {
            children.sort(new Comparator<CategoryDTO>() {
                @Override
                public int compare(CategoryDTO o1, CategoryDTO o2) {
                    return o1.getSortOrder().compareTo(o2.getSortOrder());
                }
            });
            return children;
        }
        return null;
    }
}
