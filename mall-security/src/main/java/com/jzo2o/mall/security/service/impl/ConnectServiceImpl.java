package com.jzo2o.mall.security.service.impl;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jzo2o.common.utils.UserContext;
import com.jzo2o.mall.common.enums.ClientTypeEnum;
import com.jzo2o.mall.common.enums.ConnectEnum;
import com.jzo2o.mall.common.enums.ResultCode;
import com.jzo2o.mall.common.exception.ServiceException;
import com.jzo2o.mall.common.model.Token;
import com.jzo2o.mall.common.utils.CommonUtil;
import com.jzo2o.mall.common.utils.HttpUtils;
import com.jzo2o.mall.common.utils.UuidUtils;
import com.jzo2o.mall.member.model.domain.Member;
import com.jzo2o.mall.member.service.MemberService;
import com.jzo2o.mall.security.mapper.ConnectMapper;
import com.jzo2o.mall.security.model.domain.Connect;
import com.jzo2o.mall.security.model.dto.AuthToken;
import com.jzo2o.mall.security.model.dto.ConnectAuthUser;
import com.jzo2o.mall.security.model.dto.ConnectQueryDTO;
import com.jzo2o.mall.security.model.dto.WechatMPLoginParams;
import com.jzo2o.mall.security.model.enums.SourceEnum;
import com.jzo2o.mall.security.service.ConnectService;
import com.jzo2o.mall.security.token.service.impl.MemberTokenGenerateServiceImpl;
import com.jzo2o.mall.system.enums.SettingEnum;
import com.jzo2o.mall.system.model.domain.Setting;
import com.jzo2o.mall.system.model.dto.connect.WechatConnectSetting;
import com.jzo2o.mall.system.model.dto.connect.dto.WechatConnectSettingItem;
import com.jzo2o.mall.system.service.SettingService;
import com.jzo2o.redis.helper.Cache;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.naming.NoPermissionException;
import java.nio.charset.StandardCharsets;
import java.security.AlgorithmParameters;
import java.security.Security;
import java.util.*;

/**
 * 联合登陆接口实现
 *
 * @author Chopper
 */
@Slf4j
@Service
public class ConnectServiceImpl extends ServiceImpl<ConnectMapper, Connect> implements ConnectService {

    static final boolean AUTO_REGION = true;
    /**
     * 默认密码
     */
    static String DEFAULT_PASSWORD = "111111";
    @Autowired
    private SettingService settingService;
    @Autowired
    private MemberService memberService;
    @Autowired
    private ConnectServiceImpl owner;
//    @Autowired
//    private MemberTokenGenerate memberTokenGenerate;
    @Autowired
    private MemberTokenGenerateServiceImpl memberTokenGenerate;

