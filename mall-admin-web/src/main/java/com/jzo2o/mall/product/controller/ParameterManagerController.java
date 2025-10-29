package com.jzo2o.mall.product.controller;

import com.jzo2o.mall.common.enums.ResultCode;
import com.jzo2o.mall.common.exception.ServiceException;
import com.jzo2o.mall.product.model.domain.Parameters;
import com.jzo2o.mall.product.service.ParametersService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 管理端,分类绑定参数组管理接口
 */
@RestController
@Api(tags = "管理端,分类绑定参数组管理接口")
@RequestMapping("/goods/parameters")
public class ParameterManagerController {

    @Autowired
    private ParametersService parametersService;


    @ApiOperation(value = "添加参数")
    @PostMapping
    public Parameters save(@Valid Parameters parameters) {

        if (parametersService.save(parameters)) {
            return parameters;
        }
        throw new ServiceException(ResultCode.PARAMETER_SAVE_ERROR);

    }

    @ApiOperation(value = "编辑参数")
    @PutMapping
    public Parameters update(@Valid Parameters parameters) {

        if (parametersService.updateParameter(parameters)) {
            return parameters;
        }
        throw new ServiceException(ResultCode.PARAMETER_UPDATE_ERROR);
    }

    @ApiOperation(value = "通过id删除参数")
    @ApiImplicitParam(name = "id", value = "参数ID", required = true, paramType = "path")
    @DeleteMapping(value = "/{id}")
    public void delById(@PathVariable String id) {
        parametersService.removeById(id);

    }

}
