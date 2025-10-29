package com.jzo2o.mall.system.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 物流信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TracesDTO {

    /**
     * 物流公司
     */
    private String shipper;

    /**
     * 物流单号
     */
    private String logisticCode;

    /**
     * 物流详细信息
     */
    private List<Map> traces;
}
