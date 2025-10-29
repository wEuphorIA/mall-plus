package com.jzo2o.mall.page.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jzo2o.mall.common.enums.PageEnum;
import com.jzo2o.mall.common.enums.ResultUtil;
import com.jzo2o.mall.common.model.ResultMessage;
import com.jzo2o.mall.system.model.domain.PageData;
import com.jzo2o.mall.system.model.dto.PageDataDTO;
import com.jzo2o.mall.system.service.PageDataService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 买家端,页面接口
 */
@RestController
@Api(tags = "买家端,页面接口")
@RequestMapping("/other/pageData")
public class PageBuyerController {

    /**
     * 页面管理
     */
    @Autowired
    private PageDataService pageService;

    @ApiOperation(value = "获取首页数据")
    @GetMapping("/getIndex")
    public PageDataDTO getIndex(@RequestParam String clientType) {
        PageDataDTO pageDataDTO = new PageDataDTO(PageEnum.INDEX.name());
        pageDataDTO.setPageClientType(clientType);
        PageDataDTO pageDataVO=pageService.getPageData(pageDataDTO);
        return pageDataVO;
    }

    @ApiOperation(value = "获取页面数据")
    @GetMapping
    public PageDataDTO get(PageDataDTO pageDataDTO) {
        PageDataDTO pageDataVO=pageService.getPageData(pageDataDTO);
        return pageDataVO;
    }

    @ApiOperation(value = "获取店铺首页")
    @GetMapping("/getStore")
    public PageDataDTO getShopPage(@RequestParam String clientType,String storeId) {
        PageDataDTO pageDataDTO = new PageDataDTO(PageEnum.STORE.name());
        pageDataDTO.setPageClientType(clientType);
        pageDataDTO.setNum(storeId);
        PageDataDTO pageDataVO=pageService.getPageData(pageDataDTO);
        return pageDataVO;
    }

    @ApiOperation(value = "获取页面数据")
    @ApiImplicitParam(name = "id", value = "id", required = true, dataType = "String", paramType = "path")
    @GetMapping("/get/{id}")
    public PageData getPage(@PathVariable("id") String id) {
        PageData special = pageService.getSpecial(id);
        return special;
    }

    @ApiOperation(value = "获取专题页面数据（根据消息内容得知）")
    @GetMapping("/getSpecial")
    public PageData getSpecial(@RequestParam String body) {
        String name = "";
        if (body.indexOf("』") >= 0 && body.indexOf("『") >= 0) {
            name = body.substring(body.indexOf("『") + 1, body.lastIndexOf("』"));
        } else if (body.indexOf("〉") >= 0 && body.indexOf("〈") >= 0) {
            name = body.substring(body.indexOf("〈") + 1, body.lastIndexOf("〉"));
        } else if (body.indexOf("」") >= 0 && body.indexOf("「") >= 0) {
            name = body.substring(body.indexOf("「") + 1, body.lastIndexOf("」"));
        } else if (body.indexOf("》") >= 0 && body.indexOf("《") >= 0) {
            name = body.substring(body.indexOf("《") + 1, body.lastIndexOf("》"));
        } else if (body.indexOf("）") >= 0 && body.indexOf("（") >= 0) {
            name = body.substring(body.indexOf("（") + 1, body.lastIndexOf("）"));
        } else if (body.indexOf("】") >= 0 && body.indexOf("【") >= 0) {
            name = body.substring(body.indexOf("【") + 1, body.lastIndexOf("】"));
        } else if (body.indexOf("｝") >= 0 && body.indexOf("｛") >= 0) {
            name = body.substring(body.indexOf("｛") + 1, body.lastIndexOf("｝"));
        } else if (body.indexOf("！") >= 0) {
            name = body.substring(body.indexOf("！") + 1, body.lastIndexOf("！"));
        } else if (body.indexOf("｜") >= 0) {
            name = body.substring(body.indexOf("｜") + 1, body.lastIndexOf("｜"));
        }

        PageData pageData = pageService.getOne(
                new LambdaQueryWrapper<PageData>()
                        .eq(PageData::getPageType, PageEnum.SPECIAL.name())
                        .eq(PageData::getName, name));
        return pageData;

    }
}
