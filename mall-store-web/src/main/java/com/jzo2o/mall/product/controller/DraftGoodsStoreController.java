package com.jzo2o.mall.product.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jzo2o.common.utils.UserContext;
import com.jzo2o.mall.common.enums.ResultCode;
import com.jzo2o.mall.common.enums.ResultUtil;
import com.jzo2o.mall.common.exception.ServiceException;
import com.jzo2o.mall.common.model.AuthUser;
import com.jzo2o.mall.common.model.ResultMessage;
import com.jzo2o.mall.product.model.domain.DraftGoods;
import com.jzo2o.mall.product.model.dto.DraftGoodsDTO;
import com.jzo2o.mall.product.model.dto.DraftGoodsSearchParamsDTO;
import com.jzo2o.mall.product.model.dto.DraftGoodsViewDTO;
import com.jzo2o.mall.product.service.DraftGoodsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

/**
 * 店铺端,商品模板接口
 */
@RestController
@Api(tags = "店铺端,草稿商品接口")
@RequestMapping("/goods/draftGoods")
public class DraftGoodsStoreController {
    @Autowired
    private DraftGoodsService draftGoodsService;


    @ApiOperation(value = "分页获取草稿商品列表")
    @GetMapping(value = "/page")
    public IPage<DraftGoods> getDraftGoodsByPage(DraftGoodsSearchParamsDTO searchParams) {
        AuthUser authUser = UserContext.getCurrentUser();
        String storeId = authUser.getStoreId();
        searchParams.setStoreId(storeId);
        IPage<DraftGoods> draftGoods = draftGoodsService.getDraftGoods(searchParams);
        return draftGoods;
    }

    @ApiOperation(value = "获取草稿商品")
    @GetMapping(value = "/{id}")
    public DraftGoodsViewDTO getDraftGoods(@PathVariable String id) {
        DraftGoodsViewDTO draftGoodsViewDTO = draftGoodsService.getDraftGoods(id);
        return draftGoodsViewDTO;
    }

    @ApiOperation(value = "保存草稿商品")
    @PostMapping(value = "/save", consumes = "application/json", produces = "application/json")
    public ResultMessage<String> saveDraftGoods(@RequestBody DraftGoodsDTO draftGoodsVO) {
        AuthUser authUser = UserContext.getCurrentUser();
        String storeId = authUser.getStoreId();
        if (storeId == null) {
            draftGoodsVO.setStoreId(storeId);
        } else if (storeId != null && !storeId.equals(storeId)) {
            throw new ServiceException(ResultCode.USER_AUTHORITY_ERROR);
        }
        draftGoodsService.saveGoodsDraft(draftGoodsVO);
        return ResultUtil.success();
    }

    @ApiOperation(value = "删除草稿商品")
    @DeleteMapping(value = "/{id}")
    public void deleteDraftGoods(@PathVariable String id) {
        DraftGoodsViewDTO draftGoods = draftGoodsService.getDraftGoods(id);
        draftGoodsService.deleteGoodsDraft(id);
    }

}
