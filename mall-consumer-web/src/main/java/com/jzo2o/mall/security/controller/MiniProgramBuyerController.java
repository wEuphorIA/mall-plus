package com.jzo2o.mall.security.controller;

import com.jzo2o.mall.common.enums.ResultUtil;
import com.jzo2o.mall.common.model.ResultMessage;
import com.jzo2o.mall.common.model.Token;
import com.jzo2o.mall.security.model.dto.WechatMPLoginParams;
import com.jzo2o.mall.security.service.ConnectService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 买家端,小程序登录接口
 */
@RestController
@RequestMapping("/passport/connect/miniProgram")
@Api(tags = "买家端,小程序登录接口")
public class MiniProgramBuyerController {

    @Autowired
    public ConnectService connectService;

    @GetMapping("/auto-login")
    @ApiOperation(value = "小程序自动登录")
    public Token autoLogin(@RequestHeader String uuid, WechatMPLoginParams params) {
        params.setUuid(uuid);
        Token token = this.connectService.miniProgramAutoLogin(params);
        return token;
    }

}
