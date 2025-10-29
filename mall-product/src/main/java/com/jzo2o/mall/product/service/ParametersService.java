package com.jzo2o.mall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jzo2o.mall.product.model.domain.Parameters;

/**
 * 商品参数业务层
 */
public interface ParametersService extends IService<Parameters> {



    /**
     * 更新参数组信息
     *
     * @param parameters 参数组信息
     * @return 是否更新成功
     */
    boolean updateParameter(Parameters parameters);

}