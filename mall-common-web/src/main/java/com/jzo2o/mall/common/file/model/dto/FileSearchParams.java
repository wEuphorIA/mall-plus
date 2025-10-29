package com.jzo2o.mall.common.file.model.dto;

import com.jzo2o.mall.common.file.model.domain.File;
import com.jzo2o.mysql.domain.PageVO;
import com.jzo2o.mysql.domain.SearchVO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class FileSearchParams extends PageVO {

    @ApiModelProperty(value = "文件")
    private File file;
    @ApiModelProperty(value = "搜索VO")
    private SearchVO searchVO;
    @ApiModelProperty(value = "文件夹ID")
    private String fileDirectoryId;
}
