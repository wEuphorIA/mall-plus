package com.jzo2o.mall.member.mapper;


import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.jzo2o.mall.member.model.domain.Member;
import com.jzo2o.mall.member.model.dto.MemberDTO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 会员数据处理层
 */
public interface MemberMapper extends BaseMapper<Member> {

    /**
     * 获取所有的会员手机号
     * @return 会员手机号
     */
    @Select("select m.mobile from ums_member m")
    List<String> getAllMemberMobile();

    @Select("select * from ums_member ${ew.customSqlSegment}")
    IPage<MemberDTO> pageByMemberVO(IPage<MemberDTO> page, @Param(Constants.WRAPPER) Wrapper<Member> queryWrapper);
}