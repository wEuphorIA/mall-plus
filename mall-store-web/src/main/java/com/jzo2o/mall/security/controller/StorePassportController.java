package com.jzo2o.mall.security.controller;


import com.jzo2o.common.utils.UserContext;
import com.jzo2o.mall.common.enums.ResultCode;
import com.jzo2o.mall.common.enums.ResultUtil;
import com.jzo2o.mall.common.enums.UserEnums;
import com.jzo2o.mall.common.exception.ServiceException;
import com.jzo2o.mall.common.model.AuthUser;
import com.jzo2o.mall.common.model.ResultMessage;
import com.jzo2o.mall.common.model.Token;
import com.jzo2o.mall.member.model.domain.Member;
import com.jzo2o.mall.member.service.MemberService;
import com.jzo2o.mall.security.token.service.MemberSecurityService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;

/**
 * 店铺端,商家登录接口
 *
 * @author Chopper
 * @since 2020/12/22 15:02
 */

@RestController
@Api(tags = "店铺端,商家登录接口 ")
@RequestMapping("/passport/login")
public class StorePassportController {

    /**
     * 会员安全
     */
    @Autowired
    private MemberSecurityService memberSecurityService;

    /**
     * 会员服务
     */
    @Autowired
    private MemberService memberService;

//    @Autowired
//    private SmsUtil smsUtil;

//    @Autowired
//    private VerificationService verificationService;

    @ApiOperation(value = "登录接口")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "username", value = "用户名", required = true, paramType = "query"),
            @ApiImplicitParam(name = "password", value = "密码", required = true, paramType = "query")
    })
    @PostMapping("/userLogin")
    public Token userLogin(@NotNull(message = "用户名不能为空") @RequestParam String username,
                                           @NotNull(message = "密码不能为空") @RequestParam String password, @RequestHeader String uuid) {
//        if (verificationService.check(uuid, VerificationEnums.LOGIN)) {
//            return ResultUtil.data(this.memberService.usernameStoreLogin(username, password));
//        } else {
//            throw new ServiceException(ResultCode.VERIFICATION_ERROR);
//        }
        Token token = memberSecurityService.usernameStoreLogin(username, password);
        return token;
    }

//    @ApiOperation(value = "短信登录接口")
//    @ApiImplicitParams({
//            @ApiImplicitParam(name = "mobile", value = "手机号", required = true, paramType = "query"),
//            @ApiImplicitParam(name = "code", value = "验证码", required = true, paramType = "query")
//    })
//    @PostMapping("/smsLogin")
//    public ResultMessage<Object> smsLogin(@NotNull(message = "手机号为空") @RequestParam String mobile,
//                                          @NotNull(message = "验证码为空") @RequestParam String code,
//                                          @RequestHeader String uuid) {
//        if (smsUtil.verifyCode(mobile, VerificationEnums.LOGIN, uuid, code)) {
//            return ResultUtil.data(memberService.mobilePhoneStoreLogin(mobile));
//        } else {
//            throw new ServiceException(ResultCode.VERIFICATION_SMS_CHECKED_ERROR);
//        }
//    }

    @ApiOperation(value = "注销接口")
    @PostMapping("/logout")
    public ResultMessage<Object> logout() {
        memberSecurityService.logout(UserEnums.STORE);
        return ResultUtil.success();
    }

//    @ApiOperation(value = "通过短信重置密码")
//    @ApiImplicitParams({
//            @ApiImplicitParam(name = "mobile", value = "手机号", required = true, paramType = "query"),
//            @ApiImplicitParam(name = "password", value = "是否保存登录", required = true, paramType = "query")
//    })
//    @PostMapping("/resetByMobile")
//    public ResultMessage<Member> resetByMobile(@NotNull(message = "手机号为空") @RequestParam String mobile,
//                                               @NotNull(message = "验证码为空") @RequestParam String code,
//                                               @RequestHeader String uuid) {
//        //校验短信验证码是否正确
//        if (smsUtil.verifyCode(mobile, VerificationEnums.FIND_USER, uuid, code)) {
//            //校验是否通过手机号可获取会员,存在则将会员信息存入缓存，有效时间3分钟
//            memberService.findByMobile(uuid, mobile);
//            return ResultUtil.success();
//        } else {
//            throw new ServiceException(ResultCode.VERIFICATION_SMS_CHECKED_ERROR);
//        }
//    }
    @ApiOperation(value = "修改密码")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "password", value = "密码", required = true, paramType = "query")
    })
    @PostMapping("/resetPassword")
    public boolean resetByMobile(@NotNull(message = "密码为空") @RequestParam String password, @RequestHeader String uuid) {
        boolean b = memberService.resetByMobile(uuid, password);
        return b;
    }

    @ApiOperation(value = "修改密码")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "password", value = "旧密码", required = true, paramType = "query"),
            @ApiImplicitParam(name = "newPassword", value = "新密码", required = true, paramType = "query")
    })
    @PostMapping("/modifyPass")
    public Member modifyPass(@NotNull(message = "旧密码不能为空") @RequestParam String password,
                                            @NotNull(message = "新密码不能为空") @RequestParam String newPassword) {
        AuthUser authUser =  UserContext.getCurrentUser();
        if (authUser == null) {
            throw new ServiceException(ResultCode.USER_NOT_LOGIN);
        }
        Member member = memberService.modifyPass(password, newPassword);
        return member;
    }

    @ApiOperation(value = "刷新token")
    @GetMapping("/refresh/{refreshToken}")
    public Token refreshToken(@NotNull(message = "刷新token不能为空") @PathVariable String refreshToken) {
        Token token = this.memberSecurityService.refreshStoreToken(refreshToken);
        return token;
    }
}
