package com.jzo2o.mall.member;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jzo2o.common.utils.UserContext;
import com.jzo2o.mall.common.enums.ResultUtil;
import com.jzo2o.mall.common.model.AuthUser;
import com.jzo2o.mall.common.model.ResultMessage;
import com.jzo2o.mall.member.model.domain.MemberAddress;
import com.jzo2o.mall.member.service.MemberAddressService;
import com.jzo2o.mysql.domain.PageVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Objects;


/**
 * 买家端,会员地址接口
 */
@RestController
@Api(tags = "买家端,会员地址接口")
@RequestMapping("/member/address")
public class MemberAddressBuyerController {

    /**
     * 会员收件地址
     */
    @Autowired
    private MemberAddressService memberAddressService;

    @ApiOperation(value = "获取会员收件地址分页列表")
    @GetMapping
    public IPage<MemberAddress> page(PageVO page) {
        IPage<MemberAddress> addressByMember = memberAddressService.getAddressByMember(page, UserContext.getCurrentUser().getIdString());
        return addressByMember;
    }

    @ApiOperation(value = "根据ID获取会员收件地址")
    @ApiImplicitParam(name = "id", value = "会员地址ID", dataType = "String", paramType = "path")
    @GetMapping(value = "/get/{id}")
    public MemberAddress getShippingAddress(@PathVariable String id) {
        MemberAddress memberAddress = memberAddressService.getMemberAddress(id);
        return memberAddress;
    }

    @ApiOperation(value = "获取当前会员默认收件地址")
    @GetMapping(value = "/get/default")
    public MemberAddress getDefaultShippingAddress() {
        MemberAddress defaultMemberAddress = memberAddressService.getDefaultMemberAddress();
        return defaultMemberAddress;
    }

    @ApiOperation(value = "新增会员收件地址")
    @PostMapping
    public MemberAddress addShippingAddress(@Valid MemberAddress shippingAddress) {
        AuthUser authUser = UserContext.getCurrentUser();
        //添加会员地址
        shippingAddress.setMemberId(authUser.getIdString());
        if(shippingAddress.getIsDefault()==null){
            shippingAddress.setIsDefault(false);
        }
        MemberAddress memberAddress = memberAddressService.saveMemberAddress(shippingAddress);
        return memberAddress;
    }

    @ApiOperation(value = "修改会员收件地址")
    @PutMapping
    public MemberAddress editShippingAddress(@Valid MemberAddress shippingAddress) {
        AuthUser authUser = UserContext.getCurrentUser();
        shippingAddress.setMemberId(authUser.getIdString());
        MemberAddress memberAddress = memberAddressService.updateMemberAddress(shippingAddress);
        return memberAddress;
    }

    @ApiOperation(value = "删除会员收件地址")
    @ApiImplicitParam(name = "id", value = "会员地址ID", dataType = "String", paramType = "path")
    @DeleteMapping(value = "/delById/{id}")
    public void delShippingAddressById(@PathVariable String id) {
        memberAddressService.removeMemberAddress(id);
    }

}
