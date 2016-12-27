package com.chanapp.chanjet.web.aop;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.chanapp.chanjet.web.reader.XmlReader;

/**
 * 初始化系统需要的全部拦截器。
 * 
 * <pre>
 * 不需要主动调用这个类，只需要在resources/aop.xml中按需补充就行。
 * 
 * 没有处理重复注册的问题，如果一个触发器注册多次，会重复触发
 * </pre>
 * 
 * @author tds
 *
 */
public class ServiceInterceptorRegister {

    private static Logger logger = LoggerFactory.getLogger(ServiceInterceptorRegister.class);

    private final static XmlReader reader = XmlReader.getInstance("aop.xml");

    /**
     * 只在每个线程中第一次调用的时候初始化
     */
    public static void init() {
        try {
            if (ServiceInterceptorManager.getInstance().isInited()) {
                return;
            }
            logger.info("------BEGIN register interceptor:{}", Thread.currentThread().getName());

            ServiceInterceptorManager.getInstance().clearAll();

            registerGlobalInterceptors();
            registerServiceInterceptors();

            // 设置初始化完成标识
            ServiceInterceptorManager.getInstance().setInited(true);

            logger.info("------END register interceptor:{}", Thread.currentThread().getName());
        } catch (Exception e) {
            logger.error("------ register interceptor error:", e);
        }
    }

    /**
     * 所有Service都会触发的拦截器
     */
    private static void registerGlobalInterceptors() {
        NodeList nodeList = reader.getNodeList("aop:globalInterceptor");
        if (nodeList != null) {
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node dog = nodeList.item(i);
                Element elem = (Element) dog;
                String interceptorName = elem.getAttribute("interceptor");
                ServiceInterceptorManager.getInstance().register(interceptorName);
                logger.info("###### register global interceptor:{}, {}", interceptorName, Thread.currentThread().getName());
            }
        }
    }

    /**
     * 指定Service自己的拦截器
     */
    private static void registerServiceInterceptors() {
        NodeList nodeList = reader.getNodeList("aop:serviceInterceptor");
        if (nodeList != null) {
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node dog = nodeList.item(i);
                Element elem = (Element) dog;
                String serviceName = elem.getAttribute("service");
                String interceptorName = elem.getAttribute("interceptor");
                ServiceInterceptorManager.getInstance().register(serviceName, interceptorName);
                logger.info("###### register service interceptor:{}, {}, {}", serviceName, interceptorName, Thread.currentThread().getName());
            }
        }
    }
}
