package com.chanapp.chanjet.customer.reader;

import com.chanapp.chanjet.web.reader.PropertiesReader;

/**
 * 大数据相关配置
 * 
 * @author tds
 *
 */
public class BigDataReader {
    final static PropertiesReader reader = PropertiesReader.getInstance("customer/bd.properties");

    public static String getDomain() {
        return reader.getString("bd.domain");
    }

    public static String getUpitemDetailDataUrl() {
        return getDomain() + reader.getString("bd.url.upitemDetailData");
    }
}
