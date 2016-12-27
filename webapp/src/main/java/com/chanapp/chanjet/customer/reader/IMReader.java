package com.chanapp.chanjet.customer.reader;

import com.chanapp.chanjet.web.reader.PropertiesReader;

/**
 * IM相关配置
 * 
 * @author tds
 *
 */
public class IMReader {
    final static PropertiesReader reader = PropertiesReader.getInstance("customer/im.properties");

    public static String getDomain() {
        String ctype = System.getProperty("config.type");
        if (ctype == null) {
            return reader.getString("online.domain");
        } else if (ctype.equals("dev")) {
            return reader.getString("dev.domain");
        } else if (ctype.equals("test")) {
            return reader.getString("test.domain");
        }

        return reader.getString("online.domain");
    }

    public static String getPushUrl() {
        return getDomain() + reader.getString("im.url.push");
    }

    public static String getHistoryUrl() {
        return getDomain() + reader.getString("im.url.history");
    }

    public static String getUnreadCountUrl() {
        return getDomain() + reader.getString("im.url.getUnreadCount");
    }

    public static String getResetUnreadCountUrl() {
        return getDomain() + reader.getString("im.url.resetUnreadCount");
    }
}
