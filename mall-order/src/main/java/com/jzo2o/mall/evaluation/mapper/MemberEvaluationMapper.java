package com.jzo2o.mall.evaluation.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.jzo2o.mall.evaluation.model.domain.MemberEvaluation;
import com.jzo2o.mall.evaluation.model.dto.MemberEvaluationListDTO;
import com.jzo2o.mall.evaluation.model.dto.StoreRatingDTO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 会员商品评价数据处理层
 */
public interface MemberEvaluationMapper extends BaseMapper<MemberEvaluation> {


    /**
     * 会员评价分页
     *
     * @param page         分页
     * @param queryWrapper 查询条件
     * @return 会员评价分页
     */
    @Select("select me.* from oms_member_evaluation as me ${ew.customSqlSegment}")
    IPage<MemberEvaluationListDTO> getMemberEvaluationList(IPage<MemberEvaluationListDTO> page, @Param(Constants.WRAPPER) Wrapper<MemberEvaluationListDTO> queryWrapper);

    /**
     * 评价数量
     *
     * @param goodsId 商品ID
     * @return 会员评价
     */
    @Select("select grade,count(1) as num from oms_member_evaluation Where goods_id=#{goodsId} and status='OPEN' and delete_flag = false GROUP BY grade")
    List<Map<String, Object>> getEvaluationNumber(String goodsId);

    /**
     * 获取店铺评分
     *
     * @param queryWrapper 查询条件
     * @return 店铺评分
     */
    @Select("SELECT round( AVG( delivery_score ), 2 ) AS delivery_score" +
            ",round( AVG( description_score ), 2 ) AS description_score" +
            ",round( AVG( service_score ), 2 ) AS service_score " +
            "FROM oms_member_evaluation ${ew.customSqlSegment}")
    StoreRatingDTO getStoreRatingVO(@Param(Constants.WRAPPER) Wrapper<MemberEvaluation> queryWrapper);

    /**
     * 商品会员评价数量
     *
     * @param queryWrapper 查询条件
     * @return 评价数量
     */
    @Select("SELECT goods_id,COUNT(goods_id) AS num FROM oms_member_evaluation GROUP BY goods_id")
    List<Map<String, Object>> memberEvaluationNum(@Param(Constants.WRAPPER) Wrapper<MemberEvaluation> queryWrapper);
}