package com.chanapp.chanjet.web.aop;

/**
 * 拦截器的基础实现类
 * 
 * @author tds
 *
 */
public abstract class BaseServiceInterceptorImpl implements BaseServiceInterceptorItf {

    @Override
    public ServiceInterceptorReturn before(ServiceInterceptorInfo info) {
        return ServiceInterceptorReturn.CONTINUE();
    }

    @Override
    public ServiceInterceptorReturn after(ServiceInterceptorInfo info) {
        return ServiceInterceptorReturn.CONTINUE();
    }

    @Override
    public ServiceInterceptorReturn exception(ServiceInterceptorInfo info) {
        return ServiceInterceptorReturn.CONTINUE();
    }

}
