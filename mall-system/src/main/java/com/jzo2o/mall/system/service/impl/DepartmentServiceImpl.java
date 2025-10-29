package com.jzo2o.mall.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jzo2o.mall.common.enums.ResultCode;
import com.jzo2o.mall.common.exception.ServiceException;
import com.jzo2o.mall.system.mapper.DepartmentMapper;
import com.jzo2o.mall.system.model.domain.AdminUser;
import com.jzo2o.mall.system.model.domain.Department;
import com.jzo2o.mall.system.model.dto.DepartmentDTO;
import com.jzo2o.mall.system.service.AdminUserService;
import com.jzo2o.mall.system.service.DepartmentRoleService;
import com.jzo2o.mall.system.service.DepartmentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 部门业务层实现
 */
@Slf4j
@Service
public class DepartmentServiceImpl extends ServiceImpl<DepartmentMapper, Department> implements DepartmentService {

    /**
     * 管理员
     */
    @Autowired
    private AdminUserService adminUserService;
    /**
     * 部门角色
     */
    @Autowired
    private DepartmentRoleService departmentRoleService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByIds(List<String> ids) {
        QueryWrapper<AdminUser> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("department_id", ids);
        if (adminUserService.count(queryWrapper) > 0) {
            throw new ServiceException(ResultCode.PERMISSION_DEPARTMENT_DELETE_ERROR);
        }
        this.removeByIds(ids);
        departmentRoleService.deleteByDepartment(ids);
    }

    @Override
    public List<DepartmentDTO> tree(QueryWrapper<Department> initWrapper) {
        try {
            List<Department> departments = this.list(initWrapper);

            List<DepartmentDTO> all = new ArrayList<>();
            departments.forEach(item -> all.add(new DepartmentDTO(item)));

            List<DepartmentDTO> tree = new ArrayList<>();
            all.forEach(item -> {
                if ("0".equals(item.getParentId())) {
                    initChild(item, all);
                    tree.add(item);
                }
            });

            return tree;
        } catch (Exception e) {
            log.error("部门业务错误", e);
            return null;
        }
    }


    /**
     * 递归初始化子树
     *
     * @param tree          树结构
     * @param departmentVOS 数据库对象集合
     */
    private void initChild(DepartmentDTO tree, List<DepartmentDTO> departmentVOS) {
        departmentVOS.stream()
                .filter(item -> (item.getParentId().equals(tree.getId())))
                .forEach(child -> {
                    DepartmentDTO childTree = new DepartmentDTO(child);
                    initChild(childTree, departmentVOS);
                    tree.getChildren().add(childTree);
                });
    }


}