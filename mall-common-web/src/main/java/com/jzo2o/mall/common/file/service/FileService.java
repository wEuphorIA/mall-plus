package com.jzo2o.mall.common.file.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jzo2o.mall.common.file.model.domain.File;
import com.jzo2o.mall.common.file.model.dto.FileOwnerDTO;
import com.jzo2o.mall.common.model.AuthUser;

import java.util.List;

/**
 * 文件管理业务层
 */
public interface FileService extends IService<File> {


    /**
     * 批量删除
     *
     * @param ids
     */
    void batchDelete(List<String> ids);
    /**
     * 根据文件夹ID批量删除
     *
     * @param directoryId 文件夹ID
     */
    void batchDeleteByDirectory(String directoryId);

    /**
     * 所有者批量删除
     *
     * @param ids      ID
     * @param authUser 操作者
     */
    void batchDelete(List<String> ids, AuthUser authUser);


    /**
     * 自定义搜索分页
     *

     * @param fileOwnerDTO 文件查询

     * @return
     */
    IPage<File> customerPage(FileOwnerDTO fileOwnerDTO);

    /**
     * 所属文件数据查询
     *
     * @param ownerDTO 文件查询
     * @return
     */
    IPage<File> customerPageOwner(FileOwnerDTO ownerDTO);

}