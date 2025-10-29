package com.jzo2o.mall.product.controller;


import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.PageUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jzo2o.mall.product.model.domain.Specification;
import com.jzo2o.mall.product.service.SpecificationService;
import com.jzo2o.mysql.domain.PageVO;
import com.jzo2o.mysql.utils.PageUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;


/**
 * 管理端,商品规格接口
 */
@RestController
@Api(tags = "管理端,商品规格接口")
@RequestMapping("/goods/spec")
public class SpecificationManagerController {

    @Autowired
    private SpecificationService specificationService;


    @GetMapping("/all")
    @ApiOperation(value = "获取所有可用规格")
    public List<Specification> getAll() {
        List<Specification> list = specificationService.list();
        return list;
    }

    @GetMapping
    @ApiOperation(value = "搜索规格")
    public Page<Specification> page(String specName, PageVO page) {
        LambdaQueryWrapper<Specification> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.like(CharSequenceUtil.isNotEmpty(specName), Specification::getSpecName, specName);
        Page<Specification> page1 = specificationService.page(PageUtils.initPage(page), lambdaQueryWrapper);
        return page1;
    }

    @PostMapping
    @ApiOperation(value = "保存规格")
    public void save(@Valid Specification specification) {
        specificationService.save(specification);
    }

    @PutMapping("/{id}")
    @ApiOperation(value = "更改规格")
    public boolean update(@Valid Specification specification, @PathVariable String id) {
        specification.setId(id);
        boolean b = specificationService.saveOrUpdate(specification);
        return b;
    }

    @DeleteMapping("/{ids}")
    @ApiImplicitParam(name = "ids", value = "规格ID", required = true, dataType = "String", allowMultiple = true, paramType = "path")
    @ApiOperation(value = "批量删除")
    public boolean delAllByIds(@PathVariable List<String> ids) {
        boolean b = specificationService.deleteSpecification(ids);
        return b;
    }
}
