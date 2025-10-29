package com.jzo2o.mall.security.controller;

import com.jzo2o.common.utils.UserContext;
import com.jzo2o.mall.common.enums.ResultCode;
import com.jzo2o.mall.common.enums.UserEnums;
import com.jzo2o.mall.common.exception.ServiceException;
import com.jzo2o.mall.common.model.AuthUser;
import com.jzo2o.mall.common.model.Token;
import com.jzo2o.mall.system.model.domain.AdminUser;
import com.jzo2o.mall.system.service.AdminUserService;
import com.jzo2o.mall.security.token.service.AdminUserSecurityService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;


/**
 * 管理员接口
 */
@Slf4j
@RestController
@Api(tags = "管理员")
@RequestMapping("/passport/user")
@Validated
public class AdminUserSecurityController {
    @Autowired
    private AdminUserSecurityService adminUserSecurityService;
    @Autowired
    private AdminUserService adminUserService;

    /**
     * 会员
     */
//    @Autowired
//    private MemberService memberService;
//
//    @Autowired
//    private VerificationService verificationService;

    @PostMapping(value = "/login")
    @ApiOperation(value = "登录管理员")
    public Token login(@NotNull(message = "用户名不能为空") @RequestParam String username,
                       @NotNull(message = "密码不能为空") @RequestParam String password) {
        Token token = adminUserSecurityService.login(username, password);
        return token;

//        if (verificationService.check(uuid, VerificationEnums.LOGIN)) {
//            return ResultUtil.data(adminUserService.login(username, password));
//        } else {
//            throw new ServiceException(ResultCode.VERIFICATION_ERROR);
//        }
    }

    @ApiOperation(value = "注销接口")
    @PostMapping("/logout")
    public void logout() {
        this.adminUserSecurityService.logout(UserEnums.MANAGER);
    }

    @ApiOperation(value = "刷新token")
    @GetMapping("/refresh/{refreshToken}")
    public Token refreshToken(@NotNull(message = "刷新token不能为空") @PathVariable String refreshToken) {
        Token token = this.adminUserSecurityService.refreshToken(refreshToken);
        return token;
    }


    @GetMapping(value = "/info")
    @ApiOperation(value = "获取当前登录用户接口")
    public AdminUser getUserInfo() {
        AuthUser tokenUser =  UserContext.getCurrentUser();
        if (tokenUser != null) {
            AdminUser adminUser = adminUserService.findByUsername(tokenUser.getUsername());
            adminUser.setPassword(null);
            return adminUser;
        }
        throw new ServiceException(ResultCode.USER_NOT_LOGIN);
    }

    @PutMapping(value = "/edit")
    @ApiOperation(value = "修改用户自己资料", notes = "用户名密码不会修改")
    public void editOwner(AdminUser adminUser) {

        AuthUser tokenUser =  UserContext.getCurrentUser();
        if (tokenUser != null) {
            //查询当前管理员
            AdminUser oldAdminUser = adminUserService.findByUsername(tokenUser.getUsername());
            oldAdminUser.setAvatar(adminUser.getAvatar());
            oldAdminUser.setNickName(adminUser.getNickName());
            if (!adminUserService.updateById(oldAdminUser)) {
                throw new ServiceException(ResultCode.USER_EDIT_ERROR);
            }
            return ;
        }
        throw new ServiceException(ResultCode.USER_NOT_LOGIN);
    }

    @PutMapping(value = "/admin/edit")
    @ApiOperation(value = "超级管理员修改其他管理员资料")
    public void edit(@Valid AdminUser adminUser,
                                      @RequestParam(required = false) List<String> roles) {
        if (!adminUserService.updateAdminUser(adminUser, roles)) {
            throw new ServiceException(ResultCode.USER_EDIT_ERROR);
        }
    }

    /**
     * 修改密码
     *
     * @param password
     * @param newPassword
     * @return
     */
    @PutMapping(value = "/editPassword")
    @ApiOperation(value = "修改密码")
    public void editPassword(String password, String newPassword) {
        AuthUser currentUser = UserContext.getCurrentUser();
        adminUserService.editPassword(currentUser,password, newPassword);
    }

    @PostMapping(value = "/resetPassword/{ids}")
    @ApiOperation(value = "重置密码")
    public void resetPassword(@PathVariable List ids) {
        adminUserService.resetPassword(ids);
    }


}
