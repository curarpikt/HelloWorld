package com.chanapp.chanjet.customer.util;

import java.util.HashMap;
import java.util.Map;

public class Context {
    public static String log_time_ms = "log_time_ms";
    public static String log_last_time_ms = "log_last_time_ms";
    public static String log_id = "log_id";
    public static String clientIpAddress = "clientIpAddress";
    public static String request = "request";

    private static ThreadLocal<Map<String, Object>> local = new ThreadLocal<Map<String, Object>>() {
        // 默认开辟256个空间，空置率为0时再拓展空间;也就是说一次请求在local中put的变量数量在32个以内不会触发Map的拓容。
        protected Map<String, Object> initialValue() {
            return new HashMap<String, Object>(256, 1.0f);
        }
    };

    public static Object get(String key) {
        return local.get().get(key);
    }

    public static Object put(String key, Object o) {
        return local.get().put(key, o);
    }

    public static Object remove(String key) {
        return local.get().remove(key);
    }

    public static void clear() {
        local.get().clear();
    }

}
