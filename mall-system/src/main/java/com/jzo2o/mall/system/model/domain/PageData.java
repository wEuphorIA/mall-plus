package com.jzo2o.mall.system.model.domain;

import cn.hutool.http.HtmlUtil;
import com.baomidou.mybatisplus.annotation.TableName;
import com.jzo2o.common.utils.StringUtils;
import com.jzo2o.mall.common.enums.ClientTypeEnum;
import com.jzo2o.mall.common.enums.PageEnum;
import com.jzo2o.mall.common.enums.SwitchEnum;
import com.jzo2o.mysql.domain.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * 页面数据DO
 */
@Data
@TableName("ams_page_data")
@ApiModel(value = "页面数据DO")
@NoArgsConstructor
public class PageData extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "页面名称")

    private String name;

    @ApiModelProperty(value = "页面数据")
    private String pageData;

    /**
     * @see SwitchEnum
     */
    @ApiModelProperty(value = "页面开关状态", allowableValues = "OPEN,CLOSE")
    private String pageShow;

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

    @ApiModelProperty(value = "值")
    private String num;

    public PageData(String name, String pageClientType, String pageData, String num) {
        this.name = name;
        this.pageClientType = pageClientType;
        this.pageData = pageData;
        this.num = num;
        this.pageShow = SwitchEnum.CLOSE.name();
        this.pageType = PageEnum.STORE.name();
    }

    public String getPageData() {
        if (StringUtils.isNotEmpty(pageData)) {
            return HtmlUtil.unescape(pageData);
        }
        return pageData;
    }
}