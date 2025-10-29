package com.jzo2o.mall.member.controller;

import com.jzo2o.common.utils.UserContext;
import com.jzo2o.mall.common.enums.ResultUtil;
import com.jzo2o.mall.common.model.AuthUser;
import com.jzo2o.mall.common.model.ResultMessage;
import com.jzo2o.mall.member.model.dto.FreightTemplateDTO;
import com.jzo2o.mall.member.service.FreightTemplateService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Objects;

/**
 * 店铺端,运费模板接口
 **/
@RestController
@Api(tags = "店铺端,运费模板接口")
@RequestMapping("/setting/freightTemplate")
public class FreightTemplateStoreController {

    @Autowired
    private FreightTemplateService freightTemplateService;

    @ApiOperation(value = "商家运费模板列表")
    @GetMapping
    public List<FreightTemplateDTO> list() {
        AuthUser tokenUser =  UserContext.getCurrentUser();
        String storeId = tokenUser.getStoreId();
        List<FreightTemplateDTO> freightTemplateList = freightTemplateService.getFreightTemplateList(storeId);
        return freightTemplateList;
    }

    @ApiOperation(value = "获取商家运费模板详情")
    @ApiImplicitParam(name = "id", value = "商家模板ID", required = true, paramType = "path")
    @GetMapping("/{id}")
    public FreightTemplateDTO list(@PathVariable String id) {
        FreightTemplateDTO freightTemplate = freightTemplateService.getFreightTemplate(id);
        return freightTemplate;
    }

    @ApiOperation(value = "添加商家运费模板")
    @PostMapping
    public FreightTemplateDTO add(@Valid @RequestBody FreightTemplateDTO freightTemplateVO) {
        AuthUser tokenUser =  UserContext.getCurrentUser();
        String storeId = tokenUser.getStoreId();
        freightTemplateVO.setStoreId(storeId);
        FreightTemplateDTO freightTemplateDTO = freightTemplateService.addFreightTemplate(freightTemplateVO);
        return freightTemplateDTO;
    }

    @ApiOperation(value = "修改商家运费模板")
    @PutMapping("/{id}")
    public FreightTemplateDTO edit(@PathVariable String id, @RequestBody @Valid FreightTemplateDTO freightTemplateDTO) {
        FreightTemplateDTO freightTemplateDTO1 = freightTemplateService.editFreightTemplate(freightTemplateDTO);
        return freightTemplateDTO1;
    }

    @ApiOperation(value = "删除商家运费模板")
    @ApiImplicitParam(name = "id", value = "商家模板ID", required = true, paramType = "path")
    @DeleteMapping("/{id}")
    public void edit(@PathVariable String id) {
        freightTemplateService.removeFreightTemplate(id);
    }
}
