package com.jzo2o.mall.common.file.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jzo2o.mall.common.enums.UserEnums;
import com.jzo2o.mall.common.file.model.domain.FileDirectory;
import com.jzo2o.mall.common.file.model.dto.FileDirectoryDTO;

import java.util.List;

/**
 * 文件管理业务层
 */
public interface FileDirectoryService extends IService<FileDirectory> {

    /**
     * 添加目录
     *
     * @param userEnum
     * @param id
     * @param ownerName
     */
    void addFileDirectory(UserEnums userEnum, String id, String ownerName);

    /**
     * 获取文件目录
     *
     * @param ownerId 拥有者
     * @return
     */
    List<FileDirectoryDTO> getFileDirectoryList(String ownerId);
}