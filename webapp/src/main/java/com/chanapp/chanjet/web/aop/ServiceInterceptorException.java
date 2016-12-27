package com.chanapp.chanjet.web.aop;

import com.chanjet.csp.common.base.exception.AppException;
import com.chanjet.csp.common.base.exception.RestMessageIdEnum;
import com.chanjet.csp.common.base.exception.intf.ErrorCoded;

/**
 * 拦截器异常：拦截器实现类里抛出这种异常时，会中断主业务逻辑，并把异常消息抛到外层
 * 
 * @author tds
 *
 */
public class ServiceInterceptorException extends AppException implements ErrorCoded {
    private static final long serialVersionUID = 2506702292779181358L;

    private Integer errorCode;

    public ServiceInterceptorException(RestMessageIdEnum messageId) {
        errorCode = messageId.getMessageNumber();

        Object[] arguments = new Object[1];
        arguments[0] = errorCode;
        init(this, messageId.getMessageId(), arguments);
    }

    public ServiceInterceptorException(RestMessageIdEnum messageId, Object[] params) {
        errorCode = messageId.getMessageNumber();

        Object[] arguments = new Object[params.length + 1];
        arguments[0] = errorCode;
        for (int i = 0; i < params.length; i++) {
            arguments[i + 1] = params[i];
        }

        init(this, messageId.getMessageId(), arguments);
    }

    public ServiceInterceptorException(String messageId) {
        super(messageId);
    }

    public ServiceInterceptorException(Exception e, String messageId, Object[] params) {
        super(e, messageId, params);
    }

    public ServiceInterceptorException(Exception e, String messageId) {
        super(e, messageId);
    }

    @Override
    public Integer getErrorCode() {
        return errorCode;
    }

}
