package com.chanapp.chanjet.web.aop;

import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.SuperCall;
import net.bytebuddy.implementation.bind.annotation.This;

/**
 * 拦截器的实际执行类
 * 
 * @author tds
 *
 */
public class ServiceInterceptorExecutor {
    private static Logger logger = LoggerFactory.getLogger(ServiceInterceptorExecutor.class);

    /**
     * 拦截 service method
     */
    @RuntimeType
    public static Object intercept(@SuperCall Callable<Object> zuper, @AllArguments Object[] params,
            @This Object service, @Origin Method method) throws Exception {
        // 拦截器内部调用Service时，不触发拦截
        if (ServiceInterceptorThreadFlag.isIntercepting()) {
            return zuper.call();
        }

        String serviceName = method.getDeclaringClass().getName();
        // 实现类：XXServiceImpl => XXServiceItf
        if (serviceName.endsWith("Impl")) {
            serviceName = serviceName.substring(0, serviceName.length() - 4) + "Itf";
        }
        List<BaseServiceInterceptorItf> interceptors = null;
        try {
            interceptors = ServiceInterceptorManager.getInstance().interceptors(serviceName);
        } catch (Exception e) {
            logger.error("get interceptors error:service={}, error={}", serviceName, e.getMessage());
        }

        // 拦截到的信息
        ServiceInterceptorInfo info = new ServiceInterceptorInfo();

        if (interceptors != null) {
            info.setClazz(service.getClass());
            info.setParams(params);
            info.setMethod(method);

            StackTraceElement[] stackTraces = null;
            // 获取调用堆栈信息
            StackTraceElement[] fullStackTraces = Thread.currentThread().getStackTrace();
            if (fullStackTraces.length < 3 + BaseServiceInterceptorItf.STACK_TRACE_LEVEL) {
                stackTraces = new StackTraceElement[fullStackTraces.length];
                System.arraycopy(fullStackTraces, 0, stackTraces, 0, fullStackTraces.length);
            } else {
                stackTraces = new StackTraceElement[BaseServiceInterceptorItf.STACK_TRACE_LEVEL];
                System.arraycopy(fullStackTraces, 3, stackTraces, 0, BaseServiceInterceptorItf.STACK_TRACE_LEVEL); // 第3级开始是有意义的信息
            }
            info.setStackTraces(stackTraces);
        }

        ServiceInterceptorReturn beforeReturn = ServiceInterceptorReturn.CONTINUE();
        ServiceInterceptorReturn exceptionReturn = ServiceInterceptorReturn.CONTINUE();
        ServiceInterceptorReturn afterReturn = ServiceInterceptorReturn.CONTINUE();
        try {
            if (interceptors != null) {
                for (BaseServiceInterceptorItf interceptor : interceptors) {
                    try {
                        ServiceInterceptorThreadFlag.setIntercepting();

                        beforeReturn = interceptor.before(info);
                        if (!beforeReturn.IS_CONTINUE()) {
                            break;
                        }
                    } catch (Exception beforeException) {
                        logger.error("interceptor before() error:service={}, interceptor={}, error={}", serviceName,
                                interceptor.getClass().getName(), beforeException.getMessage());
                    } finally {
                        ServiceInterceptorThreadFlag.clear();
                    }
                }
            }

            if (beforeReturn.IS_BREAK()) {
                throw beforeReturn.getException();
            }

            if (beforeReturn.IS_RETURN()) {
                return beforeReturn.getResult();
            }

            Object result = zuper.call();

            if (!method.getReturnType().getName().equals("void")) {
                if (interceptors != null) {
                    info.setResult(result);
                }
            }

            return result;
        } catch (Exception e) {
            if (beforeReturn.IS_BREAK()) {
                throw beforeReturn.getException();
            }

            if (interceptors != null) {
                for (BaseServiceInterceptorItf interceptor : interceptors) {
                    info.setException(e);
                    try {
                        ServiceInterceptorThreadFlag.setIntercepting();

                        exceptionReturn = interceptor.exception(info);
                        if (!exceptionReturn.IS_CONTINUE()) {
                            break;
                        }
                    } catch (Exception exceptionException) {
                        logger.error("interceptor exception() error:service={}, interceptor={}, error={}", serviceName,
                                interceptor.getClass().getName(), exceptionException.getMessage());
                    } finally {
                        ServiceInterceptorThreadFlag.clear();
                    }
                }
            }

            if (exceptionReturn.IS_BREAK()) {
                throw exceptionReturn.getException();
            }

            if (exceptionReturn.IS_RETURN()) {
                return exceptionReturn.getResult();
            }

            if (afterReturn.IS_BREAK()) {
                throw afterReturn.getException();
            }

            if (afterReturn.IS_RETURN()) {
                return afterReturn.getResult();
            }

            throw e;// 原样抛出
        } finally {
            if (interceptors != null && beforeReturn.IS_CONTINUE() && exceptionReturn.IS_CONTINUE()) {
                for (BaseServiceInterceptorItf interceptor : interceptors) {
                    try {
                        ServiceInterceptorThreadFlag.setIntercepting();

                        afterReturn = interceptor.after(info);
                        if (!afterReturn.IS_CONTINUE()) {
                            break;
                        }
                    } catch (Exception afterException) {
                        logger.error("interceptor after() error:service={}, interceptor={}, error={}", serviceName,
                                interceptor.getClass().getName(), afterException.getMessage());
                    } finally {
                        ServiceInterceptorThreadFlag.clear();
                    }
                }
            }
        }
    }

}
