package com.chanapp.chanjet.customer.util;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PeriodUtil {
    // 销售新进展类型
    public final static String COUNTTYPE_TODAY = "TODAY";
    public final static String COUNTTYPE_LASTWEEK = "LASTWEEK";
    public final static String COUNTTYPE_WEEK = "WEEK";
    public final static String COUNTTYPE_MONTH = "MONTH";
    public final static String LAST_MONTH = "LASTMONTH";
    public final static String COUNTTYPE_QUARTER = "QUARTER";
    public final static String COUNTTYPE_CUSTOM = "CUSTOM";
    public final static String SPLITFLAG = "||";

    private final static String[] dateValue = new String[] { COUNTTYPE_TODAY, COUNTTYPE_WEEK, COUNTTYPE_MONTH,
            COUNTTYPE_LASTWEEK, LAST_MONTH, COUNTTYPE_QUARTER };
    private final static String[] dateName = new String[] { "今天", "本周", "本月", "上周", "上月", "最近三个月" };

    public static Map<String, String> getDatePeriod() {
        Map<String, String> datePeriod = new LinkedHashMap<String, String>();
        int i = 0;
        for (String value : dateValue) {
            datePeriod.put(value, dateName[i]);
            i++;
        }
        return datePeriod;
    }

    public static List<Timestamp> getTimePeriod(String timetype) {
        String startDate = null;
        String endDate = null;
        List<Timestamp> ts = new ArrayList<Timestamp>();
        if (timetype.indexOf(SPLITFLAG) != -1) {
            timetype = timetype.trim();
            if (timetype.indexOf(SPLITFLAG) + 2 == timetype.length()) {
                startDate = timetype.substring(0, timetype.indexOf(SPLITFLAG));
                endDate = "2999-01-01";
            } else if (timetype.indexOf(SPLITFLAG) == 0) {
                startDate = "1900-01-01";
                endDate = timetype.substring(timetype.indexOf(SPLITFLAG) + SPLITFLAG.length(), timetype.length());
            } else {
                startDate = timetype.substring(0, timetype.indexOf(SPLITFLAG));
                endDate = timetype.substring(timetype.indexOf(SPLITFLAG) + SPLITFLAG.length(), timetype.length());
            }
            timetype = COUNTTYPE_CUSTOM;
        }
        Date followstart = null;
        Date followend = null;
        Timestamp startTS = null;
        Timestamp endTS = null;
        if (COUNTTYPE_WEEK.equalsIgnoreCase(timetype)) {
            followstart = DateUtil.getCurrentWeekStart();
            followend = DateUtil.getCurrentWeekEnd();
        } else if (COUNTTYPE_TODAY.equalsIgnoreCase(timetype)) {
            followstart = DateUtil.getNowDateTime();
        } else if (COUNTTYPE_MONTH.equalsIgnoreCase(timetype)) {
            followstart = DateUtil.getCurrentMonthStart();
            followend = DateUtil.getCurrentMonthEnd();
        } else if (COUNTTYPE_LASTWEEK.equalsIgnoreCase(timetype)) {
            followstart = DateUtil.getLastWeekStart();
            followend = DateUtil.getLastWeekEnd();
        } else if (LAST_MONTH.equalsIgnoreCase(timetype)) {
            followstart = DateUtil.getLastMonthStart();
            followend = DateUtil.getLastMonthEnd();
        } else if (COUNTTYPE_QUARTER.equalsIgnoreCase(timetype)) {
            followstart = DateUtil.getLastThreeMonthStart();
            followend = DateUtil.getCurrentMonthEnd();
        } else if (COUNTTYPE_CUSTOM.equalsIgnoreCase(timetype)) {
            followstart = DateUtil.getDateByString(startDate);
            followend = DateUtil.getDateByString(endDate);
        }
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        if (followend != null) {

            startTS = Timestamp.valueOf(df.format(followstart) + " 00:00:00.000");
            endTS = Timestamp.valueOf(df.format(followend) + " 23:59:59.999");
        } else {
            startTS = Timestamp.valueOf(df.format(followstart) + " 00:00:00.000");
            endTS = Timestamp.valueOf(df.format(new Date()) + " 23:59:59.999");
        }
        ts.add(startTS);
        ts.add(endTS);
        return ts;
    }

}
