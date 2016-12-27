package com.chanapp.chanjet.web.aop;

/**
 * Service拦截器
 * 
 * <pre>
 * 支持拦截serviceItf中的方法的执行前、执行后、抛出异常三种场景。
 * 对每个拦截器实现类，在内存中会生成唯一实例，所有线程共享；
 * 实现例子请参考 {@link com.chanapp.chanjet.web.aop.ServiceLogInterceptorImpl}
 * </pre>
 * 
 * @author tds
 *
 */
public interface BaseServiceInterceptorItf {

    final int STACK_TRACE_LEVEL = 12; // 提供12级调用堆栈信息

    /**
     * service method 执行前
     */
    ServiceInterceptorReturn before(ServiceInterceptorInfo info);

    /**
     * service method 执行后
     */
    ServiceInterceptorReturn after(ServiceInterceptorInfo info);

    /**
     * service method 抛出异常
     */
    ServiceInterceptorReturn exception(ServiceInterceptorInfo info);
}
