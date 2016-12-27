package com.chanapp.chanjet.web.aop;

/**
 * 拦截器before/after/exception返回值
 * 
 * <pre>
 * before()：
 *      返回CONTINUE：继续执行下一个拦截器
 *      返回BREAK：抛出业务异常时使用；不再执行被拦截的方法；不再执行之后的拦截器；不再执行本拦截器的after/exception；
 *      返回RETURN：替换被拦截的方法的返回值；不再执行被拦截的方法；不再执行之后的拦截器；不再执行本拦截器的after/exception；
 * 
 * after()：
 *      返回CONTINUE：继续执行下一个拦截器
 *      返回BREAK：抛出业务异常时使用；不再执行之后的拦截器；
 *      返回RETURN：替换被拦截的方法的返回值；不再执行之后的拦截器；
 *      
 * exception()：
 *      返回CONTINUE：继续执行下一个拦截器
 *      返回BREAK：替换被拦截的方法抛出的异常；不再执行之后的拦截器；
 *      返回RETURN：吃掉被拦截的方法的异常，并返回一个正常值；不再执行之后的拦截器；不再执行本拦截器的after；
 * </pre>
 * 
 * @author tds
 *
 */
public class ServiceInterceptorReturn {

    private static final int CONTINUE = 1;
    private static final int BREAK = 2;
    private static final int RETURN = 4;

    private int value;
    private Object result;
    private ServiceInterceptorException exception;

    private ServiceInterceptorReturn(int value, Object result, ServiceInterceptorException exception) {
        this.value = value;
        this.result = result;
        this.exception = exception;
    }

    public Object getResult() {
        return result;
    }

    public ServiceInterceptorException getException() {
        return exception;
    }

    public static ServiceInterceptorReturn CONTINUE() {
        return new ServiceInterceptorReturn(CONTINUE, null, null);
    }

    public static ServiceInterceptorReturn BREAK(ServiceInterceptorException e) {
        return new ServiceInterceptorReturn(BREAK, null, e);
    }

    public static ServiceInterceptorReturn RETURN(Object result) {
        return new ServiceInterceptorReturn(RETURN, result, null);
    }

    public boolean IS_CONTINUE() {
        return value == CONTINUE;
    }

    public boolean IS_BREAK() {
        return value == BREAK;
    }

    public boolean IS_RETURN() {
        return value == RETURN;
    }
}
