package com.jzo2o.mall.member.model.dto;

import com.jzo2o.mall.member.model.domain.FreightTemplate;
import com.jzo2o.mall.member.model.domain.FreightTemplateChild;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * 店铺运费模板
 */
@Data
public class FreightTemplateDTO extends FreightTemplate {

    private static final long serialVersionUID = 2422138942308945537L;

    @ApiModelProperty(value = "运费详细规则")
    private List<FreightTemplateChild> freightTemplateChildList;

}
