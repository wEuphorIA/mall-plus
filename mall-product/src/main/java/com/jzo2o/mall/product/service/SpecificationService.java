package com.jzo2o.mall.product.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.jzo2o.mall.product.model.domain.Specification;

import java.util.List;

/**
 * 规格业务层
 */
public interface SpecificationService extends IService<Specification> {

    /**
     * 删除规格
     *
     * @param ids 规格ID
     * @return 是否删除成功
     */
    boolean deleteSpecification(List<String> ids);

}