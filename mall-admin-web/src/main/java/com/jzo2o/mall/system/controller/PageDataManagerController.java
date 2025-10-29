package com.jzo2o.mall.system.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jzo2o.mall.common.enums.ResultUtil;
import com.jzo2o.mall.common.model.ResultMessage;
import com.jzo2o.mall.system.model.domain.PageData;
import com.jzo2o.mall.system.model.dto.PageDataDTO;
import com.jzo2o.mall.system.model.dto.PageDataListDTO;
import com.jzo2o.mall.system.service.PageDataService;
import com.jzo2o.mysql.domain.PageVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * 管理端,页面设置管理接口
 */
@RestController
@Api(tags = "管理端,页面设置管理接口")
@RequestMapping("/other/pageData")
public class PageDataManagerController {

    @Autowired
    private PageDataService pageDataService;

    @ApiOperation(value = "获取页面信息")
    @ApiImplicitParam(name = "id", value = "页面ID", required = true, dataType = "String", paramType = "path")
    @GetMapping(value = "/{id}")
    public PageData getPageData(@PathVariable String id) {
        PageData pageData = pageDataService.getById(id);
        return pageData;
    }

    @ApiOperation(value = "添加页面")
    @PostMapping("/add")
    public PageData addPageData(@Valid PageData pageData) {
        PageData pageData1 = pageDataService.addPageData(pageData);
        return pageData1;
    }

    @ApiOperation(value = "修改页面")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "页面ID", required = true, dataType = "String", paramType = "path")
    })
    @PutMapping("/update/{id}")
    public ResultMessage<PageData> updatePageData(@Valid PageData pageData, @NotNull @PathVariable String id) {
        pageData.setId(id);
        return ResultUtil.data(pageDataService.updatePageData(pageData));
    }

    @ApiOperation(value = "页面列表")
    @GetMapping("/pageDataList")
    public IPage<PageDataListDTO> pageDataList(PageVO pageVO, PageDataDTO pageDataDTO) {
        IPage<PageDataListDTO> pageDataList = pageDataService.getPageDataList(pageVO, pageDataDTO);
        return pageDataList;
    }

    @ApiOperation(value = "发布页面")
    @ApiImplicitParam(name = "id", value = "页面ID", required = true, dataType = "String", paramType = "path")
    @PutMapping("/release/{id}")
    public PageData release(@PathVariable String id) {
        PageData pageData = pageDataService.releasePageData(id);
        return pageData;
    }

    @ApiOperation(value = "删除页面")
    @ApiImplicitParam(name = "id", value = "页面ID", required = true, dataType = "String", paramType = "path")
    @DeleteMapping("/remove/{id}")
    public boolean remove(@PathVariable String id) {
        boolean b = pageDataService.removePageData(id);
        return b;
    }
}
