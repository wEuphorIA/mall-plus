package com.jzo2o.mall.search.model.domain;

import co.elastic.clients.elasticsearch._types.mapping.FieldType;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 商品属性索引
 **/
@Data
@NoArgsConstructor
public class EsGoodsAttribute implements Serializable {

    private static final long serialVersionUID = 4018042777559970062L;

    /**
     * 属性参数：0->规格；1->参数
     */
    private Integer type;

    /**
     * 属性名称
     */
    private String nameId;

    /**
     * 属性名称
     */
    private String name;

    /**
     * 属性值
     */
    private String valueId;

    /**
     * 属性值
     */
    private String value;


    /**
     * 排序
     */
    private Integer sort;

    public EsGoodsAttribute(Integer type, String nameId, String name, String valueId, String value, Integer sort) {
        this.type = type;
        this.nameId = nameId;
        this.name = name;
        this.valueId = valueId;
        this.value = value;
        this.sort = sort;
    }
}
