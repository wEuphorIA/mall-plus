package com.jzo2o.mall.system.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jzo2o.mall.system.model.domain.Department;
import com.jzo2o.mall.system.model.dto.DepartmentDTO;

import java.util.List;

/**
 * 部门业务层
 */
public interface DepartmentService extends IService<Department> {

    /**
     * 获取部门树
     *
     * @param initWrapper
     * @return
     */
    List<DepartmentDTO> tree(QueryWrapper<Department> initWrapper);

    /**
     * 删除部门
     *
     * @param ids
     */
    void deleteByIds(List<String> ids);
}