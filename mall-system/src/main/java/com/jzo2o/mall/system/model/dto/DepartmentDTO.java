package com.jzo2o.mall.system.model.dto;

import cn.hutool.core.bean.BeanUtil;
import com.jzo2o.mall.system.model.domain.Department;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 部门VO
 */
@Data
public class DepartmentDTO extends Department {

    private List<DepartmentDTO> children = new ArrayList<>();

    public DepartmentDTO() {
    }

    public DepartmentDTO(Department department) {
        BeanUtil.copyProperties(department, this);
    }
}
