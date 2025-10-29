package com.jzo2o.mall.security.token.service.impl;


import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jzo2o.common.utils.UserContext;
import com.jzo2o.mall.common.cache.CachePrefix;
import com.jzo2o.mall.common.enums.ResultCode;
import com.jzo2o.mall.common.enums.UserEnums;
import com.jzo2o.mall.common.exception.ServiceException;
import com.jzo2o.mall.common.model.AuthUser;
import com.jzo2o.mall.common.model.Token;
import com.jzo2o.mall.member.mapper.MemberMapper;
import com.jzo2o.mall.member.model.domain.Member;
import com.jzo2o.mall.member.model.domain.Store;
import com.jzo2o.mall.member.model.enums.StoreStatusEnum;
import com.jzo2o.mall.member.service.MemberService;
import com.jzo2o.mall.member.service.StoreService;
import com.jzo2o.mall.security.token.service.MemberSecurityService;
import com.jzo2o.redis.helper.Cache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 会员接口业务层实现
 *
 * @author Chopper
 * @since 2021-03-29 14:10:16
 */
@Service
public class MemberSecurityServiceImpl extends ServiceImpl<MemberMapper, Member> implements MemberSecurityService {

    /**
     * 商家token
     */
    @Autowired
    private StoreTokenGenerateServiceImpl storeTokenGenerateService;
//    /**
//     * 联合登录
//     */
//    @Autowired
//    private ConnectService connectService;

    @Autowired
    private MemberService memberService;
    /**
     * 店铺
     */
    @Autowired
    private StoreService storeService;

    @Autowired
    private MemberTokenGenerateServiceImpl memberTokenGenerateService;
//    /**
//     * RocketMQ 配置
//     */
//    @Autowired
//    private RocketmqCustomProperties rocketmqCustomProperties;
//
//    @Autowired
//    private RocketMQTemplate rocketMQTemplate;

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;
    /**
     * 缓存
     */
    @Autowired
    private Cache cache;

    @Override
    public Member getUserInfo() {
        AuthUser tokenUser = UserContext.getCurrentUser();
        if (tokenUser != null) {
            return memberService.findByUsername(tokenUser.getUsername());
        }
        throw new ServiceException(ResultCode.USER_NOT_LOGIN);
    }

    @Override
    public Token usernameLogin(String username, String password) {
        Member member = this.findMember(username);
        //判断用户是否存在
        if (member == null || !member.getDisabled()) {
            throw new ServiceException(ResultCode.USER_NOT_EXIST);
        }
        //判断密码是否输入正确
        if (!new BCryptPasswordEncoder().matches(password, member.getPassword())) {
            throw new ServiceException(ResultCode.USER_PASSWORD_ERROR);
        }
//        loginBindUser(member);
        return memberTokenGenerateService.createToken(member, false);
    }


    @Override
    public Token usernameStoreLogin(String username, String password) {

        Member member = this.findMember(username);
        //判断用户是否存在
        if (member == null || !member.getDisabled()) {
            throw new ServiceException(ResultCode.USER_NOT_EXIST);
        }
        //判断密码是否输入正确
        if (!new BCryptPasswordEncoder().matches(password, member.getPassword())) {
            throw new ServiceException(ResultCode.USER_PASSWORD_ERROR);
        }
        //对店铺状态的判定处理
        return checkMemberStore(member);
    }
//
//    @Override
//    public Token mobilePhoneStoreLogin(String mobilePhone) {
//        Member member = this.findMember(mobilePhone);
//        //如果手机号不存在则自动注册用户
//        if (member == null) {
//            throw new ServiceException(ResultCode.USER_NOT_EXIST);
//        }
//        loginBindUser(member);
//        //对店铺状态的判定处理
//        return checkMemberStore(member);
//    }
    /**
     * 传递手机号或者用户名
     *
     * @param userName 手机号或者用户名
     * @return 会员信息
     */
    private Member findMember(String userName) {
        QueryWrapper<Member> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", userName).or().eq("mobile", userName);
        return this.getOne(queryWrapper);
    }

