package com.chanapp.chanjet.web.service;

/**
 * 所有Service的基础接口
 * 
 * <ul>
 * <li>{@link com.chanapp.chanjet.web.annotation.NotSingleton} 注解标识通过
 * {@link ServiceLocator.lookup} 获取Service时每次都会只初始化一个新实例，默认Service只会在内存中创建一个实例
 * </li>
 * </ul>
 * 
 * @author tds
 *
 */
public interface BaseServiceItf {

}
