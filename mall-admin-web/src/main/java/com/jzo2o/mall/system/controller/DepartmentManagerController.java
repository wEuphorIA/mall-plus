package com.jzo2o.mall.system.controller;

import com.jzo2o.mall.system.model.domain.Department;
import com.jzo2o.mall.system.model.dto.DepartmentDTO;
import com.jzo2o.mall.system.service.DepartmentService;
import com.jzo2o.mysql.domain.SearchVO;
import com.jzo2o.mysql.utils.PageUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * 管理端,部门管理接口
 */
@RestController
@Api(tags = "管理端,部门管理接口")
@RequestMapping("/permission/department")
public class DepartmentManagerController {
    @Autowired
    private DepartmentService departmentService;

    @GetMapping(value = "/{id}")
    @ApiOperation(value = "查看部门详情")
    public Department get(@PathVariable String id) {
        Department department = departmentService.getById(id);
        return department;
    }

    @GetMapping
    @ApiOperation(value = "获取树状结构")
    public List<DepartmentDTO> getByPage(Department entity,
                                                        SearchVO searchVo) {
        List<DepartmentDTO> tree = departmentService.tree(PageUtils.initWrapper(entity, searchVo));
        return tree;

    }

    @PostMapping
    @ApiOperation(value = "新增部门")
    public Department save(@Validated Department department) {
        departmentService.save(department);
        return department;
    }

    @PutMapping("/{id}")
    @ApiOperation(value = "更新部门")
    public Department update(@PathVariable String id, @Validated Department department) {
        departmentService.updateById(department);
        return department;
    }

    @DeleteMapping(value = "/{ids}")
    @ApiOperation(value = "删除部门")
    public void delAllByIds(@PathVariable List<String> ids) {
        departmentService.deleteByIds(ids);
    }
}
