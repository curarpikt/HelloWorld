package com.chanapp.chanjet.web.reader;

/**
 * Service相关配置
 * 
 * @author tds
 *
 */
public class ServiceReader {
    final static PropertiesReader reader = PropertiesReader.getInstance("service.properties");

    public static String getImpl(String serviceName) {
        return reader.getString(serviceName, true);
    }
}
