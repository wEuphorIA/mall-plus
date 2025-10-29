package com.jzo2o.mall.product.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jzo2o.mall.product.model.domain.DraftGoods;
import com.jzo2o.mall.product.model.dto.DraftGoodsDTO;
import com.jzo2o.mall.product.model.dto.DraftGoodsSearchParamsDTO;
import com.jzo2o.mall.product.model.dto.DraftGoodsViewDTO;

/**
 * 草稿商品业务层
 **/
public interface DraftGoodsService extends IService<DraftGoods> {

    /**
     * 添加草稿商品
     *
     * @param draftGoods 草稿商品
     * @return 是否添加成功
     */
    boolean addGoodsDraft(DraftGoodsDTO draftGoods);

    /**
     * 更新草稿商品
     *
     * @param draftGoods 草稿商品
     * @return 是否更新成功
     */
    boolean updateGoodsDraft(DraftGoodsDTO draftGoods);

    /**
     * 保存草稿商品
     *
     * @param draftGoodsVO 草稿商品
     */
    void saveGoodsDraft(DraftGoodsDTO draftGoodsVO);

    /**
     * 根据ID删除草稿商品
     *
     * @param id 草稿商品ID
     */
    void deleteGoodsDraft(String id);

    /**
     * 获取草稿商品详情
     *
     * @param id 草稿商品ID
     * @return 草稿商品详情
     */
    DraftGoodsViewDTO getDraftGoods(String id);

    /**
     * 分页获取草稿商品
     *
     * @param searchParams 查询参数
     * @return 草稿商品
     */
    IPage<DraftGoods> getDraftGoods(DraftGoodsSearchParamsDTO searchParams);

}
