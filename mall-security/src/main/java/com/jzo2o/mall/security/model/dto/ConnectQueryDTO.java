package com.jzo2o.mall.security.model.dto;

import lombok.Builder;
import lombok.Data;

/**
 * ConnectQueryDTO
 */
@Data
@Builder
public class ConnectQueryDTO {

    /**
     * 用户id
     */
    private String userId;

    /**
     * 第三方id
     */
    private String unionId;

    /**
     * 联合登陆类型
     */
    private String unionType;

}
