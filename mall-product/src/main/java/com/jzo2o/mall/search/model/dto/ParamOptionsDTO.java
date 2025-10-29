package com.jzo2o.mall.search.model.dto;

import lombok.Data;

import java.util.List;

/**
 * 参数属性选择器
 **/
@Data
public class ParamOptionsDTO {

    private String key;

    private List<String> values;

}
