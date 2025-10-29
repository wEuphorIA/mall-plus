package com.jzo2o.mall.member.model.dto;

import cn.hutool.core.bean.BeanUtil;
import com.jzo2o.mall.member.model.domain.StoreMenu;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 菜单VO 展示模型
 */

@Data
public class StoreMenuDTO extends StoreMenu {

    @ApiModelProperty(value = "子菜单")
    private List<StoreMenuDTO> children = new ArrayList<>();

    public StoreMenuDTO() {

    }

    public StoreMenuDTO(StoreMenu storeMenu) {
        BeanUtil.copyProperties(storeMenu, this);
    }

    public List<StoreMenuDTO> getChildren() {
        if (children != null) {
            children.sort(new Comparator<StoreMenuDTO>() {
                @Override
                public int compare(StoreMenuDTO o1, StoreMenuDTO o2) {
                    return o1.getSortOrder().compareTo(o2.getSortOrder());
                }
            });
            return children;
        }
        return null;
    }
}
