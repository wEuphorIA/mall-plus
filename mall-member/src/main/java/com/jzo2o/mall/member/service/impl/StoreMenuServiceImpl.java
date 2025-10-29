package com.jzo2o.mall.member.service.impl;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.PageUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jzo2o.common.utils.UserContext;
import com.jzo2o.mall.common.cache.CachePrefix;
import com.jzo2o.mall.common.enums.ResultCode;
import com.jzo2o.mall.common.enums.UserEnums;
import com.jzo2o.mall.common.exception.ServiceException;
import com.jzo2o.mall.common.model.AuthUser;
import com.jzo2o.mall.member.mapper.StoreMenuMapper;
import com.jzo2o.mall.member.model.domain.Clerk;
import com.jzo2o.mall.member.model.domain.StoreMenu;
import com.jzo2o.mall.member.model.domain.StoreMenuRole;
import com.jzo2o.mall.member.model.dto.MenuSearchParamsDTO;
import com.jzo2o.mall.member.model.dto.StoreMenuDTO;
import com.jzo2o.mall.member.model.dto.StoreUserMenuDTO;
import com.jzo2o.mall.member.service.ClerkService;
import com.jzo2o.mall.member.service.StoreMenuRoleService;
import com.jzo2o.mall.member.service.StoreMenuService;
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
public class StoreMenuServiceImpl extends ServiceImpl<StoreMenuMapper, StoreMenu> implements StoreMenuService {
    /**
     * 菜单角色
     */
    @Autowired
    private StoreMenuRoleService storeMenuRoleService;

    @Autowired
    private Cache cache;

    /**
     * 店员
     */
    @Autowired
    private ClerkService clerkService;



    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteIds(List<String> ids) {
        QueryWrapper<StoreMenuRole> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("menu_id", ids);
        //如果已有角色绑定菜单，则不能直接删除
        if (storeMenuRoleService.count(queryWrapper) > 0) {
            throw new ServiceException(ResultCode.PERMISSION_MENU_ROLE_ERROR);
        }
        cache.vagueDel(CachePrefix.PERMISSION_LIST.getPrefix(UserEnums.STORE));
        cache.vagueDel(CachePrefix.STORE_USER_MENU.getPrefix());
        this.removeByIds(ids);
    }


    @Override
    public List<StoreMenuDTO> findUserTree() {
        AuthUser tokenUser =  UserContext.getCurrentUser();
        if (Boolean.TRUE.equals(tokenUser.getIsSuper())) {
            return this.tree();
        }
        //获取当前登录用户的店员信息
        Clerk clerk = clerkService.getOne(new LambdaQueryWrapper<Clerk>().eq(Clerk::getMemberId, tokenUser.getIdString()));
        //获取当前店员角色的菜单列表
        List<StoreMenu> userMenus = this.findUserList(tokenUser.getIdString(), clerk.getId());
        return this.tree(userMenus);
    }

    @Override
    public List<StoreMenu> findUserList(String userId, String clerkId) {
        String cacheKey = CachePrefix.STORE_USER_MENU.getPrefix() + userId;
        List<StoreMenu> menuList = (List<StoreMenu>) cache.get(cacheKey);
        if (menuList == null || menuList.isEmpty()) {
            menuList = this.baseMapper.findByUserId(clerkId);
            cache.put(cacheKey, menuList);
        }
        return menuList;
    }

    /**
     * 添加更新菜单
     *
     * @param storeMenu 菜单数据
     * @return 是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveOrUpdateMenu(StoreMenu storeMenu) {
        if (CharSequenceUtil.isNotEmpty(storeMenu.getId())) {
            cache.vagueDel(CachePrefix.PERMISSION_LIST.getPrefix(UserEnums.STORE));
            cache.vagueDel(CachePrefix.STORE_USER_MENU.getPrefix());
        }
        return this.saveOrUpdate(storeMenu);
    }

    @Override
    public List<StoreUserMenuDTO> getUserRoleMenu(String clerkId) {
        return this.baseMapper.getUserRoleMenu(clerkId);
    }

    @Override
    public List<StoreMenu> findByRoleIds(String roleId) {
        QueryWrapper<StoreMenu> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("role_id", roleId);
        return this.list(queryWrapper);
    }

    @Override
    public List<StoreMenu> searchList(MenuSearchParamsDTO menuSearchParams) {
        //title 需要特殊处理
        String title = null;
        if (CharSequenceUtil.isNotEmpty(menuSearchParams.getTitle())) {
            title = menuSearchParams.getTitle();
            menuSearchParams.setTitle(null);
        }
        QueryWrapper<StoreMenu> queryWrapper = PageUtils.initWrapper(menuSearchParams, new SearchVO());
        if (CharSequenceUtil.isNotEmpty(title)) {
            queryWrapper.like("title", title);
        }
        queryWrapper.orderByDesc("sort_order");
        return this.baseMapper.selectList(queryWrapper);
    }


    @Override
    public List<StoreMenuDTO> tree() {
        try {
            List<StoreMenu> menus = this.list();
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
    private List<StoreMenuDTO> tree(List<StoreMenu> menus) {
        List<StoreMenuDTO> tree = new ArrayList<>();
        menus.forEach(item -> {
            if (item.getLevel() == 0) {
                StoreMenuDTO treeItem = new StoreMenuDTO(item);
                initChild(treeItem, menus);
                tree.add(treeItem);
            }
        });
        //对一级菜单排序
        tree.sort(Comparator.comparing(StoreMenu::getSortOrder));
        return tree;
    }

    /**
     * 递归初始化子树
     *
     * @param tree  树结构
     * @param menus 数据库对象集合
     */
    private void initChild(StoreMenuDTO tree, List<StoreMenu> menus) {
        if (menus == null) {
            return;
        }
        menus.stream()
                .filter(item -> (item.getParentId().equals(tree.getId())))
                .forEach(child -> {
                    StoreMenuDTO childTree = new StoreMenuDTO(child);
                    initChild(childTree, menus);
                    tree.getChildren().add(childTree);
                });
    }

}
