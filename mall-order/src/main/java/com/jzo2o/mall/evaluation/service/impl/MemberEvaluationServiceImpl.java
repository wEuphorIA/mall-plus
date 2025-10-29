package com.jzo2o.mall.evaluation.service.impl;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.PageUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jzo2o.common.model.CurrentUser;
import com.jzo2o.common.utils.UserContext;
import com.jzo2o.mall.common.enums.ResultCode;
import com.jzo2o.mall.common.enums.SwitchEnum;
import com.jzo2o.mall.common.enums.UserEnums;
import com.jzo2o.mall.common.exception.ServiceException;
import com.jzo2o.mall.common.model.AuthUser;
import com.jzo2o.mall.evaluation.mapper.MemberEvaluationMapper;
import com.jzo2o.mall.evaluation.model.domain.MemberEvaluation;
import com.jzo2o.mall.evaluation.model.dto.*;
import com.jzo2o.mall.evaluation.model.enums.EvaluationGradeEnum;
import com.jzo2o.mall.evaluation.service.MemberEvaluationService;
import com.jzo2o.mall.member.model.domain.Member;
import com.jzo2o.mall.member.service.MemberService;
import com.jzo2o.mall.order.model.domain.Order;
import com.jzo2o.mall.order.model.domain.OrderItem;
import com.jzo2o.mall.order.model.enums.CommentStatusEnum;
import com.jzo2o.mall.order.service.OrderItemService;
import com.jzo2o.mall.order.service.OrderService;
import com.jzo2o.mall.product.model.domain.GoodsSku;
import com.jzo2o.mall.product.service.GoodsSkuService;
import com.jzo2o.mysql.utils.PageUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * 会员商品评价业务层实现
 */
@Service
public class MemberEvaluationServiceImpl extends ServiceImpl<MemberEvaluationMapper, MemberEvaluation> implements MemberEvaluationService {

    /**
     * 订单
     */
    @Autowired
    private OrderService orderService;
    /**
     * 子订单
     */
    @Autowired
    private OrderItemService orderItemService;
    /**
     * 会员
     */
    @Autowired
    private MemberService memberService;
    /**
     * 商品
     */
    @Autowired
    private GoodsSkuService goodsSkuService;
//    /**
//     * rocketMq配置
//     */
//    @Autowired
//    private RocketmqCustomProperties rocketmqCustomProperties;

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    @Override
    public IPage<MemberEvaluation> managerQuery(EvaluationQueryParamsDTO queryParams) {
        //获取评价分页
        return this.page(PageUtils.initPage(queryParams), queryParams.queryWrapper());
    }

