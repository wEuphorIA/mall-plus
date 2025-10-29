package com.jzo2o.mall.common.file.service.impl;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.PageUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jzo2o.mall.common.enums.ResultCode;
import com.jzo2o.mall.common.exception.ServiceException;
import com.jzo2o.mall.common.file.mapper.FileMapper;
import com.jzo2o.mall.common.file.model.domain.File;
import com.jzo2o.mall.common.file.model.dto.FileOwnerDTO;
import com.jzo2o.mall.common.file.service.FileService;
import com.jzo2o.mall.common.file.service.plugin.FilePluginFactory;
import com.jzo2o.mall.common.model.AuthUser;
import com.jzo2o.mysql.utils.PageUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 文件管理业务层实现
 */
@Service
public class FileServiceImpl extends ServiceImpl<FileMapper, File> implements FileService {

    @Autowired
    private FilePluginFactory filePluginFactory;

    @Override
    public void batchDelete(List<String> ids) {

        LambdaQueryWrapper<File> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(File::getId, ids);

        List<File> files = this.list(queryWrapper);
        List<String> keys = new ArrayList<>();
        files.forEach(item -> keys.add(item.getFileKey()));
        filePluginFactory.filePlugin().deleteFile(keys);
        this.remove(queryWrapper);
    }

    @Override
    public void batchDeleteByDirectory(String directoryId) {
        LambdaQueryWrapper<File> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(File::getFileDirectoryId, directoryId);

        List<File> files = this.list(queryWrapper);
        List<String> keys = new ArrayList<>();
        files.forEach(item -> keys.add(item.getFileKey()));
        filePluginFactory.filePlugin().deleteFile(keys);
        this.remove(queryWrapper);
    }

    @Override
    public void batchDelete(List<String> ids, AuthUser authUser) {
        LambdaQueryWrapper<File> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(File::getId, ids);

        queryWrapper.eq(File::getUserEnums, authUser.getRole().name());
        //操作图片属性判定
        switch (authUser.getRole()) {
            case MEMBER:
                queryWrapper.eq(File::getOwnerId, authUser.getIdString());
                break;
            case STORE:
                queryWrapper.eq(File::getOwnerId, authUser.getStoreId());
                break;
            case MANAGER:
                break;
            default:
                throw new ServiceException(ResultCode.USER_AUTHORITY_ERROR);
        }
        List<File> files = this.list(queryWrapper);
        List<String> keys = new ArrayList<>();
        files.forEach(item -> keys.add(item.getFileKey()));
        filePluginFactory.filePlugin().deleteFile(keys);
        this.remove(queryWrapper);
    }

    @Override
    public IPage<File> customerPage(FileOwnerDTO fileOwnerDTO) {
        LambdaQueryWrapper<File> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(CharSequenceUtil.isNotEmpty(fileOwnerDTO.getName()), File::getName, fileOwnerDTO.getName())
                .eq(CharSequenceUtil.isNotEmpty(fileOwnerDTO.getFileDirectoryId()),File::getFileDirectoryId, fileOwnerDTO.getFileDirectoryId())
                .like(CharSequenceUtil.isNotEmpty(fileOwnerDTO.getFileKey()), File::getFileKey, fileOwnerDTO.getFileKey())
                .like(CharSequenceUtil.isNotEmpty(fileOwnerDTO.getFileType()), File::getFileType, fileOwnerDTO.getFileType())
                .between(CharSequenceUtil.isNotEmpty(fileOwnerDTO.getStartDate()) && CharSequenceUtil.isNotEmpty(fileOwnerDTO.getEndDate()),
                        File::getCreateTime, fileOwnerDTO.getStartDate(), fileOwnerDTO.getEndDate());
        return this.page(PageUtils.initPage(fileOwnerDTO), queryWrapper);
    }

    @Override
    public IPage<File> customerPageOwner(FileOwnerDTO fileOwnerDTO) {
        LambdaQueryWrapper<File> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CharSequenceUtil.isNotEmpty(fileOwnerDTO.getOwnerId()), File::getOwnerId, fileOwnerDTO.getOwnerId())
                .eq(File::getUserEnums, fileOwnerDTO.getUserEnums())
                .eq(CharSequenceUtil.isNotEmpty(fileOwnerDTO.getFileDirectoryId()),File::getFileDirectoryId, fileOwnerDTO.getFileDirectoryId())
                .like(CharSequenceUtil.isNotEmpty(fileOwnerDTO.getName()), File::getName, fileOwnerDTO.getName())
                .like(CharSequenceUtil.isNotEmpty(fileOwnerDTO.getFileKey()), File::getFileKey, fileOwnerDTO.getFileKey())
                .like(CharSequenceUtil.isNotEmpty(fileOwnerDTO.getFileType()), File::getFileType, fileOwnerDTO.getFileType())
                .between(CharSequenceUtil.isNotEmpty(fileOwnerDTO.getStartDate()) && CharSequenceUtil.isNotEmpty(fileOwnerDTO.getEndDate()),
                        File::getCreateTime, fileOwnerDTO.getStartDate(), fileOwnerDTO.getEndDate());
        return this.page(PageUtils.initPage(fileOwnerDTO), queryWrapper);
    }
}