    @Autowired
    private Cache cache;
    /**
     * RocketMQ
     */
//    @Autowired
//    private RocketMQTemplate rocketMQTemplate;
    /**
     * RocketMQ配置
     */
//    @Autowired
//    private RocketmqCustomProperties rocketmqCustomProperties;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Token unionLoginCallback(ConnectAuthUser authUser, String uuid) {
        return this.unionLoginCallback(authUser, false);
    }

    @Override
    public void bind(String userId,String unionId, String type) {
        Connect connect = new Connect(userId, unionId, type);
        this.save(connect);


    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unbind(String type) {

        LambdaQueryWrapper<Connect> queryWrapper = new LambdaQueryWrapper<>();

        queryWrapper.eq(Connect::getUserId, UserContext.getCurrentUser().getId());
        queryWrapper.eq(Connect::getUnionType, type);

        this.remove(queryWrapper);
    }

    @Override
    public List<String> bindList() {
        LambdaQueryWrapper<Connect> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Connect::getUserId, UserContext.getCurrentUser().getId());
        List<Connect> connects = this.list(queryWrapper);
        List<String> keys = new ArrayList<>();
        connects.forEach(item -> keys.add(item.getUnionType()));
        return keys;
    }


    @Override
    @Transactional
    public Token miniProgramAutoLogin(WechatMPLoginParams params) {

        return null;

    }


    /**
     * 通过微信返回等code 获取openid 等信息
     *
     * @param code 微信code
     * @return 微信返回的信息
     */
    public JSONObject getConnect(String code) {
        WechatConnectSettingItem setting = getWechatMPSetting();
        String url = "https://api.weixin.qq.com/sns/jscode2session?" +
                "appid=" + setting.getAppId() + "&" +
                "secret=" + setting.getAppSecret() + "&" +
                "js_code=" + code + "&" +
                "grant_type=authorization_code";
        String content = HttpUtils.doGet(url, "UTF-8", 15000, 15000);
        log.error(content);
        return JSONUtil.parseObj(content);
    }

    /**
     * 手机号 绑定 且 自动登录
     *
     * @param sessionKey 微信sessionKey
     * @param params     微信小程序自动登录参数
     * @param openId     微信openid
     * @param unionId    微信unionid
     * @return token
     */
    @Transactional(rollbackFor = Exception.class)
    public Token phoneMpBindAndLogin(String sessionKey, WechatMPLoginParams params, String openId, String unionId) {
        try {
            String encryptedData = params.getEncryptedData();
            String iv = params.getIv();
            JSONObject userInfo = this.getUserInfo(encryptedData, sessionKey, iv);
            log.info("联合登陆返回：{}", userInfo.toString());


            ConnectAuthUser connectAuthUser = new ConnectAuthUser();
            connectAuthUser.setUuid(openId);
            connectAuthUser.setNickname(params.getNickName());
            connectAuthUser.setAvatar(params.getImage());

            if (userInfo.containsKey("purePhoneNumber")) {
                String phone = (String) userInfo.get("purePhoneNumber");
                connectAuthUser.setUsername("m" + phone);
                connectAuthUser.setPhone(phone);
            } else {
                connectAuthUser.setUsername(UuidUtils.getUUID());
            }
            connectAuthUser.setSource(ConnectEnum.WECHAT);
            connectAuthUser.setType(ClientTypeEnum.WECHAT_MP);

            AuthToken authToken = new AuthToken();
            authToken.setUnionId(unionId);
            connectAuthUser.setToken(authToken);
            return this.unionLoginCallback(connectAuthUser, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Connect queryConnect(ConnectQueryDTO connectQueryDTO) {

        LambdaQueryWrapper<Connect> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CharSequenceUtil.isNotEmpty(connectQueryDTO.getUserId()), Connect::getUserId,
                        connectQueryDTO.getUserId())
                .eq(CharSequenceUtil.isNotEmpty(connectQueryDTO.getUnionType()), Connect::getUnionType,
                        connectQueryDTO.getUnionType())
                .eq(CharSequenceUtil.isNotEmpty(connectQueryDTO.getUnionId()), Connect::getUnionId,
                        connectQueryDTO.getUnionId());
        return this.getOne(queryWrapper, false);
    }

    @Override
    public void deleteByMemberId(String userId) {
        LambdaQueryWrapper<Connect> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Connect::getUserId, userId);
        this.remove(queryWrapper);
    }

    /**
     * 成功登录，则检测cookie中的信息，进行会员绑定
     *
     * @param userId  用户ID
     * @param unionId 第三方用户ID
     * @param type    类型
     */
    @Override
    public void loginBindUser(String userId, String unionId, String type) {
        Connect connect = this.queryConnect(
                ConnectQueryDTO.builder().unionId(unionId).unionType(type).build()
        );
        //如果未绑定则直接绑定
        if (connect == null) {
            connect = new Connect(userId, unionId, type);
            this.save(connect);
            //如果已绑定不是当前用户信息则删除绑定信息，重新绑定
        } else if (!connect.getUserId().equals(userId)) {
            this.removeById(connect.getId());
            this.loginBindUser(userId, unionId, type);
        }
    }


    /**
     * 第三方联合登陆
     * 1.判断是否使用开放平台
     * 1.1如果使用开放平台则使用UnionId进行登录
     * 1.2如果不适用开放平台则使用OpenId进行登录
     * <p>
     * 2.用户登录后判断绑定OpenId
     *
     * @param authUser 第三方登录封装类
     * @param longTerm 是否长时间有效
     * @return token
     * @throws NoPermissionException 不允许操作
     */
    private Token unionLoginCallback(ConnectAuthUser authUser, boolean longTerm) {

        try {
            Member member = null;
            //判断是否传递手机号，如果传递手机号则使用手机号登录
            if (StrUtil.isNotBlank(authUser.getPhone())) {
                member = memberService.findByMobile(authUser.getPhone());
            }
            //如果未查到手机号的会员则使用第三方登录
            if (member == null) {
                LambdaQueryWrapper<Connect> queryWrapper = new LambdaQueryWrapper<Connect>();
                //使用UnionId登录
                if (authUser.getToken() != null && StrUtil.isNotBlank(authUser.getToken().getUnionId())) {
                    queryWrapper.eq(Connect::getUnionId, authUser.getToken().getUnionId())
                            .eq(Connect::getUnionType, authUser.getSource());
                } else {
                    //使用OpenID登录
                    SourceEnum sourceEnum = SourceEnum.getSourceEnum(authUser.getSource(), authUser.getType());
                    queryWrapper.eq(Connect::getUnionId, authUser.getUuid())
                            .eq(Connect::getUnionType, sourceEnum.name());
                }

                //查询绑定关系
                Connect connect = this.getOne(queryWrapper);

                if (connect == null) {
                    member = owner.autoRegister(authUser);
                } else {
                    //查询会员
                    member = memberService.getById(connect.getUserId());
                    //如果未绑定会员，则把刚才查询到的联合登录表数据删除
                    if (member == null) {
                        this.remove(queryWrapper);
                        member = owner.autoRegister(authUser);
                    }
                }
            }
            memberConnectLogin(member, authUser);
//            //发送用户第三方登录消息
//            MemberConnectLoginMessage memberConnectLoginMessage = new MemberConnectLoginMessage();
//            memberConnectLoginMessage.setMember(member);
//            memberConnectLoginMessage.setConnectAuthUser(authUser);
//            String destination =
//                    rocketmqCustomProperties.getMemberTopic() + ":" + MemberTagsEnum.MEMBER_CONNECT_LOGIN.name();
//            //发送用户第三方登录消息
//            rocketMQTemplate.asyncSend(destination, JSONUtil.toJsonStr(memberConnectLoginMessage),
//                    RocketmqSendCallbackBuilder.commonCallback());

            return memberTokenGenerate.createToken(member, longTerm);
        } catch (Exception e) {
            log.error("联合登陆失败：", e);
            throw e;
        }
    }

    @Transactional
    public Member autoRegister(ConnectAuthUser authUser) {

        if (CharSequenceUtil.isEmpty(authUser.getNickname())) {
            authUser.setNickname(CommonUtil.getSpecialStr("用户"));
        }
        if (CharSequenceUtil.isEmpty(authUser.getAvatar())) {
            authUser.setAvatar("");
        }
        try {
//            String username = UuidUtils.getUUID();
            Member member = new Member(authUser.getUsername(), UuidUtils.getUUID(), authUser.getAvatar(), authUser.getNickname(),
                    authUser.getGender() != null ? Convert.toInt(authUser.getGender().getCode()) : 0, authUser.getPhone());
//            member.setPassword(DEFAULT_PASSWORD);
            // 发送会员注册信息
            memberService.registerHandler(member);

            return member;
        } catch (ServiceException e) {
            log.error("自动注册服务抛出异常：", e);
            throw e;
        } catch (Exception e) {
            log.error("自动注册异常：", e);
            throw new ServiceException(ResultCode.USER_AUTO_REGISTER_ERROR);
        }
    }

    public void memberConnectLogin(Member member, ConnectAuthUser authUser) {
        //保存UnionID
        if (StrUtil.isNotBlank(authUser.getToken().getUnionId())) {
            loginBindUser(member.getId(), authUser.getToken().getUnionId(), authUser.getSource().name());
        }
        //保存OpenID
        if (StrUtil.isNotBlank(authUser.getUuid())) {
            SourceEnum sourceEnum = SourceEnum.getSourceEnum(authUser.getSource(), authUser.getType());
            loginBindUser(member.getId(), authUser.getUuid(), sourceEnum.name());
        }
        //保存手机号，判断用户是否存手机号，如果不存在则保存手机号
        if (StrUtil.isNotBlank(authUser.getPhone())&&StrUtil.isBlank(member.getMobile())) {
            memberService.changeMobile(member.getId(),member.getMobile());
        }

    }
    /**
     * 获取微信小程序配置
     *
     * @return 微信小程序配置
     */
    private WechatConnectSettingItem getWechatMPSetting() {
        Setting setting = settingService.get(SettingEnum.WECHAT_CONNECT.name());

        WechatConnectSetting wechatConnectSetting = JSONUtil.toBean(setting.getSettingValue(),
                WechatConnectSetting.class);

        if (wechatConnectSetting == null) {
            throw new ServiceException(ResultCode.WECHAT_CONNECT_NOT_EXIST);
        }
        //寻找对应对微信小程序登录配置
        for (WechatConnectSettingItem wechatConnectSettingItem : wechatConnectSetting.getWechatConnectSettingItems()) {
            if (wechatConnectSettingItem.getClientType().equals(ClientTypeEnum.WECHAT_MP.name())) {
                return wechatConnectSettingItem;
            }
        }

        throw new ServiceException(ResultCode.WECHAT_CONNECT_NOT_EXIST);
    }


    /**
     * 解密，获取微信信息
     *
     * @param encryptedData 加密信息
     * @param sessionKey    微信sessionKey
     * @param iv            微信揭秘参数
     * @return 用户信息
     */
    public JSONObject getUserInfo(String encryptedData, String sessionKey, String iv) {

        log.info("encryptedData:{},sessionKey:{},iv:{}", encryptedData, sessionKey, iv);
        //被加密的数据
        byte[] dataByte = Base64.getDecoder().decode(encryptedData);
        //加密秘钥
        byte[] keyByte =  Base64.getDecoder().decode(sessionKey);
        //偏移量
        byte[] ivByte = Base64.getDecoder().decode(iv);
        try {
            //如果密钥不足16位，那么就补足.  这个if 中的内容很重要
            int base = 16;
            if (keyByte.length % base != 0) {
                int groups = keyByte.length / base + (keyByte.length % base != 0 ? 1 : 0);
                byte[] temp = new byte[groups * base];
                Arrays.fill(temp, (byte) 0);
                System.arraycopy(keyByte, 0, temp, 0, keyByte.length);
                keyByte = temp;
            }
            //初始化
            Security.addProvider(new BouncyCastleProvider());
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
            SecretKeySpec spec = new SecretKeySpec(keyByte, "AES");
            AlgorithmParameters parameters = AlgorithmParameters.getInstance("AES");
            parameters.init(new IvParameterSpec(ivByte));
            //初始化
            cipher.init(Cipher.DECRYPT_MODE, spec, parameters);
            byte[] resultByte = cipher.doFinal(dataByte);
            if (null != resultByte && resultByte.length > 0) {
                String result = new String(resultByte, StandardCharsets.UTF_8);
                return JSONUtil.parseObj(result);
            }
        } catch (Exception e) {
            log.error("解密，获取微信信息错误", e);
        }
        throw new ServiceException(ResultCode.USER_CONNECT_ERROR);
    }


}