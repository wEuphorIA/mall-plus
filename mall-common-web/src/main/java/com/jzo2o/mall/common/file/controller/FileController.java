package com.jzo2o.mall.common.file.controller;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jzo2o.common.utils.StringUtils;
import com.jzo2o.common.utils.UserContext;
import com.jzo2o.mall.common.enums.ResultCode;
import com.jzo2o.mall.common.enums.UserEnums;
import com.jzo2o.mall.common.exception.ServiceException;
import com.jzo2o.mall.common.file.model.domain.File;
import com.jzo2o.mall.common.file.model.dto.FileOwnerDTO;
import com.jzo2o.mall.common.file.service.FileService;
import com.jzo2o.mall.common.file.service.plugin.FilePluginFactory;
import com.jzo2o.mall.common.file.util.Base64DecodeMultipartFile;
import com.jzo2o.mall.common.model.AuthUser;
import com.jzo2o.mall.common.utils.CommonUtil;
import com.jzo2o.mall.system.enums.SettingEnum;
import com.jzo2o.mall.system.model.domain.Setting;
import com.jzo2o.mall.system.service.SettingService;
import com.jzo2o.redis.helper.Cache;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;
import java.util.Objects;


/**
 * 文件管理管理接口
 */
@Slf4j
@RestController
@Api(tags = "文件管理接口")
public class FileController {

    @Autowired
    private FileService fileService;

    @Autowired
    private SettingService settingService;
    @Autowired
    private FilePluginFactory filePluginFactory;

    @Autowired
    private Cache cache;

    @ApiOperation(value = "文件上传")
    @PostMapping(value = "/upload/file")
    public String upload(MultipartFile file,
                                        String base64,
                                        @RequestHeader String accessToken, @RequestParam(required=false) String directoryPath) {


        AuthUser authUser = UserContext.getCurrentUser();

        //如果用户未登录，则无法上传图片
        if (authUser == null) {
            throw new ServiceException(ResultCode.USER_AUTHORITY_ERROR);
        }
        if (file == null) {
            throw new ServiceException(ResultCode.FILE_NOT_EXIST_ERROR);
        }
        Setting setting = settingService.get(SettingEnum.OSS_SETTING.name());
        if (setting == null || CharSequenceUtil.isBlank(setting.getSettingValue())) {
            throw new ServiceException(ResultCode.OSS_NOT_EXIST);
        }
        if (CharSequenceUtil.isEmpty(file.getContentType())) {
            throw new ServiceException(ResultCode.IMAGE_FILE_EXT_ERROR);
        }


        if (!CharSequenceUtil.containsAny(Objects.requireNonNull(file.getContentType()).toLowerCase(), "image", "video")) {
            throw new ServiceException(ResultCode.FILE_TYPE_NOT_SUPPORT);
        }

        if (CharSequenceUtil.isNotBlank(base64)) {
            //base64上传
            file = Base64DecodeMultipartFile.base64Convert(base64);
        }
        String result;
        String fileKey = CommonUtil.rename(Objects.requireNonNull(file.getOriginalFilename()));
        File newFile = new File();
        try {
            InputStream inputStream = file.getInputStream();
            //上传至第三方云服务或服务器
            String scene = authUser.getRole().name();
            if (StrUtil.equalsAny(authUser.getRole().name(), UserEnums.MEMBER.name(), UserEnums.STORE.name(), UserEnums.SEAT.name())) {
                scene = scene + "/" + authUser.getIdString();
            }
            //mrt,2024-03-14:在店铺上传规格图片时没有传此参数，默认为店铺id
            if(StringUtils.isEmpty(directoryPath)){
                directoryPath = authUser.getStoreId();
            }

            fileKey = scene + "/" + directoryPath + "/" + fileKey;
            //上传至第三方云服务或服务器
            result = filePluginFactory.filePlugin().inputStreamUpload(inputStream, fileKey);
            //保存数据信息至数据库
            newFile.setName(file.getOriginalFilename());
            newFile.setFileSize(file.getSize());
            newFile.setFileType(file.getContentType());
            newFile.setFileKey(fileKey);
            newFile.setUrl(result);
            newFile.setCreateBy(authUser.getUsername());
            newFile.setUserEnums(authUser.getRole().name());
            //如果是店铺，则记录店铺id
            if (authUser.getRole().equals(UserEnums.STORE)) {
                newFile.setOwnerId(authUser.getStoreId());
            } else {
                newFile.setOwnerId(authUser.getIdString());
            }

            //存储文件目录
            if (StrUtil.isNotEmpty(directoryPath)) {
                if (directoryPath.indexOf("/") > 0) {
                    newFile.setFileDirectoryId(directoryPath.substring(directoryPath.lastIndexOf("/") + 1));
                } else {
                    newFile.setFileDirectoryId(directoryPath);
                }
            }
            fileService.save(newFile);
        } catch (Exception e) {
            log.error("文件上传失败", e);
            throw new ServiceException(ResultCode.OSS_EXCEPTION_ERROR);
        }
        return result;
    }

    @ApiOperation(value = "获取自己的图片资源")
    @GetMapping("/file")
    public IPage<File> getFileList(@RequestHeader String accessToken, FileOwnerDTO fileOwnerDTO) {

        AuthUser authUser = UserContext.getCurrentUser();
        if (authUser == null) {
//            ResponseUtil.output(ThreadContextHolder.getHttpResponse(), 403, ResponseUtil.resultMap(false,
//                    403, "登录已失效，请重新登录"));
            throw new ServiceException(ResultCode.USER_NOT_LOGIN);
        }
        //只有买家才写入自己id
        if (authUser.getRole().equals(UserEnums.MEMBER)) {
            fileOwnerDTO.setOwnerId(authUser.getIdString());
        }//如果是店铺，则写入店铺id
        else if (authUser.getRole().equals(UserEnums.STORE)) {
            fileOwnerDTO.setOwnerId(authUser.getStoreId());
        }
        fileOwnerDTO.setUserEnums(authUser.getRole().name());
        IPage<File> fileIPage = fileService.customerPageOwner(fileOwnerDTO);
        return fileIPage;
    }

    @ApiOperation(value = "文件重命名")
    @PostMapping(value = "/file/rename")
    public File upload(@RequestHeader String accessToken, String id, String newName) {

        AuthUser authUser = UserContext.getCurrentUser();
        File file = fileService.getById(id);
        file.setName(newName);
        //操作图片属性判定
        switch (authUser.getRole()) {
            case MEMBER:
                if (file.getOwnerId().equals(authUser.getIdString()) && file.getUserEnums().equals(authUser.getRole().name())) {
                    break;
                }
                throw new ServiceException(ResultCode.USER_AUTHORITY_ERROR);
            case STORE:
                if (file.getOwnerId().equals(authUser.getStoreId()) && file.getUserEnums().equals(authUser.getRole().name())) {
                    break;
                }
                throw new ServiceException(ResultCode.USER_AUTHORITY_ERROR);
            case MANAGER:
                break;
            default:
                throw new ServiceException(ResultCode.USER_AUTHORITY_ERROR);
        }
        fileService.updateById(file);
        return file;
    }

    @ApiOperation(value = "文件删除")
    @DeleteMapping(value = "/file/delete/{ids}")
    public void delete(@RequestHeader String accessToken, @PathVariable List<String> ids) {

        AuthUser authUser = UserContext.getCurrentUser();
        fileService.batchDelete(ids, authUser);
    }

}
