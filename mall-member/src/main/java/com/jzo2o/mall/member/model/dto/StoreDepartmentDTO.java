package com.jzo2o.mall.member.model.dto;

import cn.hutool.core.bean.BeanUtil;
import com.jzo2o.mall.member.model.domain.StoreDepartment;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 部门VO
 */
@Data
public class StoreDepartmentDTO extends StoreDepartment {

    private List<StoreDepartmentDTO> children = new ArrayList<>();

    public StoreDepartmentDTO() {
    }

    public StoreDepartmentDTO(StoreDepartment storeDepartment) {
        BeanUtil.copyProperties(storeDepartment, this);
    }
}
