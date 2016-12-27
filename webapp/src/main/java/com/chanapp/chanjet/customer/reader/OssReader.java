package com.chanapp.chanjet.customer.reader;

import com.chanapp.chanjet.web.reader.PropertiesReader;

/**
 * OSS相关配置
 * 
 * @author tds
 *
 */
public class OssReader {
    final static PropertiesReader reader = PropertiesReader.getInstance("customer/oss.properties");

    public static String getDomain(String key) {
        return reader.getString(key);
    }

    public static String getUpitemDetailDataUrl() {
        return getDomain("oss.preview.domain") + reader.getString("bd.url.upitemDetailData");
    }
}
