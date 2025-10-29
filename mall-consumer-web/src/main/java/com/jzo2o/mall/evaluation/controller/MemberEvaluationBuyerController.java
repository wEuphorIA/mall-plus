package com.jzo2o.mall.evaluation.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jzo2o.common.utils.UserContext;
import com.jzo2o.mall.common.enums.ResultUtil;
import com.jzo2o.mall.common.enums.SwitchEnum;
import com.jzo2o.mall.common.model.AuthUser;
import com.jzo2o.mall.common.model.ResultMessage;
import com.jzo2o.mall.evaluation.model.domain.MemberEvaluation;
import com.jzo2o.mall.evaluation.model.dto.EvaluationNumberDTO;
import com.jzo2o.mall.evaluation.model.dto.EvaluationQueryParamsDTO;
import com.jzo2o.mall.evaluation.model.dto.MemberEvaluationAddDTO;
import com.jzo2o.mall.evaluation.model.dto.MemberEvaluationDTO;
import com.jzo2o.mall.evaluation.service.MemberEvaluationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * 买家端,会员商品评价接口
 */
@RestController
@Api(tags = "买家端,会员商品评价接口")
@RequestMapping("/member/evaluation")
public class MemberEvaluationBuyerController {

    /**
     * 会员商品评价
     */
    @Autowired
    private MemberEvaluationService memberEvaluationService;

    @ApiOperation(value = "添加会员评价")
    @PostMapping
    public MemberEvaluationAddDTO save(@Valid MemberEvaluationAddDTO memberEvaluationDTO) {
        MemberEvaluationAddDTO memberEvaluationAddDTO = memberEvaluationService.addMemberEvaluation(memberEvaluationDTO, true);
        return memberEvaluationAddDTO;
    }

    @ApiOperation(value = "查看会员评价详情")
    @ApiImplicitParam(name = "id", value = "评价ID", required = true, paramType = "path")
    @GetMapping(value = "/get/{id}")
    public MemberEvaluationDTO get(@NotNull(message = "评价ID不能为空") @PathVariable("id") String id) {
        MemberEvaluationDTO memberEvaluationDTO = memberEvaluationService.queryById(id);
        return memberEvaluationDTO;

    }

    @ApiOperation(value = "查看当前会员评价列表")
    @GetMapping
    public IPage<MemberEvaluation> queryMineEvaluation(EvaluationQueryParamsDTO evaluationQueryParams) {
        //设置当前登录会员
        AuthUser authUser = UserContext.getCurrentUser();
        evaluationQueryParams.setMemberId(authUser.getIdString());
        IPage<MemberEvaluation> memberEvaluationIPage = memberEvaluationService.managerQuery(evaluationQueryParams);
        return memberEvaluationIPage;
    }

    @ApiOperation(value = "查看某一个商品的评价列表")
    @ApiImplicitParam(name = "goodsId", value = "商品ID", required = true, dataType = "Long", paramType = "path")
    @GetMapping(value = "/{goodsId}/goodsEvaluation")
    public IPage<MemberEvaluation> queryGoodsEvaluation(EvaluationQueryParamsDTO evaluationQueryParams,
                                                                       @NotNull @PathVariable("goodsId") String goodsId) {
        //设置查询查询商品
        evaluationQueryParams.setGoodsId(goodsId);
        evaluationQueryParams.setStatus(SwitchEnum.OPEN.name());
        IPage<MemberEvaluation> memberEvaluationIPage = memberEvaluationService.managerQuery(evaluationQueryParams);
        return memberEvaluationIPage;
    }

    @ApiOperation(value = "查看某一个商品的评价数量")
    @ApiImplicitParam(name = "goodsId", value = "商品ID", required = true, dataType = "Long", paramType = "path")
    @GetMapping(value = "/{goodsId}/evaluationNumber")
    public EvaluationNumberDTO queryEvaluationNumber(@NotNull @PathVariable("goodsId") String goodsId) {
        EvaluationNumberDTO evaluationNumber = memberEvaluationService.getEvaluationNumber(goodsId);
        return evaluationNumber;
    }
}
