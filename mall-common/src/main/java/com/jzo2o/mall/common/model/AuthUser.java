package com.jzo2o.mall.common.model;

import com.jzo2o.common.model.CurrentUser;
import com.jzo2o.common.model.CurrentUserInfo;
import com.jzo2o.mall.common.enums.UserEnums;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthUser implements CurrentUser,Serializable {

    private static final long serialVersionUID = 582441893336003319L;

    /**
     * accessToken
     */
    private String accessToken;
    /**
     * 用户名
     */
    private String username;

    /**
     * 昵称
     */
    private String nickName;

    /**
     * 头像
     */
    private String face;

    /**
     * id
     */
    private String id;

    /**
     * 长期有效（用于手机app登录场景或者信任场景等）
     */
    private Boolean longTerm = false;

    /**
     * @see UserEnums
     * 角色
     */
    private UserEnums role;

    /**
     * 如果角色是商家，则存在此店铺id字段
     * storeId
     */
    private String storeId;
    /**
     * 如果角色是商家，则存在此店铺id字段
     * clerkId
     */
    private String clerkId;

    /**
     * 如果角色是商家，则存在此店铺名称字段
     * storeName
     */
    private String storeName;

    /**
     * 是否是超级管理员
     */
    private Boolean isSuper = false;

    /**
     * 租户id
     */
    private String tenantId;


    public AuthUser(String username, String id, String nickName, String face, UserEnums role) {
        this.username = username;
        this.face = face;
        this.id = id;
        this.role = role;
        this.nickName = nickName;
    }


    @Override
    public String getIdString() {
        return id;
    }
    public Long getId() {
        return Long.parseLong(id);
    }

    @Override
    public String getName() {
        return username;
    }

    @Override
    public void setName(String name) {
        this.username = name;
    }

    @Override
    public String getAvatar() {
        return null;
    }

    @Override
    public Integer getUserType() {
        return null;
    }
}
