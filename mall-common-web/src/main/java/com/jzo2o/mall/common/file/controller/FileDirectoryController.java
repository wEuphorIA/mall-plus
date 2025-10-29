package com.jzo2o.mall.common.file.controller;

import com.jzo2o.common.model.CurrentUserInfo;
import com.jzo2o.common.utils.UserContext;
import com.jzo2o.mall.common.enums.ResultUtil;
import com.jzo2o.mall.common.file.model.domain.FileDirectory;
import com.jzo2o.mall.common.file.model.dto.FileDirectoryDTO;
import com.jzo2o.mall.common.file.service.FileDirectoryService;
import com.jzo2o.mall.common.file.service.FileService;
import com.jzo2o.mall.common.model.AuthUser;
import com.jzo2o.mall.common.model.ResultMessage;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 文件目录管理接口
 */
@RestController
@Api(tags = "文件目录管理接口")
@RequestMapping("/resource/fileDirectory")
@RequiredArgsConstructor
public class FileDirectoryController {

    private final FileDirectoryService fileDirectoryService;
    private final FileService fileService;

    @ApiOperation(value = "获取文件目录列表")
    @GetMapping
    public List<FileDirectoryDTO> getSceneFileList() {
        List<FileDirectoryDTO> fileDirectoryList = fileDirectoryService.getFileDirectoryList(UserContext.getCurrentUser().getIdString());
        return fileDirectoryList;
    }

    @ApiOperation(value = "添加文件目录")
    @PostMapping
    public FileDirectory addSceneFileList(@RequestBody @Valid FileDirectory fileDirectory) {
        AuthUser authUser = UserContext.getCurrentUser();
        fileDirectory.setDirectoryType(authUser.getRole().name());
        fileDirectory.setOwnerId(UserContext.getCurrentUser().getIdString());
        fileDirectoryService.save(fileDirectory);
        return fileDirectory;
    }

    @ApiOperation(value = "修改文件目录")
    @PutMapping
    public FileDirectory editSceneFileList(@RequestBody @Valid FileDirectory fileDirectory) {
        AuthUser authUser = UserContext.getCurrentUser();
        fileDirectory.setDirectoryType(authUser.getRole().name());
        fileDirectory.setOwnerId(UserContext.getCurrentUser().getIdString());
        fileDirectoryService.updateById(fileDirectory);
        return fileDirectory;
    }

    @ApiOperation(value = "删除文件目录")
    @DeleteMapping("/{id}")
    public void deleteSceneFileList(@PathVariable String id) {
        //删除文件夹下面的图片
        fileService.batchDeleteByDirectory(id);
        //删除目录
        fileDirectoryService.removeById(id);
    }

}
