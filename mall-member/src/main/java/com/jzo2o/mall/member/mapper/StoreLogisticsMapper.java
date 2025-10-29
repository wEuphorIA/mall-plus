package com.jzo2o.mall.member.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jzo2o.mall.member.model.domain.StoreLogistics;
import com.jzo2o.mall.member.model.dto.StoreLogisticsDTO;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 物流公司数据处理层
 */
public interface StoreLogisticsMapper extends BaseMapper<StoreLogistics> {

    /**
     * 获取店铺选择的物流公司
     *
     * @param storeId 店铺ID
     * @return 物流公司列表
     */
    @Select("SELECT l.id as logistics_id,l.name FROM ams_logistics l RIGHT JOIN  ums_store_logistics sl ON l.id=sl.logistics_id WHERE sl.store_id=#{storeId} AND l.disabled='OPEN'")
    List<StoreLogisticsDTO> getSelectedStoreLogistics(String storeId);

    /**
     * 店铺已选择的物流公司名称列表
     *
     * @param storeId 店铺ID
     * @return 店铺已选择的物流公司名称列表
     */
    @Select("SELECT l.name FROM ams_logistics l RIGHT JOIN  ums_store_logistics sl ON l.id=sl.logistics_id WHERE sl.store_id=#{storeId} AND l.disabled='OPEN'")
    List<String> getSelectedStoreLogisticsName(String storeId);

    /**
     * 获取店铺地址VO列表
     *
     * @param storeId 店铺列表
     * @return 店铺地址VO列表
     */
    @Select("SELECT id as logistics_id , `name` , ( SELECT sl.id FROM ums_store_logistics sl WHERE l.id = sl.logistics_id AND sl.store_id=#{storeId} ) AS selected,(SELECT sl.face_sheet_flag FROM ums_store_logistics sl WHERE l.id = sl.logistics_id AND sl.store_id = #{storeId}) as face_sheet_flag FROM ams_logistics l WHERE l.disabled='OPEN';")
    //@Select("SELECT *, ( SELECT sl.id FROM ums_store_logistics sl WHERE l.id = sl.logistics_id AND sl.store_id=#{storeId} ) AS selected FROM ams_logistics l WHERE l.disabled='OPEN';")
    List<StoreLogisticsDTO> getStoreLogistics(String storeId);

    /**
     * 店铺已选择的物流公司
     * @param storeId 店铺Id
     * @return 物流公司列表
     */
    @Select("SELECT sl.logistics_id,l.name,sl.face_sheet_flag FROM ams_logistics l INNER JOIN ums_store_logistics sl on sl.logistics_id=l.id WHERE l.disabled = 'OPEN' AND store_id=#{storeId};")
    List<StoreLogisticsDTO> getOpenStoreLogistics(String storeId);

    /**
     * 店铺未选择的物流公司
     * @param storeId 店铺Id
     * @return 物流公司列表
     */
    @Select("SELECT id as logistics_id,name FROM ams_logistics WHERE id not in(SELECT logistics_id FROM ums_store_logistics WHERE store_id=#{storeId}) AND disabled = 'OPEN'")
    List<StoreLogisticsDTO> getCloseStroreLogistics(String storeId);

    /**
     * 获取店铺选择的物流公司并使用了电子面单
     *
     * @param storeId 店铺ID
     * @return 物流公司列表
     */
    @Select("SELECT id as logistics_id , `name` FROM ams_logistics WHERE disabled='OPEN'" +
            "AND id in(SELECT logistics_id FROM ums_store_logistics WHERE store_id=#{storeId} and face_sheet_flag=1)")
    List<StoreLogisticsDTO> getSelectedStoreLogisticsUseFaceSheet(String storeId);


}