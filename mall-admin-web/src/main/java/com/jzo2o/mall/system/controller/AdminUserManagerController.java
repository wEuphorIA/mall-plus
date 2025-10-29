package com.jzo2o.mall.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jzo2o.mall.common.enums.ResultCode;
import com.jzo2o.mall.common.exception.ServiceException;
import com.jzo2o.mall.system.model.domain.AdminUser;
import com.jzo2o.mall.system.model.dto.AdminUserDTO;
import com.jzo2o.mall.system.service.AdminUserService;
import com.jzo2o.mall.security.token.service.AdminUserSecurityService;
import com.jzo2o.mysql.domain.PageVO;
import com.jzo2o.mysql.domain.SearchVO;
import com.jzo2o.mysql.utils.PageUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;


/**
 * 管理员接口
 */
@Slf4j
@RestController
@Api(tags = "管理员")
@RequestMapping("/passport/user")
@Validated
public class AdminUserManagerController {
    @Autowired
    private AdminUserService adminUserService;
    @Autowired
    private AdminUserSecurityService adminUserSecurityService;
    /**
     * 会员
     */
//    @Autowired
//    private MemberService memberService;
//
//    @Autowired
//    private VerificationService verificationService;


    @GetMapping
    @ApiOperation(value = "多条件分页获取用户列表")
    public IPage<AdminUserDTO> getByCondition(AdminUserDTO user,
                                                            SearchVO searchVo,
                                                            PageVO pageVo) {
        Page<AdminUser> objectPage = PageUtils.initPage(pageVo);
        QueryWrapper<AdminUser> objectQueryWrapper = PageUtils.initWrapper(user, searchVo);
        IPage<AdminUserDTO> page = adminUserService.adminUserPage(objectPage,objectQueryWrapper );

        return page;
    }

    @PostMapping
    @ApiOperation(value = "添加用户")
    public void register(@Valid AdminUserDTO adminUser,
                                          @RequestParam(required = false) List<String> rolesList) {
        int rolesMaxSize = 10;
            if (rolesList != null && rolesList.size() >= rolesMaxSize) {
                throw new ServiceException(ResultCode.PERMISSION_BEYOND_TEN);
            }
            adminUserService.saveAdminUser(adminUser, rolesList);
    }

    @PutMapping(value = "/enable/{userId}")
    @ApiOperation(value = "禁/启 用 用户")
    public void disable(@ApiParam("用户唯一id标识") @PathVariable String userId, Boolean status) {
        AdminUser user = adminUserService.getById(userId);
        if (user == null) {
            throw new ServiceException(ResultCode.USER_NOT_EXIST);
        }
        user.setStatus(status);
        adminUserService.updateById(user);

        //登出用户
        if (Boolean.FALSE.equals(status)) {
            List<String> userIds = new ArrayList<>();
            userIds.add(userId);
            adminUserSecurityService.logout(userIds);
        }

    }

    @DeleteMapping(value = "/{ids}")
    @ApiOperation(value = "批量通过ids删除")
    public void delAllByIds(@PathVariable List<String> ids) {
        adminUserService.deleteCompletely(ids);
    }

}
