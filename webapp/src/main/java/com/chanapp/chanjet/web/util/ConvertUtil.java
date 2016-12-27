package com.chanapp.chanjet.web.util;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chanjet.csp.appmanager.AppWorkManager;
import com.chanjet.csp.common.base.exception.AppException;
import com.chanjet.csp.common.base.usertype.DynamicEnum;

public class ConvertUtil {
    private static final Logger logger = LoggerFactory.getLogger(ConvertUtil.class);

    /**
     * 字符串转Integer
     * 
     * @param value
     * @param defaultValue value为null或空时，返回的默认值
     * @return
     */
    public static Integer toInt(String value, Integer defaultValue) {
        try {
            if (value == null || value.trim().isEmpty()) {
                return defaultValue;
            }
            return Integer.parseInt(value.trim());
        } catch (Exception e) {
            logger.error("{} toInt error:{}", value, e.getMessage());
            throw new AppException(value + " toInt error");
        }
    }

    public static Integer toInt(String value) {
        return toInt(value, null);
    }

    /**
     * 字符串转Long
     * 
     * @param value
     * @param defaultValue value为null或空时，返回的默认值
     * @return
     */
    public static Long toLong(String value, Long defaultValue) {
        try {
            if (value == null || value.trim().isEmpty()) {
                return defaultValue;
            }
            return Long.parseLong(value.trim());
        } catch (Exception e) {
            logger.error("{} toLong error:{}", value, e.getMessage());
            throw new AppException(value + " toLong error");
        }
    }

    public static Long toLong(String value) {
        return toLong(value, null);
    }

    /**
     * 字符串转BigDecimal
     * 
     * @param value
     * @param defaultValue value为null或空时，返回的默认值
     * @return
     */
    public static BigDecimal toBigDecimal(String value, BigDecimal defaultValue) {
        try {
            if (value == null || value.trim().isEmpty()) {
                return defaultValue;
            }
            return new BigDecimal(value.trim());
        } catch (Exception e) {
            logger.error("{} toBigDecimal error:{}", value, e.getMessage());
            throw new AppException(value + " toBigDecimal error");
        }
    }

    public static BigDecimal toBigDecimal(String value) {
        return toBigDecimal(value, null);
    }

    /**
     * 字符串转Double
     * 
     * @param value
     * @param defaultValue value为null或空时，返回的默认值
     * @return
     */
    public static Double toDouble(String value, Double defaultValue) {
        try {
            if (value == null || value.trim().isEmpty()) {
                return defaultValue;
            }
            return Double.parseDouble(value.trim());
        } catch (Exception e) {
            logger.error("{} toDouble error:{}", value, e.getMessage());
            throw new AppException(value + " toDouble error");
        }
    }

    public static Double toDouble(String value) {
        return toDouble(value, null);
    }

    /**
     * 字符串转Float
     * 
     * @param value
     * @param defaultValue value为null或空时，返回的默认值
     * @return
     */
    public static Float toFloat(String value, Float defaultValue) {
        try {
            if (value == null || value.trim().isEmpty()) {
                return defaultValue;
            }
            return Float.parseFloat(value.trim());
        } catch (Exception e) {
            logger.error("{} toFloat error:{}", value, e.getMessage());
            throw new AppException(value + " toFloat error");
        }
    }

    public static Float toFloat(String value) {
        return toFloat(value, null);
    }

    /**
     * 字符串转Boolean
     * 
     * @param value
     * @param defaultValue value为null或空时，返回的默认值
     * @return
     */
    public static Boolean toBoolean(String value, Boolean defaultValue) {
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        value = value.trim().toUpperCase();
        if ("1".equals(value) || "TRUE".equals(value) || "T".equals(value) || "Y".equals(value)) {
            return Boolean.TRUE;
        } else if ("0".equals(value) || "FALSE".equals(value) || "F".equals(value) || "N".equals(value)) {
            return Boolean.FALSE;
        }
        logger.error("{} toBoolean error", value);
        throw new AppException(value + " toBoolean error");
    }

    public static Boolean toBoolean(String value) {
        return toBoolean(value, null);
    }

    /**
     * 字符串转java.util.Date
     * 
     * @param value
     * @param defaultValue value为null或空时，返回的默认值
     * @return
     */
    public static Date toDate(String value, Date defaultValue) {
        try {
            if (value == null || value.trim().isEmpty()) {
                return defaultValue;
            }
            return new SimpleDateFormat("yyyy-MM-dd").parse(value.trim());
        } catch (Exception e) {
            logger.error("{} toDate error:{}", value, e.getMessage());
            throw new AppException(value + " toDate error");
        }
    }

    public static Date toDate(String value) {
        return toDate(value, null);
    }

    /**
     * 字符串转java.sql.Timestamp
     * 
     * @param value
     * @param defaultValue value为null或空时，返回的默认值
     * @return
     */
    public static Timestamp toTimestamp(String value, Timestamp defaultValue) {
        try {
            if (value == null || value.trim().isEmpty()) {
                return defaultValue;
            }
            Long longValue = 0L;
            try {
                longValue = toLong(value);
                if (longValue <= 0) {
                    longValue = toDate(value).getTime();
                }
            } catch (Exception e) {

            }
            if (longValue <= 0) {
                throw new Exception(value + " toTimestamp error");
            }
            return new Timestamp(longValue);
        } catch (Exception e) {
            logger.error("{} toTimestamp error:{}", value, e.getMessage());
            throw new AppException(value + " toTimestamp error");
        }
    }

    public static Timestamp toTimestamp(String value) {
        return toTimestamp(value, null);
    }

    /**
     * 字符串转CspEnum
     * 
     * @param enumName 枚举名称
     * @param enumValue 枚举值
     * @return
     */
    public static DynamicEnum toCspEnum(String enumName, String enumValue) {
        try {
            if (enumName == null || enumName.trim().isEmpty() || enumValue == null || enumValue.trim().isEmpty()) {
                return null;
            }
            return AppWorkManager.getBoDataAccessManager().createDynamicEnumValue(enumName.trim(), enumValue.trim());
        } catch (Exception e) {
            logger.error("{}#{} toCspEnum error:{}", enumName, enumValue, e.getMessage());
            throw new AppException(enumName + "#" + enumValue + " toCspEnum error");
        }
    }

    /**
     * 转换为UTF-8编码，用于URL中的中文参数解码
     * 
     * @param str
     * @return 转换失败时，会返回原值
     */
    public static String toUTF8(String str) {
        try {
            return new String(str.getBytes("ISO-8859-1"), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            logger.warn("{} toUTF8 error :{}", str, e.getMessage());
        }
        return str;
    }

    /**
     * 从map中根据Key，取值，并转换为字符串
     * 
     * @param map
     * @param key
     * @param defaultValue
     * @return
     */
    public static String getStringFromMap(Map<String, Object> map, String key, String defaultValue) {
        if (map == null || key == null || !map.containsKey(key) || map.get(key) == null) {
            return defaultValue;
        }
        return map.get(key).toString();
    }

    /**
     * 从map中根据Key，取值，并转换为字符串，Key不存在时返回：""
     * 
     * @param map
     * @param key
     * @return
     */
    public static String getStringFromMap(Map<String, Object> map, String key) {
        return getStringFromMap(map, key, "");
    }
}
