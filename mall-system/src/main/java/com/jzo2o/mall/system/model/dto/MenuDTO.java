package com.jzo2o.mall.system.model.dto;

import cn.hutool.core.bean.BeanUtil;
import com.jzo2o.mall.system.model.domain.Menu;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 菜单VO 展示模型
 */

@Data
public class MenuDTO extends Menu {

    @ApiModelProperty(value = "子菜单")
    private List<MenuDTO> children = new ArrayList<>();

    public MenuDTO() {

    }

    public MenuDTO(Menu menu) {
        BeanUtil.copyProperties(menu, this);
    }

    public List<MenuDTO> getChildren() {
        if (children != null) {
            children.sort(new Comparator<MenuDTO>() {
                @Override
                public int compare(MenuDTO o1, MenuDTO o2) {
                    return o1.getSortOrder().compareTo(o2.getSortOrder());
                }
            });
            return children;
        }
        return null;
    }
}
