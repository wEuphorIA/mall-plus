package com.jzo2o.mall.member;

import com.jzo2o.common.utils.UserContext;
import com.jzo2o.mall.common.enums.ResultCode;
import com.jzo2o.mall.common.enums.ResultUtil;
import com.jzo2o.mall.common.enums.UserEnums;
import com.jzo2o.mall.common.exception.ServiceException;
import com.jzo2o.mall.common.model.AuthUser;
import com.jzo2o.mall.common.model.ResultMessage;
import com.jzo2o.mall.member.model.domain.Member;
import com.jzo2o.mall.member.model.dto.MemberEditDTO;
import com.jzo2o.mall.member.service.MemberService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;

import javax.validation.constraints.NotNull;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;


/**
 * 买家端,会员接口
 */
@Slf4j
@RestController
@Api(tags = "买家端,会员接口")
@RequestMapping("/member/account")
public class MemberBuyerController {

    @Autowired
    private MemberService memberService;

    @ApiOperation(value = "绑定手机号")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "username", value = "用户名", required = true, paramType = "query"),
            @ApiImplicitParam(name = "mobile", value = "手机号", required = true, paramType = "query"),
            @ApiImplicitParam(name = "code", value = "验证码", required = true, paramType = "query")
    })
    @PostMapping("/bindMobile")
    public boolean bindMobile(@NotNull(message = "用户名不能为空") @RequestParam String username,
                                            @NotNull(message = "手机号为空") @RequestParam String mobile,
                                            @NotNull(message = "验证码为空") @RequestParam String code
                                            ) {
        Member member = memberService.findByUsername(username);
        if (member == null) {
            throw new ServiceException(ResultCode.USER_NOT_EXIST);
        }
        boolean b = memberService.changeMobile(member.getId(), mobile);
        return b;
    }

    @ApiOperation(value = "注册用户")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "username", value = "用户名", required = true, paramType = "query"),
            @ApiImplicitParam(name = "password", value = "密码", required = true, paramType = "query"),
            @ApiImplicitParam(name = "mobilePhone", value = "手机号", required = true, paramType = "query"),
            @ApiImplicitParam(name = "code", value = "验证码", required = true, paramType = "query")
    })
    @PostMapping("/register")
    public Member register(@NotNull(message = "用户名不能为空") @RequestParam String username,
                                          @NotNull(message = "密码不能为空") @RequestParam String password,
                                          @NotNull(message = "手机号为空") @RequestParam String mobilePhone,
                                          @NotNull(message = "验证码不能为空") @RequestParam String code) {

//        if (smsUtil.verifyCode(mobilePhone, VerificationEnums.REGISTER, uuid, code)) {
//            return ResultUtil.data(memberService.register(username, password, mobilePhone));
//        } else {
//            throw new ServiceException(ResultCode.VERIFICATION_SMS_CHECKED_ERROR);
//        }
        Member register = memberService.register(username, password, mobilePhone);
        return register;

    }

//    @ApiOperation(value = "获取当前登录用户接口")
//    @GetMapping
//    public Member getUserInfo() {
//        Member userInfo = memberService.getUserInfo();
//        return userInfo;
//    }

    @ApiOperation(value = "通过短信重置密码")
//    @ApiImplicitParams({
//            @ApiImplicitParam(name = "mobile", value = "手机号", required = true, paramType = "query"),
//            @ApiImplicitParam(name = "password", value = "是否保存登录", required = true, paramType = "query")
//    })
    @PostMapping("/resetByMobile")
    public void resetByMobile(@NotNull(message = "手机号为空") @RequestParam String mobile,
                                               @NotNull(message = "验证码为空") @RequestParam String code,
                                               @RequestHeader String uuid) {
        //校验短信验证码是否正确
//        if (smsUtil.verifyCode(mobile, VerificationEnums.FIND_USER, uuid, code)) {
//            //校验是否通过手机号可获取会员,存在则将会员信息存入缓存，有效时间3分钟
//            memberService.findByMobile(uuid, mobile);
//            return ResultUtil.success();
//        } else {
//            throw new ServiceException(ResultCode.VERIFICATION_SMS_CHECKED_ERROR);
//        }
        memberService.findByMobile(uuid, mobile);
    }

    @ApiOperation(value = "修改密码")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "password", value = "是否保存登录", required = true, paramType = "query")
    })
    @PostMapping("/resetPassword")
    public boolean resetByMobile(@NotNull(message = "密码为空") @RequestParam String password, @RequestHeader String uuid) {
        boolean b = memberService.resetByMobile(uuid, password);
        return b;
    }

    @ApiOperation(value = "修改用户自己资料")
    @PutMapping("/editOwn")
    public Member editOwn(MemberEditDTO memberEditDTO) {
        Member member = memberService.editOwn(memberEditDTO);
        return member;
    }

    @ApiOperation(value = "修改密码")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "password", value = "旧密码", required = true, paramType = "query"),
            @ApiImplicitParam(name = "newPassword", value = "新密码", required = true, paramType = "query")
    })
    @PutMapping("/modifyPass")
    public Member modifyPass(@NotNull(message = "旧密码不能为空") @RequestParam String password,
                                            @NotNull(message = "新密码不能为空") @RequestParam String newPassword) {
        Member member = memberService.modifyPass(password, newPassword);
        return member;
    }

    @ApiOperation(value = "初始设置密码")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "newPassword", value = "新密码", required = true, paramType = "query")
    })
    @PutMapping("/canInitPassword")
    public boolean canInitPassword() {
        boolean b = memberService.canInitPass();
        return b;
    }

    @ApiOperation(value = "初始设置密码")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "newPassword", value = "新密码", required = true, paramType = "query")
    })
    @PutMapping("/initPassword")
    public void initPassword(@NotNull(message = "密码不能为空") @RequestParam String password) {
        memberService.initPass(password);
    }

    @ApiOperation(value = "注销账号")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "password", value = "密码", required = true, paramType = "query")
    })
    @PutMapping("/cancellation")
    public void cancellation(@NotNull(message = "密码不能为空") @RequestParam String password) {
        memberService.cancellation(password);
    }

//    @ApiOperation(value = "刷新token")
//    @GetMapping("/refresh/{refreshToken}")
//    public ResultMessage<Object> refreshToken(@NotNull(message = "刷新token不能为空") @PathVariable String refreshToken) {
//        return ResultUtil.data(this.memberService.refreshToken(refreshToken));
//    }

    @GetMapping("/getImUser")
    @ApiOperation(value = "获取用户信息")
    public Member getImUser() {
        AuthUser authUser = UserContext.getCurrentUser();
        Member member = memberService.getById(authUser.getIdString());
        return member;
    }

    @GetMapping("/getImUserDetail/{memberId}")
    @ApiImplicitParam(name = "memberId", value = "店铺Id", required = true, dataType = "String", paramType = "path")
    @ApiOperation(value = "获取用户信息")
    public Member getImUserDetail(@PathVariable String memberId) {
        Member member = memberService.getById(memberId);
        return member;
    }

}
