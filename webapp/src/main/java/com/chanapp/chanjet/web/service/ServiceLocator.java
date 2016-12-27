package com.chanapp.chanjet.web.service;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chanapp.chanjet.web.annotation.NotSingleton;
import com.chanapp.chanjet.web.aop.BaseServiceInterceptorItf;
import com.chanapp.chanjet.web.aop.ServiceInterceptorManager;
import com.chanapp.chanjet.web.aop.ServiceInterceptorRegister;
import com.chanjet.csp.appmanager.AppWorkManager;
import com.chanjet.csp.bo.api.IBusinessObjectHome;
import com.chanjet.csp.bo.api.IBusinessObjectRow;
import com.chanjet.csp.bo.api.IBusinessObjectRowSet;
import com.chanjet.csp.common.base.exception.AppException;

/**
 * Service定位
 * 
 * @author tds
 *
 */
public final class ServiceLocator {
    private static Logger logger = LoggerFactory.getLogger(ServiceLocator.class);
    private final ConcurrentHashMap<String, BaseServiceItf> _services = new ConcurrentHashMap<String, BaseServiceItf>();

    private static class ServiceLocatorHolder {
        private static final ServiceLocator INSTANCE = new ServiceLocator();
    }

    private ServiceLocator() {
        ServiceInterceptorRegister.init();// 初始化拦截器
    }

    public static ServiceLocator getInstance() {
        return ServiceLocatorHolder.INSTANCE;
    }

    /**
     * 根据接口名称获取Service实现类
     * 
     * @param serviceItf service接口类，必须继承BaseServiceItf或BoBaseServiceItf
     * @param params Service自己的构造函数参数
     * @return BaseServiceItf
     * @throws 获取失败时抛出400异常
     */
    @SuppressWarnings("unchecked")
    public <T extends BaseServiceItf> T lookup(Class<T> serviceItf, Object... params) {
        synchronized (serviceItf) {
            String serviceName = serviceItf.getName();

            if (_services.containsKey(serviceName)) {
                return (T) _services.get(serviceName);
            }

            try {
                // 实现类：XXServiceItf => XXServiceImpl
                Class<BaseServiceItf> _serviceImpl = (Class<BaseServiceItf>) Class
                        .forName(serviceName.substring(0, serviceName.length() - 3) + "Impl");

                // 单例标记
                boolean singleton = serviceItf.getAnnotation(NotSingleton.class) == null
                        && _serviceImpl.getAnnotation(NotSingleton.class) == null;

                Class<? extends BaseServiceItf> serviceImpl = null;
                if (Modifier.isFinal(_serviceImpl.getModifiers())) { // 不拦截final的ServiceImpl
                    serviceImpl = _serviceImpl;
                } else {
                    List<BaseServiceInterceptorItf> interceptors = ServiceInterceptorManager.getInstance()
                            .interceptors(serviceName);
                    if (interceptors != null && interceptors.size() > 0) { // 拦截
                        serviceImpl = ServiceInterceptorManager.getInstance().intercept(_serviceImpl);
                    } else {
                        serviceImpl = _serviceImpl;
                    }
                }

                T service = null;
                if (params == null || params.length == 0) {
                    service = (T) serviceImpl.newInstance();
                } else {
                    // 支持service自己的带参数构造函数
                    Class<?>[] paramClasses = new Class<?>[params.length - 1];
                    for (int i = 0, len = params.length; i < len; i++) {
                        paramClasses[i++] = params[i].getClass();
                    }
                    Constructor<BaseServiceItf> serviceConstructor = (Constructor<BaseServiceItf>) serviceImpl
                            .getConstructor(paramClasses);
                    serviceConstructor.setAccessible(true);
                    service = (T) serviceConstructor.newInstance(params);
                }

                // NotSingleton的service，每次都new
                if (singleton) {
                    _services.put(serviceName, service);
                }

                return service;
            } catch (ClassNotFoundException | SecurityException | InstantiationException | IllegalAccessException
                    | IllegalArgumentException | NoSuchMethodException | InvocationTargetException e) {
                logger.error("get service [{}] error: {}", serviceName, e.getMessage());
                throw new AppException(e, "get service [" + serviceName + "] error");
            }
        }
    }

    public <T extends BaseServiceItf> T lookup(Class<T> serviceItf) {
        return lookup(serviceItf, new Object[] {});
    }

    /**
     * 根据Bo名称获取Bo的Service实现类，不存在时，返回默认实现类：BoBaseServiceImpl
     * 
     * @param boName BO名称
     * @param params BoService自己的构造函数参数
     * @return BoBaseServiceItf
     */
    @SuppressWarnings("unchecked")
    public BoBaseServiceItf<IBusinessObjectHome, IBusinessObjectRow, IBusinessObjectRowSet> lookup(String boName,
            Object... params) {
        synchronized (boName) {
            String serviceName = AppWorkManager.getCurrentAppId() + ".service." + boName.toLowerCase() + "." + boName
                    + "ServiceItf";
            if (_services.containsKey(serviceName)) {
                return (BoBaseServiceItf<IBusinessObjectHome, IBusinessObjectRow, IBusinessObjectRowSet>) _services
                        .get(serviceName);
            }
            try {
                Class<BaseServiceItf> serviceItf = (Class<BaseServiceItf>) Class.forName(serviceName);
                if (!BoBaseServiceItf.class.isAssignableFrom(serviceItf)) {
                    throw new IllegalArgumentException(
                            "bo[" + boName + "]的service[" + serviceName + "] 必须继承自BoBaseServiceItf");
                }
                return (BoBaseServiceItf<IBusinessObjectHome, IBusinessObjectRow, IBusinessObjectRowSet>) lookup(
                        serviceItf, params);
            } catch (ClassNotFoundException e) {
                logger.warn("bo service [{}] not found: {}, use bobaseservice", boName, e.getMessage());

                BoBaseServiceItf<IBusinessObjectHome, IBusinessObjectRow, IBusinessObjectRowSet> service = null;
                try {
                    List<BaseServiceInterceptorItf> interceptors = ServiceInterceptorManager.getInstance()
                            .interceptors(serviceName);
                    if (interceptors != null && interceptors.size() > 0) { // 拦截
                        Class<? extends BaseServiceItf> serviceImpl = ServiceInterceptorManager.getInstance()
                                .intercept(BoBaseServiceImpl.class);
                        Constructor<BaseServiceItf> serviceConstructor = (Constructor<BaseServiceItf>) serviceImpl
                                .getConstructor(String.class);
                        serviceConstructor.setAccessible(true);
                        service = (BoBaseServiceItf<IBusinessObjectHome, IBusinessObjectRow, IBusinessObjectRowSet>) serviceConstructor
                                .newInstance(boName);
                    } else {
                        service = new BoBaseServiceImpl<IBusinessObjectHome, IBusinessObjectRow, IBusinessObjectRowSet>(
                                boName);
                    }
                } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException
                        | IllegalArgumentException | InvocationTargetException exp) {
                    logger.error("bo service [{}]  register interceptor error: {}", boName, exp.getMessage());
                }

                if (service == null) {
                    logger.error("get bo service [{}] error", serviceName);
                    throw new AppException(e, "get bo service [" + serviceName + "] error");
                }

                _services.put(serviceName, service);
                return service;
            }
        }
    }

    public BoBaseServiceItf<IBusinessObjectHome, IBusinessObjectRow, IBusinessObjectRowSet> lookup(String boName) {
        return lookup(boName, new Object[] {});
    }
}
