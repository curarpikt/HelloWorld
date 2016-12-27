package com.chanapp.chanjet.customer.restlet.v2.web.user;

import java.util.HashMap;
import java.util.Map;

import com.chanapp.chanjet.web.restlet.BaseRestlet;

/**
 * 401，404回掉测试用
 * 
 * @author tds
 *
 */
public class UserInfo extends BaseRestlet {
    @Override
    public Object run() {
        Map<String, Object> resultMap = new HashMap<String, Object>();
        resultMap.put("success", true);
        return resultMap;
    }

}
