package com.jzo2o.mall.member.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jzo2o.common.utils.UserContext;
import com.jzo2o.mall.common.enums.ResultCode;
import com.jzo2o.mall.common.exception.ServiceException;
import com.jzo2o.mall.common.model.AuthUser;
import com.jzo2o.mall.member.mapper.StoreDepartmentMapper;
import com.jzo2o.mall.member.model.domain.Clerk;
import com.jzo2o.mall.member.model.domain.StoreDepartment;
import com.jzo2o.mall.member.model.dto.StoreDepartmentDTO;
import com.jzo2o.mall.member.service.ClerkService;
import com.jzo2o.mall.member.service.StoreDepartmentRoleService;
import com.jzo2o.mall.member.service.StoreDepartmentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 店铺部门业务层实现
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class StoreDepartmentServiceImpl extends ServiceImpl<StoreDepartmentMapper, StoreDepartment> implements StoreDepartmentService {

    @Autowired
    private StoreDepartmentRoleService storeDepartmentRoleService;

    @Autowired
    private ClerkService clerkService;

    @Override
    public void deleteByIds(List<String> ids) {
        AuthUser tokenUser =  UserContext.getCurrentUser();
        //校验是否有操作店铺部门权限
        List<StoreDepartment> storeDepartments = this.list(new QueryWrapper<StoreDepartment>()
                .in("id", ids)
                .eq("store_id", tokenUser.getStoreId()));
        if (storeDepartments.size() != ids.size()) {
            throw new ServiceException(ResultCode.USER_AUTHORITY_ERROR);
        }
        //校验店员是否绑定部门
        if (clerkService.count(new QueryWrapper<Clerk>().in("department_id", ids)) > 0) {
            throw new ServiceException(ResultCode.PERMISSION_DEPARTMENT_DELETE_ERROR);
        }
        //删除店铺部门
        this.removeByIds(ids);
        //删除店铺部门角色
        storeDepartmentRoleService.deleteByDepartment(ids);
    }

    @Override
    public List<StoreDepartmentDTO> tree(QueryWrapper<StoreDepartment> initWrapper) {
        try {
            List<StoreDepartment> departments = this.list(initWrapper);

            List<StoreDepartmentDTO> all = new ArrayList<>();
            departments.forEach(item -> all.add(new StoreDepartmentDTO(item)));

            List<StoreDepartmentDTO> tree = new ArrayList<>();
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
    private void initChild(StoreDepartmentDTO tree, List<StoreDepartmentDTO> departmentVOS) {
        departmentVOS.stream()
                .filter(item -> (item.getParentId().equals(tree.getId())))
                .forEach(child -> {
                    StoreDepartmentDTO childTree = new StoreDepartmentDTO(child);
                    initChild(childTree, departmentVOS);
                    tree.getChildren().add(childTree);
                });
    }

    @Override
    public Boolean update(StoreDepartment storeDepartment) {
        AuthUser tokenUser =  UserContext.getCurrentUser();
        StoreDepartment temp = this.getById(storeDepartment);
        //校验部门是否存在
        if (temp == null) {
            throw new ServiceException(ResultCode.PERMISSION_NOT_FOUND_ERROR);
        }
        //校验店铺权限
        if (!temp.getStoreId().equals(tokenUser.getStoreId())) {
            throw new ServiceException(ResultCode.USER_AUTHORITY_ERROR);
        }
        return this.updateById(storeDepartment);
    }
}