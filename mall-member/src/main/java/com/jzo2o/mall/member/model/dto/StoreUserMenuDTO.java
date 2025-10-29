package com.jzo2o.mall.member.model.dto;

import com.jzo2o.mall.member.model.domain.StoreMenu;
import lombok.Data;

/**
 * RoleMenuVO
 */
@Data
public class StoreUserMenuDTO extends StoreMenu {

    private static final long serialVersionUID = -7478870595109016162L;

    /**
     * 是否是超级管理员
     */
    private Boolean isSuper;

    public Boolean getSuper() {
        if (this.isSuper == null) {
            return false;
        }
        return isSuper;
    }
}
