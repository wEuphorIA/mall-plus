package com.jzo2o.mall.search.model.dto;

import lombok.Data;

import java.util.List;

/**
 * 搜索相关商品品牌名称，分类名称及属性
 **/
@Data
public class EsGoodsRelatedInfoDTO {

    /**
     * 分类集合
     */
    List<SelectorOptionsDTO> categories;

    /**
     * 品牌集合
     */
    List<SelectorOptionsDTO> brands;

    /**
     * 参数集合
     */
    List<ParamOptionsDTO> paramOptions;


}
