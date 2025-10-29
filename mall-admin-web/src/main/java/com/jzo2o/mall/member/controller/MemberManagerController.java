package com.jzo2o.mall.member.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jzo2o.mall.common.enums.ResultUtil;
import com.jzo2o.mall.common.model.ResultMessage;
import com.jzo2o.mall.member.model.domain.Member;
import com.jzo2o.mall.member.model.dto.ManagerMemberEditDTO;
import com.jzo2o.mall.member.model.dto.MemberAddDTO;
import com.jzo2o.mall.member.model.dto.MemberDTO;
import com.jzo2o.mall.member.model.dto.MemberSearchDTO;
import com.jzo2o.mall.member.service.MemberService;
import com.jzo2o.mysql.domain.PageVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 管理端,会员接口
 */
@RestController
@Api(tags = "管理端,会员接口")
@RequestMapping("/passport/member")
public class MemberManagerController {
    @Autowired
    private MemberService memberService;

    @ApiOperation(value = "会员分页列表")
    @GetMapping
    public IPage<MemberDTO> getByPage(MemberSearchDTO memberSearchDTO, PageVO page) {
        IPage<MemberDTO> memberPage = memberService.getMemberPage(memberSearchDTO, page);
        return memberPage;
    }


    @ApiOperation(value = "通过ID获取会员信息")
    @ApiImplicitParam(name = "id", value = "会员ID", required = true, dataType = "String", paramType = "path")
    @GetMapping(value = "/{id}")
    public MemberDTO get(@PathVariable String id) {
        MemberDTO member = memberService.getMember(id);
        return member;
    }

    @ApiOperation(value = "添加会员")
    @PostMapping
    public Member save(@Valid MemberAddDTO member) {
        Member member1 = memberService.addMember(member);
        return member1;
    }

//    @PreventDuplicateSubmissions
    @ApiOperation(value = "修改会员基本信息")
    @PutMapping
    public Member update(@Valid ManagerMemberEditDTO managerMemberEditDTO) {
        Member member = memberService.updateMember(managerMemberEditDTO);
        return member;
    }

//    @PreventDuplicateSubmissions
    @ApiOperation(value = "修改会员状态,开启关闭会员")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "memberIds", value = "会员ID", required = true, dataType = "String", allowMultiple = true, paramType = "query"),
            @ApiImplicitParam(name = "disabled", required = true, dataType = "boolean", paramType = "query")
    })
    @PutMapping("/updateMemberStatus")
    public void updateMemberStatus(@RequestParam List<String> memberIds, @RequestParam Boolean disabled) {
        memberService.updateMemberStatus(memberIds, disabled);
    }


    @ApiOperation(value = "根据条件查询会员总数")
    @GetMapping("/num")
    public Long getByPage(MemberSearchDTO memberSearchDTO) {
        long memberNum = memberService.getMemberNum(memberSearchDTO);
        return memberNum;
    }


}
