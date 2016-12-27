package com.chanapp.chanjet.web.aop;

/**
 * 执行拦截器after/before/exception时，记录标记
 * 
 * @author tds
 *
 */
class ServiceInterceptorThreadFlag {
    private static ThreadLocal<Boolean> local = new ThreadLocal<Boolean>() {
        @Override
        protected Boolean initialValue() {
            return Boolean.FALSE;
        }
    };

    public static Boolean isIntercepting() {
        return local.get();
    }

    public static void setIntercepting() {
        local.set(Boolean.TRUE);
    }

    public static void clear() {
        local.set(Boolean.FALSE);
    }

}
