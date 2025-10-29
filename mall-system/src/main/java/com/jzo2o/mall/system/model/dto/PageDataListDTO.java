package com.jzo2o.mall.system.model.dto;

import com.jzo2o.mall.common.enums.SwitchEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 楼层装修数据VO
 */
@Data
public class PageDataListDTO {

    @ApiModelProperty(value = "页面ID")
    private String id;
    @ApiModelProperty(value = "页面名称")
    private String name;
    /**
     * @see SwitchEnum
     */
    @ApiModelProperty(value = "页面开关状态", allowableValues = "OPEN,CLOSE")
    private String pageShow;
}
