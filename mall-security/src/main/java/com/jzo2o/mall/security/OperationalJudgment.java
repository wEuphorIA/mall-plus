//package com.jzo2o.mall.security;
//
//
//import com.jzo2o.common.utils.BeanUtils;
//import com.jzo2o.mall.common.enums.ResultCode;
//import com.jzo2o.mall.common.exception.ServiceException;
//import com.jzo2o.mall.common.model.AuthUser;
//import com.jzo2o.mall.security.context.UserContext;
//
//import java.util.Objects;
//
//
///**
// * 全局统一判定是否可操作某属性
// *
// * @author Chopper
// * @version v1.0
// * 2020-08-20 18:07
// */
//public class OperationalJudgment {
//
//    /**
//     * 需要判定的对象必须包含属性 memberId，storeId 代表判定的角色
//     *
//     * @param object 判定的对象
//     * @param <T> 判定处理对象
//     * @return 处理结果
//     */
//    public static <T> T judgment(T object) {
//        return judgment(object, "memberId", "storeId");
//    }
//
//    /**
//     * 需要判定的对象必须包含属性 memberId，storeId 代表判定的角色
//     *
//     * @param object 判定对象
//     * @param buyerIdField 买家id
//     * @param storeIdField 店铺id
//     * @param <T> 范型
//     * @return 返回判定本身，防止多次查询对象
//     */
//    public static <T> T judgment(T object, String buyerIdField, String storeIdField) {
//        AuthUser tokenUser = Objects.requireNonNull(UserContext.getCurrentUser());
//        switch (tokenUser.getRole()) {
//            case MANAGER:
//                return object;
//            case MEMBER:
//                if (tokenUser.getId().equals(BeanUtils.getFieldValueByName(buyerIdField, object))) {
//                    return object;
//                } else {
//                    throw new ServiceException(ResultCode.USER_AUTHORITY_ERROR);
//                }
//            case STORE:
//                if (tokenUser.getStoreId().equals(BeanUtils.getFieldValueByName(storeIdField, object))) {
//                    return object;
//                } else {
//                    throw new ServiceException(ResultCode.USER_AUTHORITY_ERROR);
//                }
//            default:
//                throw new ServiceException(ResultCode.USER_AUTHORITY_ERROR);
//        }
//    }
//}
