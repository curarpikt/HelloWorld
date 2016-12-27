package com.chanapp.chanjet.web.aop;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chanapp.chanjet.web.service.BaseServiceItf;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.NamingStrategy;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatchers;

/**
 * Service拦截器管理类，提供注册，获取，保存系统中注册的所有拦截器等等功能
 * 
 * <pre>
 * 拦截器分为两种：
 * 1. 全局级别的，所有Service都会触发；
 * 2. Service级别的，只会在指定的Service上触发；
 * 3. 拦截器可以注册多个，触发顺序：全局（按先后顺序） -》Service自己的（按先后顺序）;
 * 4. 没有处理重复注册的问题，如果一个触发器注册多次，会重复触发
 * </pre>
 * 
 * @author tds
 *
 */
public class ServiceInterceptorManager {
    private static Logger logger = LoggerFactory.getLogger(ServiceInterceptorManager.class);

    private final List<BaseServiceInterceptorItf> _global_interceptors = Collections
            .synchronizedList(new ArrayList<BaseServiceInterceptorItf>());
    private final ConcurrentHashMap<String, List<BaseServiceInterceptorItf>> _interceptors = new ConcurrentHashMap<String, List<BaseServiceInterceptorItf>>();

    private final ConcurrentHashMap<String, BaseServiceInterceptorItf> _single_interceptors = new ConcurrentHashMap<String, BaseServiceInterceptorItf>();

    public static final String SUFFIX = "CUSTOMER";

    private boolean isInited = false;

    private static class ServiceInterceptorManagerHolder {
        private static final ServiceInterceptorManager INSTANCE = new ServiceInterceptorManager();
    }

    private ServiceInterceptorManager() {

    }

    public static ServiceInterceptorManager getInstance() {
        return ServiceInterceptorManagerHolder.INSTANCE;
    }

    public <T extends BaseServiceItf> Class<? extends T> intercept(Class<T> serviceImpl) {
        try {
            return new ByteBuddy().with(new NamingStrategy.SuffixingRandom(SUFFIX)).subclass(serviceImpl)
                    .method(ElementMatchers.not(ElementMatchers.isDeclaredBy(Object.class)))
                    .intercept(MethodDelegation.to(ServiceInterceptorExecutor.class)).make()
                    .load(this.getClass().getClassLoader(), ClassLoadingStrategy.Default.WRAPPER).getLoaded();
        } catch (Exception e) {
            logger.warn("intercept service:{} error:{}, return service self", serviceImpl.getName(), e.getMessage());
            return serviceImpl;
        }
    }

    private BaseServiceInterceptorItf getInterceptorInstance(String interceptorName) {
        try {
            BaseServiceInterceptorItf interceptorInstance = _single_interceptors.get(interceptorName);
            if (interceptorInstance == null) {
                Class<?> interceptor = Class.forName(interceptorName);
                if (!BaseServiceInterceptorItf.class.isAssignableFrom(interceptor)) {
                    throw new IllegalArgumentException(
                            "interceptor[" + interceptorName + "]非法， 必须继承自BaseServiceInterceptorItf");
                }
                interceptorInstance = (BaseServiceInterceptorItf) interceptor.newInstance();
                BaseServiceInterceptorItf _interceptorInstance = _single_interceptors.putIfAbsent(interceptorName,
                        interceptorInstance);
                if (_interceptorInstance != null) {
                    interceptorInstance = _interceptorInstance;
                }
            }
            return interceptorInstance;
        } catch (Exception e) {
            logger.error("get interceptor[{}] instance error: {}", interceptorName, e.getMessage());
        }
        logger.warn("get interceptor[{}] instance failed", interceptorName);
        return null;
    }

    public <I extends BaseServiceInterceptorItf> void register(String interceptorName) {
        if (interceptorName == null) {
            return;
        }

        BaseServiceInterceptorItf interceptorInstance = getInterceptorInstance(interceptorName);
        if (interceptorInstance != null) {
            _global_interceptors.add(interceptorInstance);
        }

    }

    public <I extends BaseServiceInterceptorItf> void register(Class<I> interceptor) {
        register(interceptor.getName());
    }

    public void register(String serviceName, String interceptorName) {
        if (serviceName == null || interceptorName == null) {
            return;
        }

        try {
            Class<?> serviceItf = Class.forName(serviceName);
            if (!BaseServiceItf.class.isAssignableFrom(serviceItf)) {
                logger.error("service[" + serviceName + "]非法， 必须继承自BaseServiceItf");
                return;
            }
        } catch (ClassNotFoundException e) {
            logger.error("service[" + serviceName + "]非法， 必须继承自BaseServiceItf，{}", e.getMessage());
        }

        List<BaseServiceInterceptorItf> serivce_interceptors = _interceptors.get(serviceName);
        if (serivce_interceptors == null) {
            serivce_interceptors = new ArrayList<BaseServiceInterceptorItf>();
            List<BaseServiceInterceptorItf> _serivce_interceptors = _interceptors.putIfAbsent(serviceName,
                    serivce_interceptors);
            if (_serivce_interceptors != null) {
                serivce_interceptors = _serivce_interceptors;
            }
        }

        BaseServiceInterceptorItf interceptorInstance = getInterceptorInstance(interceptorName);
        if (interceptorInstance != null) {
            serivce_interceptors.add(interceptorInstance);
        }
    }

    public <T extends BaseServiceItf, I extends BaseServiceInterceptorItf> void register(Class<T> serviceItf,
            Class<I> interceptor) {
        register(serviceItf.getName(), interceptor.getName());
    }

    public <T extends BaseServiceItf> void clear(Class<T> serviceItf) {
        String serviceName = serviceItf.getName();
        clear(serviceName);
    }

    public void clear(String serviceName) {
        if (_interceptors.containsKey(serviceName)) {
            for (@SuppressWarnings("unused")
            BaseServiceInterceptorItf _interceptor : _interceptors.get(serviceName)) {
                _interceptor = null;
            }
        }
        _interceptors.get(serviceName).clear();
    }

    public void clearAll() {
        for (@SuppressWarnings("unused")
        BaseServiceInterceptorItf _interceptor : _global_interceptors) {
            _interceptor = null;
        }
        _global_interceptors.clear();

        for (String serviceName : _interceptors.keySet()) {
            clear(serviceName);
        }
        _interceptors.clear();

        setInited(false);
    }

    public List<BaseServiceInterceptorItf> interceptors(String serviceName) {
        List<BaseServiceInterceptorItf> interceptors = new ArrayList<BaseServiceInterceptorItf>();

        if (_global_interceptors != null) {
            interceptors.addAll(_global_interceptors);
        }

        if (_interceptors.containsKey(serviceName) && !_interceptors.get(serviceName).isEmpty()) {
            interceptors.addAll(_interceptors.get(serviceName));
        }

        return Collections.unmodifiableList(interceptors);
    }

    public boolean isInited() {
        return isInited;
    }

    public void setInited(boolean isInited) {
        this.isInited = isInited;
    }
}
