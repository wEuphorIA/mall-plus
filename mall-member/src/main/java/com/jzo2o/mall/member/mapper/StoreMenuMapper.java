package com.jzo2o.mall.member.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jzo2o.mall.member.model.domain.StoreMenu;
import com.jzo2o.mall.member.model.dto.StoreUserMenuDTO;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 菜单数据处理层
 */
public interface StoreMenuMapper extends BaseMapper<StoreMenu> {

    /**
     * 根据用户获取菜单列表
     *
     * @param userId 用户ID
     * @return 菜单列表
     */
    @Select("SELECT menu.* FROM ums_store_menu AS menu WHERE menu.id IN (" +
            "SELECT rm.menu_id FROM ums_store_menu_role AS rm WHERE rm.role_id IN (" +
            "SELECT ur.role_id FROM ums_clerk_role AS ur WHERE ur.clerk_id=#{userId}) OR rm.role_id IN (" +
            "SELECT dr.role_id FROM ums_store_department_role AS dr WHERE dr.department_id=(" +
            "SELECT department_id FROM ums_clerk AS au WHERE au.id = #{userId})))")
    List<StoreMenu> findByUserId(String userId);

    /**
     * 根据用户获取菜单权限
     *
     * @param userId 用户ID
     * @return 用户菜单VO列表
     */
    @Select("SELECT rm.is_super as is_super,m.*FROM ums_store_menu AS m INNER JOIN ums_store_menu_role AS rm ON rm.menu_id=m.id WHERE rm.role_id IN (" +
            "SELECT ur.role_id FROM ums_clerk_role AS ur WHERE ur.clerk_id=#{userId}) OR rm.role_id IN (" +
            "SELECT dr.role_id FROM ums_store_department_role AS dr INNER JOIN ums_clerk AS au ON au.department_id=dr.department_id " +
            "WHERE au.id=#{userId}) GROUP BY m.id,rm.is_super ORDER BY rm.is_super desc")
    List<StoreUserMenuDTO> getUserRoleMenu(String userId);
}