package com.jzo2o.mall.system.model.dto;

import com.jzo2o.mall.common.enums.ClientTypeEnum;
import com.jzo2o.mall.common.enums.PageEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 楼层装修数据VO
 *
 */
@Data
public class PageDataDTO {

    @ApiModelProperty(value = "页面数据")
    private String pageData;

    @ApiModelProperty(value = "值")
    private String num;

    /**
     * @see PageEnum
     */
    @ApiModelProperty(value = "页面类型", allowableValues = "INDEX,STORE,SPECIAL")
    private String pageType;

    /**
     * @see ClientTypeEnum
     */
    @ApiModelProperty(value = "客户端类型", allowableValues = "PC,H5,WECHAT_MP,APP")
    private String pageClientType;

    public PageDataDTO(){

    }
    public PageDataDTO(String pageType) {
        this.pageType = pageType;
    }
}
