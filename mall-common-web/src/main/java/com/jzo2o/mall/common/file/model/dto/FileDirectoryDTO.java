package com.jzo2o.mall.common.file.model.dto;

import cn.hutool.core.bean.BeanUtil;
import com.jzo2o.mall.common.file.model.domain.FileDirectory;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class FileDirectoryDTO extends FileDirectory {

    @ApiModelProperty(value = "文件目录列表")
    private List<FileDirectory> children= new ArrayList<>();

    public FileDirectoryDTO(FileDirectory fileDirectory){
        BeanUtil.copyProperties(fileDirectory, this);
    }
}