    private Token checkMemberStore(Member member) {
        if (Boolean.TRUE.equals(member.getHaveStore())) {
            Store store = storeService.getById(member.getStoreId());
            if (!store.getStoreDisable().equals(StoreStatusEnum.OPEN.name())) {
                throw new ServiceException(ResultCode.STORE_CLOSE_ERROR);
            }
        } else {
            throw new ServiceException(ResultCode.USER_NOT_EXIST);
        }
        return storeTokenGenerateService.createToken(member, false);
    }

//    @Override
//    public Token refreshToken(String refreshToken) {
//        return memberTokenGenerate.refreshToken(refreshToken);
//    }
//
    @Override
    public Token refreshStoreToken(String refreshToken) {
        return storeTokenGenerateService.refreshToken(refreshToken);
    }
//
//    @Override
//    @Transactional
//    public Token mobilePhoneLogin(String mobilePhone) {
//        QueryWrapper<Member> queryWrapper = new QueryWrapper<>();
//        queryWrapper.eq("mobile", mobilePhone);
//        Member member = this.baseMapper.selectOne(queryWrapper);
//        //如果手机号不存在则自动注册用户
//        if (member == null) {
//            member = new Member(mobilePhone, UuidUtils.getUUID(), mobilePhone);
//            registerHandler(member);
//        }
//        loginBindUser(member);
//        return memberTokenGenerate.createToken(member, false);
//    }


//    /**
//     * 成功登录，则检测cookie中的信息，进行会员绑定
//     *
//     * @param member 会员
//     */
//    private void loginBindUser(Member member) {
//        //获取cookie存储的信息
//        String uuid = CookieUtil.getCookie(ConnectService.CONNECT_COOKIE, ThreadContextHolder.getHttpRequest());
//        String connectType = CookieUtil.getCookie(ConnectService.CONNECT_TYPE, ThreadContextHolder.getHttpRequest());
//        //如果联合登陆存储了信息
//        if (CharSequenceUtil.isNotEmpty(uuid) && CharSequenceUtil.isNotEmpty(connectType)) {
//            try {
//                //获取信息
//                ConnectAuthUser connectAuthUser = getConnectAuthUser(uuid, connectType);
//                if (connectAuthUser == null) {
//                    return;
//                }
//                Connect connect = connectService.queryConnect(
//                        ConnectQueryDTO.builder().unionId(connectAuthUser.getUuid()).unionType(connectType).build()
//                );
//                if (connect == null) {
//                    connect = new Connect(member.getId(), connectAuthUser.getUuid(), connectType);
//                    connectService.save(connect);
//                }
//            } catch (ServiceException e) {
//                throw e;
//            } catch (Exception e) {
//                log.error("绑定第三方联合登陆失败：", e);
//            } finally {
//                //联合登陆成功与否，都清除掉cookie中的信息
//                CookieUtil.delCookie(ConnectService.CONNECT_COOKIE, ThreadContextHolder.getHttpResponse());
//                CookieUtil.delCookie(ConnectService.CONNECT_TYPE, ThreadContextHolder.getHttpResponse());
//            }
//        }
//
//    }


//    /**
//     * 检测是否可以绑定第三方联合登陆
//     * 返回null原因
//     * 包含原因1：redis中已经没有联合登陆信息  2：已绑定其他账号
//     *
//     * @return 返回对象则代表可以进行绑定第三方会员，返回null则表示联合登陆无法继续
//     */
//    private ConnectAuthUser checkConnectUser() {
//        //获取cookie存储的信息
//        String uuid = CookieUtil.getCookie(ConnectService.CONNECT_COOKIE, ThreadContextHolder.getHttpRequest());
//        String connectType = CookieUtil.getCookie(ConnectService.CONNECT_TYPE, ThreadContextHolder.getHttpRequest());
//
//        //如果联合登陆存储了信息
//        if (CharSequenceUtil.isNotEmpty(uuid) && CharSequenceUtil.isNotEmpty(connectType)) {
//            //枚举 联合登陆类型获取
//            ConnectAuthEnum authInterface = ConnectAuthEnum.valueOf(connectType);
//
//            ConnectAuthUser connectAuthUser = getConnectAuthUser(uuid, connectType);
//            if (connectAuthUser == null) {
//                throw new ServiceException(ResultCode.USER_OVERDUE_CONNECT_ERROR);
//            }
//            //检测是否已经绑定过用户
//            Connect connect = connectService.queryConnect(
//                    ConnectQueryDTO.builder().unionType(connectType).unionId(connectAuthUser.getUuid()).build()
//            );
//            //没有关联则返回true，表示可以继续绑定
//            if (connect == null) {
//                connectAuthUser.setConnectEnum(authInterface);
//                return connectAuthUser;
//            } else {
//                throw new ServiceException(ResultCode.USER_CONNECT_BANDING_ERROR);
//            }
//        } else {
//            throw new ServiceException(ResultCode.USER_CONNECT_NOT_EXIST_ERROR);
//        }
//    }


    /**
     * 登出
     */
    @Override
    public void logout(UserEnums userEnums) {
        AuthUser authUser = UserContext.getCurrentUser();
        String currentUserToken = authUser.getAccessToken();

        if (CharSequenceUtil.isNotEmpty(currentUserToken)) {
            cache.remove(CachePrefix.ACCESS_TOKEN.getPrefix(userEnums, authUser.getIdString()) + currentUserToken);
            cache.vagueDel(CachePrefix.REFRESH_TOKEN.getPrefix(userEnums, authUser.getIdString()));
        }
    }

    @Override
    public void logout(String userId) {

        cache.vagueDel(CachePrefix.ACCESS_TOKEN.getPrefix(UserEnums.MANAGER, userId));
        cache.vagueDel(CachePrefix.REFRESH_TOKEN.getPrefix(UserEnums.MANAGER, userId));
    }


}