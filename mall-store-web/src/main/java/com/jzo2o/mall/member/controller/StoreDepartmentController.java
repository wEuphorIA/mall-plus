package com.jzo2o.mall.member.controller;

import cn.hutool.core.util.PageUtil;
import com.jzo2o.common.model.CurrentUserInfo;
import com.jzo2o.common.utils.UserContext;
import com.jzo2o.mall.common.enums.ResultUtil;
import com.jzo2o.mall.common.model.AuthUser;
import com.jzo2o.mall.common.model.ResultMessage;
import com.jzo2o.mall.member.model.domain.StoreDepartment;
import com.jzo2o.mall.member.model.dto.StoreDepartmentDTO;
import com.jzo2o.mall.member.service.StoreDepartmentService;
import com.jzo2o.mysql.domain.SearchVO;
import com.jzo2o.mysql.utils.PageUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * 管理端,部门管理接口
 *
 * @author Chopper
 * @since 2020/11/22 12:06
 */
@RestController
@Api(tags = "店铺端,部门管理接口")
@RequestMapping("/department")
public class StoreDepartmentController {
    @Autowired
    private StoreDepartmentService storeDepartmentService;

    @GetMapping(value = "/{id}")
    @ApiOperation(value = "查看部门详情")
    public StoreDepartment get(@PathVariable String id) {
        StoreDepartment storeDepartment = storeDepartmentService.getById(id);
        return storeDepartment;
    }

    @GetMapping
    @ApiOperation(value = "获取树状结构")
    public List<StoreDepartmentDTO> getByPage(StoreDepartment entity,
                                                             SearchVO searchVo) {
        AuthUser tokenUser =  UserContext.getCurrentUser();
        entity.setStoreId(tokenUser.getStoreId());
        List<StoreDepartmentDTO> tree = storeDepartmentService.tree(PageUtils.initWrapper(entity, searchVo));
        return tree;

    }

    @PostMapping
    @ApiOperation(value = "新增部门")
    public StoreDepartment save(StoreDepartment storeDepartment) {
        AuthUser tokenUser =  UserContext.getCurrentUser();
        storeDepartment.setStoreId(tokenUser.getStoreId());
        storeDepartmentService.save(storeDepartment);
        return storeDepartment;
    }

    @PutMapping("/{id}")
    @ApiOperation(value = "更新部门")
    public StoreDepartment update(@PathVariable String id, StoreDepartment storeDepartment) {
        storeDepartment.setId(id);
        storeDepartmentService.update(storeDepartment);
        return storeDepartment;
    }

    @DeleteMapping(value = "/{ids}")
    @ApiOperation(value = "删除部门")
    public void delAllByIds(@PathVariable List<String> ids) {
        storeDepartmentService.deleteByIds(ids);
    }
}
