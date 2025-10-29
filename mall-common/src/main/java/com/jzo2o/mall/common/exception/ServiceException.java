package com.jzo2o.mall.common.exception;

import com.jzo2o.common.expcetions.AbstractException;
import com.jzo2o.mall.common.enums.ResultCode;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 全局业务异常类
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ServiceException extends AbstractException {

    private static final long serialVersionUID = 3447728300174142127L;

    public static final String DEFAULT_MESSAGE = "网络错误，请稍后重试！";

    /**
     * 异常消息
     */
    private String msg = DEFAULT_MESSAGE;

    /**
     * 错误码
     */
    private ResultCode resultCode;

    public ServiceException(String msg) {
        this.resultCode = ResultCode.ERROR;
        this.msg = msg;
    }

    public ServiceException() {
        super();
    }

    @Override
    public int getCode() {
        return resultCode.code();
    }

    @Override
    public String getMessage() {
        return this.msg;
    }

    public ServiceException(ResultCode resultCode) {
        this.resultCode = resultCode;
        this.msg= resultCode.message();
    }

    public ServiceException(ResultCode resultCode, String message) {
        this.resultCode = resultCode;
        this.msg = resultCode.message()+" "+message;
    }

}
