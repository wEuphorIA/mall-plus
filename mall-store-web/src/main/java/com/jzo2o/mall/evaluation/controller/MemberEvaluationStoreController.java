package com.jzo2o.mall.evaluation.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jzo2o.common.utils.UserContext;
import com.jzo2o.mall.common.enums.ResultUtil;
import com.jzo2o.mall.common.model.AuthUser;
import com.jzo2o.mall.common.model.ResultMessage;
import com.jzo2o.mall.evaluation.model.dto.EvaluationQueryParamsDTO;
import com.jzo2o.mall.evaluation.model.dto.MemberEvaluationDTO;
import com.jzo2o.mall.evaluation.model.dto.MemberEvaluationListDTO;
import com.jzo2o.mall.evaluation.service.MemberEvaluationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

/**
 * 店铺端,商品评价管理接口
 */
@RestController
@Api(tags = "店铺端,商品评价管理接口")
@RequestMapping("/member/evaluation")
public class MemberEvaluationStoreController {

    @Autowired
    private MemberEvaluationService memberEvaluationService;

    @ApiOperation(value = "分页获取会员评论列表")
    @GetMapping
    public IPage<MemberEvaluationListDTO> getByPage(EvaluationQueryParamsDTO evaluationQueryParams) {
        AuthUser authUser = UserContext.getCurrentUser();
        String storeId = Objects.requireNonNull(authUser).getStoreId();
        evaluationQueryParams.setStoreId(storeId);
        IPage<MemberEvaluationListDTO> memberEvaluationListDTOIPage = memberEvaluationService.queryPage(evaluationQueryParams);
        return memberEvaluationListDTOIPage;
    }

    @ApiOperation(value = "通过id获取")
    @ApiImplicitParam(name = "id", value = "评价ID", required = true, dataType = "String", paramType = "path")
    @GetMapping(value = "/get/{id}")
    public MemberEvaluationDTO get(@PathVariable String id) {
        MemberEvaluationDTO memberEvaluationDTO = memberEvaluationService.queryById(id);
        return memberEvaluationDTO;
    }

    @ApiOperation(value = "回复评价")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "评价ID", required = true, dataType = "String", paramType = "path"),
            @ApiImplicitParam(name = "reply", value = "回复内容", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "replyImage", value = "回复图片", dataType = "String", paramType = "query")
    })
    @PutMapping(value = "/reply/{id}")
    public void reply(@PathVariable String id, @RequestParam String reply, @RequestParam String replyImage) {
//        OperationalJudgment.judgment(memberEvaluationService.queryById(id));
        memberEvaluationService.reply(id, reply, replyImage);
    }
}
