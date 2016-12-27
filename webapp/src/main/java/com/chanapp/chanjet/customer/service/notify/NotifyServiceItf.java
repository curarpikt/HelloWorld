package com.chanapp.chanjet.customer.service.notify;

import java.util.List;
import java.util.Map;

import com.chanapp.chanjet.web.service.BaseServiceItf;

/**
 * @author tds
 *
 */
public interface NotifyServiceItf extends BaseServiceItf {
    List<Map<String, Object>> records(long timeline, String categories, int count);

    Map<String, Object> unreadCount(String categories);

    Map<String, Object> restCount(String categories);
}