    @Override
    public IPage<MemberEvaluationListDTO> queryPage(EvaluationQueryParamsDTO evaluationQueryParams) {
        return this.baseMapper.getMemberEvaluationList(PageUtils.initPage(evaluationQueryParams), evaluationQueryParams.queryWrapper());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MemberEvaluationAddDTO addMemberEvaluation(MemberEvaluationAddDTO memberEvaluationDTO, Boolean isSelf) {
        //获取子订单信息
        OrderItem orderItem = orderItemService.getBySn(memberEvaluationDTO.getOrderItemSn());
        //获取订单信息
        Order order = orderService.getBySn(orderItem.getOrderSn());
        //检测是否可以添加会员评价
        Member member;

        checkMemberEvaluation(orderItem, order);

        if (Boolean.TRUE.equals(isSelf)) {
            //自我评价商品时，获取当前登录用户信息
            member = memberService.getUserInfo();
        } else {
            //获取用户信息 非自己评价时，读取数据库
            member = memberService.getById(order.getMemberId());
            if (member == null) {
                throw new ServiceException(ResultCode.USER_NOT_EXIST);
            }
        }
        //获取商品信息
//        GoodsSku goodsSku = goodsSkuService.getGoodsSkuByIdFromCache(memberEvaluationDTO.getSkuId());
        //新增用户评价
        MemberEvaluation memberEvaluation = new MemberEvaluation(memberEvaluationDTO, orderItem, member, order);
//        //过滤商品咨询敏感词
//        memberEvaluation.setContent(SensitiveWordsFilter.filter(memberEvaluation.getContent()));
        //添加评价
        this.save(memberEvaluation);

        //修改订单货物评价状态为已评价
        orderItemService.updateCommentStatus(orderItem.getSn(), CommentStatusEnum.FINISHED);
        //发送商品评价消息
//        applicationEventPublisher.publishEvent(new TransactionCommitSendMQEvent("同步商品评价消息",
//                rocketmqCustomProperties.getGoodsTopic(), GoodsTagsEnum.GOODS_COMMENT_COMPLETE.name(), JSONUtil.toJsonStr(memberEvaluation)));
        return memberEvaluationDTO;
    }

    @Override
    public MemberEvaluationDTO queryById(String id) {
        return new MemberEvaluationDTO(this.getById(id));
    }

    @Override
    public boolean updateStatus(String id, String status) {
        UpdateWrapper updateWrapper = Wrappers.update();
        updateWrapper.eq("id", id);
        updateWrapper.set("status", status.equals(SwitchEnum.OPEN.name()) ? SwitchEnum.OPEN.name() : SwitchEnum.CLOSE.name());
        return this.update(updateWrapper);
    }

    @Override
    public boolean delete(String id) {
        LambdaUpdateWrapper<MemberEvaluation> updateWrapper = Wrappers.lambdaUpdate();
        updateWrapper.set(MemberEvaluation::getDeleteFlag, true);
        updateWrapper.eq(MemberEvaluation::getId, id);
        return this.update(updateWrapper);
    }

    @Override
    public boolean reply(String id, String reply, String replyImage) {
        UpdateWrapper<MemberEvaluation> updateWrapper = Wrappers.update();
        updateWrapper.set("reply_status", true);
        updateWrapper.set("reply", reply);
        if (CharSequenceUtil.isNotEmpty(replyImage)) {
            updateWrapper.set("have_reply_image", true);
            updateWrapper.set("reply_image", replyImage);
        }
        updateWrapper.eq("id", id);
        return this.update(updateWrapper);
    }

    @Override
    public EvaluationNumberDTO getEvaluationNumber(String goodsId) {
        EvaluationNumberDTO evaluationNumberDTO = new EvaluationNumberDTO();
        List<Map<String, Object>> list = this.baseMapper.getEvaluationNumber(goodsId);


        Integer good = 0;
        Integer moderate = 0;
        Integer worse = 0;
        for (Map<String, Object> map : list) {
            if (map.get("grade").equals(EvaluationGradeEnum.GOOD.name())) {
                good = Integer.valueOf(map.get("num").toString());
            } else if (map.get("grade").equals(EvaluationGradeEnum.MODERATE.name())) {
                moderate = Integer.valueOf(map.get("num").toString());
            } else if (map.get("grade").equals(EvaluationGradeEnum.WORSE.name())) {
                worse = Integer.valueOf(map.get("num").toString());
            }
        }
        evaluationNumberDTO.setAll(good + moderate + worse);
        evaluationNumberDTO.setGood(good);
        evaluationNumberDTO.setModerate(moderate);
        evaluationNumberDTO.setWorse(worse);
        int count = (int) this.count(new QueryWrapper<MemberEvaluation>()
                .eq("have_image", 1)
                .eq("status", SwitchEnum.OPEN.name())
                .eq("goods_id", goodsId));
        evaluationNumberDTO.setHaveImage((long) count);

        return evaluationNumberDTO;
    }

    @Override
    public long todayMemberEvaluation() {
        return this.count(new LambdaQueryWrapper<MemberEvaluation>().ge(MemberEvaluation::getCreateTime, DateUtil.beginOfDay(new DateTime())));
    }

    @Override
    public long getWaitReplyNum() {
        AuthUser authUser = UserContext.getCurrentUser();
        QueryWrapper<MemberEvaluation> queryWrapper = Wrappers.query();
        queryWrapper.eq(CharSequenceUtil.equals(authUser.getRole().name(), UserEnums.STORE.name()),
                "store_id", authUser.getStoreId());
        queryWrapper.eq("reply_status", false);
        return this.count(queryWrapper);
    }

    /**
     * 统计商品评价数量
     *
     * @param evaluationQueryParams 查询条件
     * @return 商品评价数量
     */
    @Override
    public long getEvaluationCount(EvaluationQueryParamsDTO evaluationQueryParams) {
        return this.count(evaluationQueryParams.queryWrapper());
    }

    @Override
    public List<Map<String, Object>> memberEvaluationNum(DateTime startDate, DateTime endDate) {
        return this.baseMapper.memberEvaluationNum(new QueryWrapper<MemberEvaluation>()
                .between("create_time", startDate, endDate));
    }

    @Override
    public StoreRatingDTO getStoreRatingVO(String storeId, String status) {
        LambdaQueryWrapper<MemberEvaluation> lambdaQueryWrapper = Wrappers.lambdaQuery();
        lambdaQueryWrapper.eq(MemberEvaluation::getStoreId, storeId);
        lambdaQueryWrapper.eq(MemberEvaluation::getStatus, SwitchEnum.OPEN.name());
        return this.baseMapper.getStoreRatingVO(lambdaQueryWrapper);
    }

    /**
     * 检测会员评价
     *
     * @param orderItem 子订单
     * @param order     订单
     */
    public void checkMemberEvaluation(OrderItem orderItem, Order order) {

        //根据子订单编号判断是否评价过
        if (orderItem.getCommentStatus().equals(CommentStatusEnum.FINISHED.name())) {
            throw new ServiceException(ResultCode.EVALUATION_DOUBLE_ERROR);
        }

        //判断是否是当前会员的订单
        AuthUser authUser = UserContext.getCurrentUser();
        if (UserContext.getCurrentUser() != null && !order.getMemberId().equals(authUser.getIdString())) {
            throw new ServiceException(ResultCode.ORDER_NOT_USER);
        }
    }

}