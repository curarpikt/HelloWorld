package com.chanapp.chanjet.customer.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chanjet.csp.common.base.rest.RestRequest;
import com.chanjet.csp.common.base.util.StringUtils;

public class UserAgentUtil {
    private static final Logger logger = LoggerFactory.getLogger(UserAgentUtil.class);

    /**
     * 是否是IE
     * 
     * @param request
     * @return
     */
    public static boolean isIE(RestRequest request) {
        String ua = request.getHeader("user-agent");
        logger.info("isIE ua={}", ua);
        if (StringUtils.isEmpty(ua)) {
            return false;
        }
        ua = ua.toUpperCase();
        if (ua.contains("MSIE") || ua.contains("RV:11")) {
            return true;
        }
        return false;
    }

}
