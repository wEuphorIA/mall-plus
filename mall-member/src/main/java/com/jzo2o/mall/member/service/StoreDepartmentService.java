package com.jzo2o.mall.member.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jzo2o.mall.member.model.domain.StoreDepartment;
import com.jzo2o.mall.member.model.dto.StoreDepartmentDTO;

import java.util.List;

/**
 * 店铺部门业务层
 */
public interface StoreDepartmentService extends IService<StoreDepartment> {

    /**
     * 获取部门树
     *
     * @param initWrapper
     * @return
     */
    List<StoreDepartmentDTO> tree(QueryWrapper<StoreDepartment> initWrapper);

    /**
     * 删除部门
     *
     * @param ids
     */
    void deleteByIds(List<String> ids);

    /**
     * 更新店铺部门
     *
     * @param storeDepartment 店铺部门
     * @return
     */
    Boolean update(StoreDepartment storeDepartment);
}