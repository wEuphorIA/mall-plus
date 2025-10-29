package com.jzo2o.mall.product.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jzo2o.mall.common.enums.ResultCode;
import com.jzo2o.mall.common.exception.ServiceException;
import com.jzo2o.mall.product.model.domain.CategoryParameterGroup;
import com.jzo2o.mall.product.model.domain.Parameters;
import com.jzo2o.mall.product.model.dto.ParameterGroupDTO;
import com.jzo2o.mall.product.service.CategoryParameterGroupService;
import com.jzo2o.mall.product.service.ParametersService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理端,分类绑定参数组接口
 */
@RestController
@Api(tags = "管理端,分类绑定参数组接口")
@RequestMapping("/goods/categoryParameters")
public class CategoryParameterGroupManagerController {

    /**
     * 参数组
     */
    @Autowired
    private ParametersService parametersService;

    /**
     * 分类参数
     */
    @Autowired
    private CategoryParameterGroupService categoryParameterGroupService;

    @ApiOperation(value = "查询某分类下绑定的参数信息")
    @GetMapping(value = "/{categoryId}")
    @ApiImplicitParam(name = "categoryId",value = "分类id", required = true, dataType = "String", paramType = "path")
    public List<ParameterGroupDTO> getCategoryParam(@PathVariable String categoryId) {
        List<ParameterGroupDTO> categoryParams = categoryParameterGroupService.getCategoryParams(categoryId);
        return categoryParams;
    }

    @ApiOperation(value = "保存数据")
    @PostMapping
    public CategoryParameterGroup saveOrUpdate(@Validated CategoryParameterGroup categoryParameterGroup) {

        if (categoryParameterGroupService.save(categoryParameterGroup)) {
            return categoryParameterGroup;
        }
        throw new ServiceException(ResultCode.CATEGORY_PARAMETER_SAVE_ERROR);
    }

    @ApiOperation(value = "更新数据")
    @PutMapping
    public CategoryParameterGroup update(@Validated CategoryParameterGroup categoryParameterGroup) {

        if (categoryParameterGroupService.updateById(categoryParameterGroup)) {
            return categoryParameterGroup;
        }
        throw new ServiceException(ResultCode.CATEGORY_PARAMETER_UPDATE_ERROR);
    }

    @ApiOperation(value = "通过id删除参数组")
    @ApiImplicitParam(name = "id", value = "参数组ID", required = true, dataType = "String", paramType = "path")
    @DeleteMapping(value = "/{id}")
    public void delAllByIds(@PathVariable String id) {
        //删除参数
        parametersService.remove(new QueryWrapper<Parameters>().eq("group_id", id));
        //删除参数组
        categoryParameterGroupService.removeById(id);
    }

}
