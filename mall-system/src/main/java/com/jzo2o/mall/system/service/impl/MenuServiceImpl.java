package com.jzo2o.mall.system.service.impl;

import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jzo2o.mall.common.cache.CachePrefix;
import com.jzo2o.mall.common.enums.ResultCode;
import com.jzo2o.mall.common.enums.UserEnums;
import com.jzo2o.mall.common.exception.ServiceException;
import com.jzo2o.mall.common.model.AuthUser;
import com.jzo2o.mall.system.mapper.MenuMapper;
import com.jzo2o.mall.system.model.domain.Menu;
import com.jzo2o.mall.system.model.domain.RoleMenu;
import com.jzo2o.mall.system.model.dto.MenuDTO;
import com.jzo2o.mall.system.model.dto.MenuSearchParamsDTO;
import com.jzo2o.mall.system.model.dto.UserMenuDTO;
import com.jzo2o.mall.system.service.MenuService;
import com.jzo2o.mall.system.service.RoleMenuService;
import com.jzo2o.mysql.domain.SearchVO;
import com.jzo2o.mysql.utils.PageUtils;
import com.jzo2o.redis.helper.Cache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 权限业务层实现
 */
@Slf4j
@Service
public class MenuServiceImpl extends ServiceImpl<MenuMapper, Menu> implements MenuService {
    /**
     * 菜单角色
     */
    @Autowired
    private RoleMenuService roleMenuService;


    @Autowired
    private Cache cache;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteIds(List<String> ids) {
        QueryWrapper<RoleMenu> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("menu_id", ids);
        //如果已有角色绑定菜单，则不能直接删除
        if (roleMenuService.count(queryWrapper) > 0) {
            throw new ServiceException(ResultCode.PERMISSION_MENU_ROLE_ERROR);
        }
        cache.vagueDel(CachePrefix.USER_MENU.getPrefix(UserEnums.MANAGER));
        cache.vagueDel(CachePrefix.PERMISSION_LIST.getPrefix(UserEnums.MANAGER));
        this.removeByIds(ids);
    }


    @Override
    public List<MenuDTO> findUserTree(AuthUser authUser) {
        Objects.requireNonNull(authUser);
        if (Boolean.TRUE.equals(authUser.getIsSuper())) {
            return this.tree();
        }
        List<Menu> userMenus = this.findUserList(authUser.getIdString());
        return this.tree(userMenus);
    }

    @Override
    public List<Menu> findUserList(String userId) {
//        return this.baseMapper.findByUserId(userId);
        String cacheKey = CachePrefix.USER_MENU.getPrefix(UserEnums.MANAGER) + userId;
        List<Menu> menuList = (List<Menu>) cache.get(cacheKey);
        if (menuList == null) {
            menuList = this.baseMapper.findByUserId(userId);
            //每5分钟重新确认用户权限
            cache.put(cacheKey, menuList, 300L);
        }
        return menuList;
    }

    /**
     * 添加更新菜单
     *
     * @param menu 菜单数据
     * @return 是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveOrUpdateMenu(Menu menu) {
        cache.vagueDel(CachePrefix.USER_MENU.getPrefix(UserEnums.MANAGER));
        cache.vagueDel(CachePrefix.PERMISSION_LIST.getPrefix(UserEnums.MANAGER));
        return this.saveOrUpdate(menu);
    }

    @Override
    public List<UserMenuDTO> findAllMenu(String userId) {
        return this.baseMapper.getUserRoleMenu(userId);
    }

    @Override
    public List<Menu> findByRoleIds(String roleId) {
        QueryWrapper<Menu> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("role_id", roleId);
        return this.list(queryWrapper);
    }

    @Override
    public List<Menu> searchList(MenuSearchParamsDTO menuSearchParams) {
        //title 需要特殊处理
        String title = null;
        if (CharSequenceUtil.isNotEmpty(menuSearchParams.getTitle())) {
            title = menuSearchParams.getTitle();
            menuSearchParams.setTitle(null);
        }
        QueryWrapper<Menu> queryWrapper = PageUtils.initWrapper(menuSearchParams, new SearchVO());
        if (CharSequenceUtil.isNotEmpty(title)) {
            queryWrapper.like("title", title);
        }
        queryWrapper.orderByDesc("sort_order");
        return this.baseMapper.selectList(queryWrapper);
    }


    @Override
    public List<MenuDTO> tree() {
        try {
            List<Menu> menus = this.list();
            return tree(menus);
        } catch (Exception e) {
            log.error("菜单树错误", e);
        }
        return Collections.emptyList();
    }

    /**
     * 传入自定义菜单集合
     *
     * @param menus 自定义菜单集合
     * @return 修改后的自定义菜单集合
     */
    private List<MenuDTO> tree(List<Menu> menus) {
        List<MenuDTO> tree = new ArrayList<>();
        menus.forEach(item -> {
            if (item.getLevel() == 0) {
                MenuDTO treeItem = new MenuDTO(item);
                initChild(treeItem, menus);
                tree.add(treeItem);
            }
        });
        //对一级菜单排序
        tree.sort(Comparator.comparing(Menu::getSortOrder));
        return tree;
    }

    /**
     * 递归初始化子树
     *
     * @param tree  树结构
     * @param menus 数据库对象集合
     */
    private void initChild(MenuDTO tree, List<Menu> menus) {
        if (menus == null) {
            return;
        }
        menus.stream()
                .filter(item -> (item.getParentId().equals(tree.getId())))
                .forEach(child -> {
                    MenuDTO childTree = new MenuDTO(child);
                    initChild(childTree, menus);
                    tree.getChildren().add(childTree);
                });
    }

}
