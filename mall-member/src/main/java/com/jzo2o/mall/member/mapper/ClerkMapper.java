package com.jzo2o.mall.member.mapper;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jzo2o.mall.member.model.domain.Clerk;
import com.jzo2o.mall.member.model.dto.ClerkDTO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 店员数据处理层
 */
public interface ClerkMapper extends BaseMapper<Clerk> {

    /**
     * 查询店员分页数据
     * @param page 分页信息
     * @param ew 店铺ID
     * @return
     */
    @Select("select ums_clerk.*,m.id,m.mobile as mobile from ums_clerk inner join ums_member as m on ums_clerk.member_id = m.id ${ew.customSqlSegment}")
    IPage<ClerkDTO> selectClerkPage(Page page, @Param(Constants.WRAPPER) QueryWrapper ew);


}