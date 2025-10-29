package com.jzo2o.mall.security.token.service.impl;

import com.jzo2o.mall.common.enums.ClientTypeEnum;
import com.jzo2o.mall.common.enums.UserEnums;
import com.jzo2o.mall.common.model.AuthUser;
import com.jzo2o.mall.common.model.Token;
import com.jzo2o.mall.member.model.domain.Member;
import com.jzo2o.mall.security.token.service.TokenGenerateService;
import com.jzo2o.mall.security.token.TokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * 会员token生成
 */
@Component
public class MemberTokenGenerateServiceImpl implements TokenGenerateService<Member> {
    @Autowired
    private TokenUtil tokenUtil;
//    @Autowired
//    private RocketmqCustomProperties rocketmqCustomProperties;
//
//    @Autowired
//    private RocketMQTemplate rocketMQTemplate;

    @Override
    public Token createToken(Member member, Boolean longTerm) {

//        ClientTypeEnum clientTypeEnum;
//        try {
//            //获取客户端类型
//            String clientType = ThreadContextHolder.getHttpRequest().getHeader("clientType");
//            //如果客户端为空，则缺省值为PC，pc第三方登录时不会传递此参数
//            if (clientType == null) {
//                clientTypeEnum = ClientTypeEnum.PC;
//            } else {
//                clientTypeEnum = ClientTypeEnum.valueOf(clientType);
//            }
//        } catch (Exception e) {
//            clientTypeEnum = ClientTypeEnum.UNKNOWN;
//        }
        //记录最后登录时间，客户端类型
        member.setLastLoginDate(new Date());
        member.setClientEnum(ClientTypeEnum.WECHAT_MP.clientName());
//        String destination = rocketmqCustomProperties.getMemberTopic() + ":" + MemberTagsEnum.MEMBER_LOGIN.name();
//        rocketMQTemplate.asyncSend(destination, member, RocketmqSendCallbackBuilder.commonCallback());

        AuthUser authUser = AuthUser.builder()
                .username(member.getUsername())
                .face(member.getFace())
                .id(member.getId())
                .role(UserEnums.MEMBER)
                .nickName(member.getNickName())
                .longTerm(longTerm)
                .build();
        //登陆成功生成token
        return tokenUtil.createToken(authUser);
    }

    @Override
    public Token refreshToken(String refreshToken) {
        return tokenUtil.refreshToken(refreshToken);
    }

}
