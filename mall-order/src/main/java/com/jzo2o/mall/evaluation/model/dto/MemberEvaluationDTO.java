package com.jzo2o.mall.evaluation.model.dto;

import cn.hutool.core.bean.BeanUtil;
import com.jzo2o.mall.evaluation.model.domain.MemberEvaluation;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 会员评价VO
 */
@Data
@NoArgsConstructor
public class MemberEvaluationDTO extends MemberEvaluation {

    private static final long serialVersionUID = 6696978796248845481L;

    @ApiModelProperty(value = "评论图片")
    private List<String> evaluationImages;

    @ApiModelProperty(value = "回复评论图片")
    private List<String> replyEvaluationImages;

    public MemberEvaluationDTO(MemberEvaluation memberEvaluation) {
        BeanUtil.copyProperties(memberEvaluation, this);
    }
}
