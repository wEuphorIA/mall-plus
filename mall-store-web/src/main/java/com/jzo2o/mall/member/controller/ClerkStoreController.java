package com.jzo2o.mall.member.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jzo2o.common.utils.UserContext;
import com.jzo2o.mall.common.enums.ResultCode;
import com.jzo2o.mall.common.enums.ResultUtil;
import com.jzo2o.mall.common.exception.ServiceException;
import com.jzo2o.mall.common.model.AuthUser;
import com.jzo2o.mall.common.model.ResultMessage;
import com.jzo2o.mall.member.model.domain.Clerk;
import com.jzo2o.mall.member.model.domain.Member;
import com.jzo2o.mall.member.model.dto.*;
import com.jzo2o.mall.member.service.ClerkService;
import com.jzo2o.mall.member.service.MemberService;
import com.jzo2o.mysql.domain.PageVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


/**
 * 店员接口
 */
@Slf4j
@RestController
@Api(tags = "店员")
@RequestMapping("/clerk")
@Transactional(rollbackFor = Exception.class)
@Validated
public class ClerkStoreController {
    @Autowired
    private ClerkService clerkService;

    @Autowired
    private MemberService memberService;


    @GetMapping
    @ApiOperation(value = "分页获取店员列表")
    public IPage<ClerkDTO> page(ClerkQueryDTO clerkQueryDTO,
                                               PageVO pageVo) {

        IPage<ClerkDTO> page = clerkService.clerkForPage(pageVo, clerkQueryDTO);
        return page;
    }

    @GetMapping("/{id}")
    @ApiOperation(value = "获取店员详细")
    public ClerkDTO get(@PathVariable String id) {
        ClerkDTO clerkDTO = clerkService.get(id);

        return clerkDTO;
    }


    @PostMapping("/{mobile}/check")
    @ApiOperation(value = "检测手机号码有效性")
    public Member check(@PathVariable /*@Phone(message = "手机号码格式不正确")*/ String mobile) {
        Member member = clerkService.checkClerk(mobile);
        return member;
    }


    @PostMapping
    @ApiOperation(value = "添加店员")
    public  void add(@Valid ClerkAddDTO clerkAddDTO) {
        AuthUser tokenUser =  UserContext.getCurrentUser();
        int rolesMaxSize = 10;
        try {
            if (clerkAddDTO.getRoles() != null && clerkAddDTO.getRoles().size() >= rolesMaxSize) {
                throw new ServiceException(ResultCode.PERMISSION_BEYOND_TEN);
            }
            //校验是否已经是会员
            Member member = memberService.findByMobile(clerkAddDTO.getMobile());
            if (member == null) {
                //添加会员
                MemberAddDTO memberAddDTO = new MemberAddDTO();
                memberAddDTO.setMobile(clerkAddDTO.getMobile());
                memberAddDTO.setPassword(clerkAddDTO.getPassword());
                memberAddDTO.setUsername(clerkAddDTO.getUsername());
                member = memberService.addMember(memberAddDTO);
            } else {
                //校验要添加的会员是否已经是店主
                if (Boolean.TRUE.equals(member.getHaveStore())) {
                    throw new ServiceException(ResultCode.STORE_APPLY_DOUBLE_ERROR);
                }
                //校验会员的有效性
                if (Boolean.FALSE.equals(member.getDisabled())) {
                    throw new ServiceException(ResultCode.USER_STATUS_ERROR);
                }
            }
            //添加店员
            clerkAddDTO.setMemberId(member.getId());
            clerkAddDTO.setShopkeeper(false);
            clerkAddDTO.setStoreId(tokenUser.getStoreId());
            clerkService.saveClerk(clerkAddDTO);
            //修改此会员拥有店铺
            List<String> ids = new ArrayList<>();
            ids.add(member.getId());
            memberService.updateHaveShop(true, tokenUser.getStoreId(), ids);
        } catch (ServiceException se) {
            log.info(se.getMsg(), se);
            throw se;
        } catch (Exception e) {
            log.error("添加店员出错", e);
        }
    }


    @PutMapping("/{id}")
    @ApiImplicitParam(name = "id", value = "店员id", required = true, paramType = "path")
    @ApiOperation(value = "修改店员")
    public Clerk edit(@PathVariable String id, @Valid ClerkEditDTO clerkEditDTO) {
        clerkEditDTO.setId(id);
        Clerk clerk = clerkService.updateClerk(clerkEditDTO);
        return clerk;
    }

    @PutMapping(value = "/enable/{clerkId}")
    @ApiOperation(value = "禁/启 用 店员")
    public void disable(@ApiParam("用户唯一id标识") @PathVariable String clerkId, Boolean status) {
        clerkService.disable(clerkId, status);
    }


    @DeleteMapping(value = "/delByIds/{ids}")
    @ApiOperation(value = "删除店员")
    public void deleteClerk(@PathVariable List<String> ids) {
        clerkService.deleteClerk(ids);
    }


    @PostMapping(value = "/resetPassword/{ids}")
    @ApiOperation(value = "重置密码")
    public void resetPassword(@PathVariable List ids) {
        clerkService.resetPassword(ids);
    }


}
