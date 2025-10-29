package com.jzo2o.mall.system.model.dto;

import cn.hutool.core.bean.BeanUtil;
import com.jzo2o.common.utils.BeanUtils;
import com.jzo2o.mall.system.model.domain.Region;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 地区VO
 */
@Data
@NoArgsConstructor
public class RegionDTO extends Region {

    /**
     * 子信息
     */
    private List<RegionDTO> children;

    public RegionDTO(Region region) {
        BeanUtil.copyProperties(region, this);
        this.children = new ArrayList<>();
    }
}
