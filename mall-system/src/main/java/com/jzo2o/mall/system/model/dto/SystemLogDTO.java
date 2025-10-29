package com.jzo2o.mall.system.model.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.io.Serializable;


/**
 * 日志
 */
@Data
//@Document(indexName = "#{@elasticsearchProperties.indexPrefix}_" + EsSuffix.LOGS_INDEX_NAME)
@ToString
@NoArgsConstructor
@Accessors(chain = true)
public class SystemLogDTO implements Serializable {

//
//    private static final long serialVersionUID = -8995552592401630086L;
//
//    @Id
//    @ApiModelProperty(value = "id")
//    private String id;
//
//
//    @ApiModelProperty(value = "日志记录时间")
//    @Field(type = FieldType.Long)
//    private Long createTime = new Date().getTime();
//
//    @ApiModelProperty(value = "请求用户")
//    @Field(type = FieldType.Text)
//    private String username;
//
//    @ApiModelProperty(value = "请求路径")
//    @Field(type = FieldType.Text)
//    private String requestUrl;
//
//    @ApiModelProperty(value = "请求参数")
//    @Field(type = FieldType.Text)
//    private String requestParam;
//
//    @ApiModelProperty(value = "响应参数")
//    @Field(type = FieldType.Text)
//    private String responseBody;
//
//    @ApiModelProperty(value = "ip")
//    @Field(type = FieldType.Keyword)
//    private String ip;
//
//    @ApiModelProperty(value = "方法操作名称")
//    @Field(type = FieldType.Keyword)
//    private String name;
//
//
//    @ApiModelProperty(value = "请求类型")
//    @Field(type = FieldType.Keyword)
//    private String requestType;
//
//
//    @ApiModelProperty(value = "自定义日志内容")
//    @Field(type = FieldType.Text)
//    private String customerLog;
//
//
//    @ApiModelProperty(value = "ip信息")
//    @Field(type = FieldType.Text)
//    private String ipInfo;
//
//    @ApiModelProperty(value = "花费时间")
//    @Field(type = FieldType.Integer)
//    private Integer costTime;
//
//    @ApiModelProperty(value = "商家")
//    @Field(type = FieldType.Long)
//    private Long storeId = -1L;
//
//    /**
//     * 转换请求参数为Json
//     *
//     * @param paramMap
//     */
//    public void setMapToParams(Map<String, String[]> paramMap) {
//
//        this.requestParam = ObjectUtil.mapToString(paramMap);
//    }
}
