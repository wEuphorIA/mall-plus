package com.jzo2o.mall.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jzo2o.mall.system.model.domain.Menu;
import com.jzo2o.mall.system.model.dto.UserMenuDTO;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 菜单数据处理层
 */
public interface MenuMapper extends BaseMapper<Menu> {

    /**
     * 根据用户获取菜单列表
     *
     * @param userId 用户ID
     * @return 菜单列表
     */
    @Select("SELECT menu.* FROM ams_menu AS menu WHERE menu.id IN (" +
            "SELECT rm.menu_id FROM ams_role_menu AS rm WHERE rm.role_id IN (" +
            "SELECT ur.role_id FROM ams_user_role AS ur WHERE ur.user_id=#{userId}) OR rm.role_id IN (" +
            "SELECT dr.role_id FROM ams_department_role AS dr WHERE dr.department_id =(" +
            "SELECT department_id FROM ams_admin_user AS au WHERE au.id = #{userId})))")
    List<Menu> findByUserId(String userId);

    /**
     * 根据用户获取菜单权限
     *
     * @param userId 用户ID
     * @return 用户菜单VO列表
     */
    @Select("SELECT rm.is_super as is_super,m.*FROM ams_menu AS m INNER JOIN ams_role_menu AS rm ON rm.menu_id=m.id WHERE rm.role_id IN (" +
            "SELECT ur.role_id FROM ams_user_role AS ur WHERE ur.user_id=#{userId}) OR rm.role_id IN (" +
            "SELECT dr.role_id FROM ams_department_role AS dr INNER JOIN ams_admin_user AS au ON au.department_id=dr.department_id " +
            "WHERE au.id=#{userId}) GROUP BY m.id,rm.is_super ORDER BY rm.is_super desc")
    List<UserMenuDTO> getUserRoleMenu(String userId);
}