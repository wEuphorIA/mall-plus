package com.jzo2o.mall.search.model.dto;

import lombok.Data;

import java.util.List;

/**
 **/
@Data
public class SelectorOptionsDTO {

    private String name;

    private String value;

    private String url;

    private List<SelectorOptionsDTO> otherOptions;


}
