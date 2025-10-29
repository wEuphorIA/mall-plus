package com.jzo2o.mall.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jzo2o.mall.system.model.dto.SystemLogDTO;
import com.jzo2o.mysql.domain.PageVO;
import com.jzo2o.mysql.domain.SearchVO;

import java.util.List;

/**
 * 系统日志业务层
 */
public interface SystemLogService {

    /**
     * 添加日志
     *
     * @param systemLogDTO
     * @return
     */
    void saveLog(SystemLogDTO systemLogDTO);

    /**
     * 通过id删除日志
     *
     * @param id
     */
    void deleteLog(List<String> id);

    /**
     * 删除全部日志
     */
    void flushAll();

    /**
     * 分页搜索获取日志
     *
     * @param key          关键字
     * @param searchVo     查询VO
     * @param pageVO       分页
     * @param operatorName 操作人
     * @param storeId      店铺ID
     * @return 日志分页
     */
    IPage<SystemLogDTO> queryLog(String storeId, String operatorName, String key, SearchVO searchVo, PageVO pageVO);
}
