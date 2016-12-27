package com.chanapp.chanjet.web.aop;

import java.lang.reflect.Method;

/**
 * 拦截到的信息：提供了获取当前service/method/params/等等方法
 * 
 * @author tds
 *
 */
public class ServiceInterceptorInfo {
    private Class<?> clazz;
    private Object[] params;
    private Method method;
    private Object result;
    private Exception exception;
    private StackTraceElement[] stackTraces;

    public String getServiceName() {
        String serviceName = this.getClazz().getName();
        try {
            serviceName = serviceName.substring(0, serviceName.indexOf("$"));
        } catch (Exception e) {
            // ignore
        }
        return serviceName;
    }

    /**
     * @return the clazz
     */
    public Class<?> getClazz() {
        return clazz;
    }

    /**
     * @param clazz the clazz to set
     */
    public void setClazz(Class<?> clazz) {
        this.clazz = clazz;
    }

    /**
     * @return the params
     */
    public Object[] getParams() {
        return params;
    }

    /**
     * @param params the params to set
     */
    public void setParams(Object[] params) {
        this.params = params;
    }

    /**
     * @return the method
     */
    public Method getMethod() {
        return method;
    }

    /**
     * @param method the method to set
     */
    public void setMethod(Method method) {
        this.method = method;
    }

    /**
     * @return the result
     */
    public Object getResult() {
        return result;
    }

    /**
     * @param result the result to set
     */
    public void setResult(Object result) {
        this.result = result;
    }

    /**
     * @return the exception
     */
    public Exception getException() {
        return exception;
    }

    /**
     * @param exception the exception to set
     */
    public void setException(Exception exception) {
        this.exception = exception;
    }

    /**
     * @return the stackTraces
     */
    public StackTraceElement[] getStackTraces() {
        return stackTraces;
    }

    /**
     * @param stackTraces the stackTraces to set
     */
    public void setStackTraces(StackTraceElement[] stackTraces) {
        this.stackTraces = stackTraces;
    }
}
