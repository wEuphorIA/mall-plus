package com.jzo2o.mall.member.service.impl;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.PageUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jzo2o.common.utils.StringUtils;
import com.jzo2o.common.utils.UserContext;
import com.jzo2o.mall.common.cache.CachePrefix;
import com.jzo2o.mall.common.enums.ResultCode;
import com.jzo2o.mall.common.enums.UserEnums;
import com.jzo2o.mall.common.exception.ServiceException;
import com.jzo2o.mall.common.model.AuthUser;
import com.jzo2o.mall.member.mapper.ClerkMapper;
import com.jzo2o.mall.member.model.domain.Clerk;
import com.jzo2o.mall.member.model.domain.Member;
import com.jzo2o.mall.member.model.domain.StoreClerkRole;
import com.jzo2o.mall.member.model.domain.StoreRole;
import com.jzo2o.mall.member.model.dto.ClerkAddDTO;
import com.jzo2o.mall.member.model.dto.ClerkDTO;
import com.jzo2o.mall.member.model.dto.ClerkEditDTO;
import com.jzo2o.mall.member.model.dto.ClerkQueryDTO;
import com.jzo2o.mall.member.service.*;
import com.jzo2o.mysql.domain.PageVO;
import com.jzo2o.mysql.utils.PageUtils;
import com.jzo2o.redis.helper.Cache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 店员业务实现
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class ClerkServiceImpl extends ServiceImpl<ClerkMapper, Clerk> implements ClerkService {

    @Autowired
    private StoreRoleService storeRoleService;
    @Autowired
    private StoreDepartmentService storeDepartmentService;
    @Autowired
    private MemberService memberService;
    @Autowired
    private StoreClerkRoleService storeClerkRoleService;
    @Autowired
    private Cache cache;

    @Override
    public IPage<ClerkDTO> clerkForPage(PageVO page, ClerkQueryDTO clerkQueryDTO) {
        AuthUser tokenUser =  UserContext.getCurrentUser();
        QueryWrapper<ClerkDTO> clerkVOQueryWrapper = new QueryWrapper<>();
        clerkVOQueryWrapper.eq("ums_clerk.store_id", tokenUser.getStoreId());
        clerkVOQueryWrapper.eq(StringUtils.isNotEmpty(clerkQueryDTO.getDepartmentId()), "ums_clerk.department_id", clerkQueryDTO.getDepartmentId());
        clerkVOQueryWrapper.like(StringUtils.isNotEmpty(clerkQueryDTO.getClerkName()), "ums_clerk.clerk_name", clerkQueryDTO.getClerkName());
        clerkVOQueryWrapper.like(StringUtils.isNotEmpty(clerkQueryDTO.getMobile()), "m.mobile", clerkQueryDTO.getMobile());

        return this.baseMapper.selectClerkPage(PageUtils.initPage(page), clerkVOQueryWrapper);


        /*Page<Clerk> clerkPage = page(initPage, initWrapper);

        if (clerkPage.getRecords().size() > 0) {
            List<StoreRole> roles = storeRoleService.list(new QueryWrapper<StoreRole>()
                    .eq("store_id", UserContext.getCurrentUser().getStoreId()));

            List<StoreDepartment> departments = storeDepartmentService.list(new QueryWrapper<StoreDepartment>()
                    .eq("store_id", UserContext.getCurrentUser().getStoreId()));

            List<String> memberIds = new ArrayList<>();
            clerkPage.getRecords().forEach(clerk -> {
                memberIds.add(clerk.getMemberId());
            });
            List<Member> members = memberService.list(new QueryWrapper<Member>().in("id", memberIds));

            List<ClerkVO> result = new ArrayList<>();

            clerkPage.getRecords().forEach(clerk -> {
                ClerkVO clerkVO = new ClerkVO(clerk);
                if (!CharSequenceUtil.isEmpty(clerk.getDepartmentId())) {
                    try {
                        clerkVO.setDepartmentTitle(
                                departments.stream().filter
                                        (department -> department.getId().equals(clerk.getDepartmentId()))
                                        .collect(Collectors.toList())
                                        .get(0)
                                        .getTitle()
                        );
                    } catch (Exception e) {
                        log.error("填充部门信息异常", e);
                    }
                }
                clerkVO.setMobile(
                        members.stream().filter
                                (member -> member.getId().equals(clerk.getMemberId()))
                                .collect(Collectors.toList())
                                .get(0)
                                .getMobile()
                );
                if (!StringUtils.isEmpty(clerk.getRoleIds())) {
                    try {
                        List<String> memberRoles = Arrays.asList(clerk.getRoleIds().split(","));
                        clerkVO.setRoles(
                                roles.stream().filter
                                        (role -> memberRoles.contains(role.getId()))
                                        .collect(Collectors.toList())
                        );
                    } catch (Exception e) {
                        log.error("填充部门信息异常", e);
                    }
                }
                result.add(clerkVO);
            });
            Page<ClerkVO> pageResult = new Page(clerkPage.getCurrent(), clerkPage.getSize(), clerkPage.getTotal());
            pageResult.setRecords(result);
            return pageResult;
        }
        return new Page<ClerkVO>();*/
    }


    @Override
    public ClerkDTO get(String id) {
        AuthUser tokenUser =  UserContext.getCurrentUser();
        Clerk clerk = this.getById(id);
        ClerkDTO clerkVO = new ClerkDTO(clerk);
        //手机号码
        clerkVO.setMobile(memberService.getById(clerk.getMemberId()).getMobile());
        if (!CharSequenceUtil.isEmpty(clerk.getDepartmentId())) {
            clerkVO.setDepartmentTitle(storeDepartmentService.getById(clerk.getDepartmentId()).getTitle());
        }
        if (!StringUtils.isEmpty(clerk.getRoleIds())) {
            List<String> memberRoles = Arrays.asList(clerk.getRoleIds().split(","));
            List<StoreRole> roles = storeRoleService.list(new QueryWrapper<StoreRole>()
                    .eq("store_id", tokenUser.getStoreId()));
            clerkVO.setRoles(
                    roles.stream().filter
                                    (role -> memberRoles.contains(role.getId()))
                            .collect(Collectors.toList())
            );
        }
        return clerkVO;
    }

    @Override
    public Clerk updateClerk(ClerkEditDTO clerkEditDTO) {
        AuthUser tokenUser =  UserContext.getCurrentUser();
        Clerk clerk = this.getById(clerkEditDTO.getId());
        if (clerk != null) {
            //编辑店主限制
            if (Boolean.TRUE.equals(clerk.getShopkeeper())) {
                throw new ServiceException(ResultCode.CANT_EDIT_CLERK_SHOPKEEPER);
            }

            //校验当前店员是否是当前店铺的
            if (!clerk.getStoreId().equals(tokenUser.getStoreId())) {
                throw new ServiceException(ResultCode.USER_AUTHORITY_ERROR);
            }
            if (Boolean.TRUE.equals(clerkEditDTO.getIsSuper())) {
                clerk.setRoleIds("");
            } else {
                //角色赋值
                if (!clerkEditDTO.getRoles().isEmpty()) {
                    clerk.setRoleIds(CharSequenceUtil.join(",", clerkEditDTO.getRoles()));
                    //添加店员用户角色
                    List<StoreClerkRole> storeClerkRoleList = new ArrayList<>();

                    clerkEditDTO.getRoles().forEach(a -> storeClerkRoleList.add(StoreClerkRole.builder().clerkId(clerk.getId()).roleId(a).build()));

                    storeClerkRoleService.saveBatch(storeClerkRoleList);
                } else {
                    clerk.setRoleIds("");
                    LambdaQueryWrapper<StoreClerkRole> queryWrapper = new LambdaQueryWrapper<>();
                    queryWrapper.eq(StoreClerkRole::getClerkId, clerk.getId());
                    storeClerkRoleService.remove(queryWrapper);
                }
            }

            //部门校验
            if (CharSequenceUtil.isNotEmpty(clerkEditDTO.getDepartmentId())) {
                if (storeDepartmentService.getById(clerkEditDTO.getDepartmentId()) != null) {
                    clerk.setDepartmentId(clerkEditDTO.getDepartmentId());
                } else {
                    throw new ServiceException(ResultCode.PERMISSION_NOT_FOUND_ERROR);
                }
            } else {
                clerk.setDepartmentId("");
            }

            cache.vagueDel(CachePrefix.PERMISSION_LIST.getPrefix(UserEnums.STORE) + clerk.getMemberId());
            cache.vagueDel(CachePrefix.STORE_USER_MENU.getPrefix() + clerk.getMemberId());
            clerk.setIsSuper(clerkEditDTO.getIsSuper());
            this.updateById(clerk);
            return clerk;
        }
        throw new ServiceException(ResultCode.CLERK_NOT_FOUND_ERROR);
    }

    @Override
    public Clerk saveClerk(ClerkAddDTO clerkAddDTO) {
        Clerk clerk = new Clerk(clerkAddDTO);
        clerk.setShopkeeper(clerkAddDTO.getShopkeeper());
        clerk.setIsSuper(clerkAddDTO.getIsSuper());
        //校验此会员是否已经是店员
        Clerk temp = this.getClerkByMemberId(clerkAddDTO.getMemberId());

        //店员信息不为空
        if (temp != null && !temp.getStoreId().equals(clerkAddDTO.getStoreId())) {
            throw new ServiceException(ResultCode.CLERK_USER_ERROR);
        }
        if (temp != null) {
            throw new ServiceException(ResultCode.CLERK_ALREADY_EXIT_ERROR);
        }
        //部门校验
        if (CharSequenceUtil.isNotEmpty(clerkAddDTO.getDepartmentId()) && storeDepartmentService.getById(clerkAddDTO.getDepartmentId()) == null) {
            throw new ServiceException(ResultCode.PERMISSION_NOT_FOUND_ERROR);
        }
        //角色校验
        if (clerkAddDTO.getRoles() != null && !clerkAddDTO.getRoles().isEmpty()) {
            List<StoreRole> storeRoles = storeRoleService.list(clerkAddDTO.getRoles());
            if (storeRoles.size() != clerkAddDTO.getRoles().size()) {
                throw new ServiceException(ResultCode.USER_AUTHORITY_ERROR);
            }
        }

        this.save(clerk);

        //判断用户角色权限不为超级会员且权限路径不为空
        if (Boolean.FALSE.equals(clerkAddDTO.getIsSuper()) && clerkAddDTO.getRoles() != null) {
            //添加店员用户角色
            List<StoreClerkRole> storeClerkRoleList = new ArrayList<>();

            clerkAddDTO.getRoles().forEach(a -> storeClerkRoleList.add(StoreClerkRole.builder().clerkId(clerk.getId()).roleId(a).build()));

            storeClerkRoleService.saveBatch(storeClerkRoleList);
        }

        return clerk;
    }

    @Override
    public Clerk getClerkByMemberId(String memberId) {
        return this.getOne(new QueryWrapper<Clerk>().eq("member_id", memberId));
    }

    @Override
    public void resetPassword(List<String> ids) {
        AuthUser tokenUser =  UserContext.getCurrentUser();
        QueryWrapper<Clerk> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("store_id", tokenUser.getStoreId());
        queryWrapper.in("id", ids);
        List<Clerk> clerks = this.baseMapper.selectList(queryWrapper);
        //校验要重置的店员是否是当前店铺的店员
        if (clerks.size() != ids.size()) {
            throw new ServiceException(ResultCode.USER_AUTHORITY_ERROR);
        }
        //店员密码就是会员密码所以要组织会员修改密码参数信息
        List<String> memberIds = new ArrayList<>();
        clerks.forEach(clerk -> {
            //如果是店主无法重置密码
            if (Boolean.TRUE.equals(clerk.getShopkeeper())) {
                throw new ServiceException(ResultCode.CLERK_SUPPER);
            }
            memberIds.add(clerk.getMemberId());
        });
        memberService.resetPassword(memberIds);
    }


    @Override
    public void deleteClerk(List<String> ids) {
        AuthUser tokenUser =  UserContext.getCurrentUser();
        QueryWrapper<Clerk> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("store_id", tokenUser.getStoreId());
        queryWrapper.in("id", ids);
        List<Clerk> clerks = this.baseMapper.selectList(queryWrapper);
        if (!clerks.isEmpty()) {
            //校验要重置的店员是否是当前店铺的店员
            if (clerks.size() != ids.size()) {
                throw new ServiceException(ResultCode.USER_AUTHORITY_ERROR);
            }
            //删除店员
            this.removeByIds(ids);
            //更改会员为为拥有店铺
            List<String> memberIds = new ArrayList<>();
            clerks.forEach(clerk -> {
                //无法删除当前登录的店员
                if (tokenUser.getClerkId().equals(clerk.getId())) {
                    throw new ServiceException(ResultCode.CLERK_CURRENT_SUPPER);
                }
                //无法删除店主
                if (Boolean.TRUE.equals(clerk.getShopkeeper())) {
                    throw new ServiceException(ResultCode.CLERK_SUPPER);
                }
                memberIds.add(clerk.getMemberId());
            });
            memberService.updateHaveShop(false, null, memberIds);
        }
    }

    @Override
    public Member checkClerk(String mobile) {
        AuthUser tokenUser =  UserContext.getCurrentUser();
        //校验是否已经是会员
        Member member = memberService.findByMobile(mobile);
        if (member != null) {
            //校验要添加的会员是否已经是店主
            if (Boolean.TRUE.equals(member.getHaveStore())) {
                throw new ServiceException(ResultCode.STORE_APPLY_DOUBLE_ERROR);
            }
            //校验会员的有效性
            if (Boolean.FALSE.equals(member.getDisabled())) {
                throw new ServiceException(ResultCode.USER_STATUS_ERROR);
            }
            //校验此会员是否已经是店员
            Clerk clerk = this.getClerkByMemberId(member.getId());
            if (clerk != null && !clerk.getStoreId().equals(tokenUser.getStoreId())) {
                throw new ServiceException(ResultCode.CLERK_USER_ERROR);
            }
            if (clerk != null && clerk.getStoreId().equals(tokenUser.getStoreId())) {
                throw new ServiceException(ResultCode.CLERK_ALREADY_EXIT_ERROR);
            }
            return member;
        }
        return new Member();
    }

    @Override
    public void disable(String id, Boolean status) {
        Clerk clerk = this.getById(id);
        if (clerk == null) {
            throw new ServiceException(ResultCode.USER_NOT_EXIST);
        }
        //店主无法禁用
        if (clerk.getShopkeeper() && clerk.getStatus()) {
            throw new ServiceException(ResultCode.CLERK_SUPPER);
        }
        clerk.setStatus(status);
        this.updateById(clerk);
    }
}
