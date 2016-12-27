package com.chanapp.chanjet.customer.constant;

public interface BI {
    /**
     * 服务端埋点-用户初次登录
     */
    String USER_ADD = "0011001";
    /**
     * 服务端埋点-用户每次登录
     */
    String USER_LOGIN = "0011002";
    /**
     * 服务端埋点-新增客户
     */
    String CUSTOMER_ADD = "0021001";
    /**
     * 服务端埋点-新增联系人
     */
    String CONTACT_ADD = "0031001";
    /**
     * 服务端埋点-新增待办
     */
    String TODOWORK_ADD = "0041001";
    /**
     * 服务端埋点-新增工作记录
     */
    String WORKRECORD_ADD = "0051001";
    /**
     * 服务端埋点-设置考勤时间
     */
    String CHECKIN_SETTIME = "0061001";
    /**
     * 服务端埋点-新增考勤
     */
    String CHECKIN_ADD = "0061002";
}
