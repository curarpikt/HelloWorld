package com.chanapp.chanjet.customer.util;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import com.chanjet.csp.common.base.util.StringUtils;

public class DateUtil {
    public static String formatTimeStamp(Timestamp date, String format) {
        SimpleDateFormat df = null;
        String returnValue = "";
        if (date != null) {
            df = new SimpleDateFormat(format);
            returnValue = df.format(date);
        }
        return returnValue;
    }

    public static String getDateString(java.util.Date date) {
        return DateUtil.formatDate(date, "yyyy-MM-dd");
    }

    public static String getDateTimeString(java.util.Date date) {
        return DateUtil.formatDate(date, "yyyy-MM-dd HH:mm:ss");
    }

    public static String getDateStringByFormat(java.util.Date date, String format) {
        return DateUtil.formatDate(date, format);
    }

    public static Date getDateTimeByString(String dateValue) {
        Date date = DateUtil.parseDate(dateValue, "yyyy-MM-dd HH:mm:ss");
        if (date == null) {
            date = DateUtil.parseDate(dateValue, "yyyy-MM-dd");
        }
        return date;
    }

    public static Date getDateByString(String dateValue) {
        return DateUtil.parseDate(dateValue, "yyyy-MM-dd");
    }

    public static Date parseDate(String dateValue, String format) {
        SimpleDateFormat df = null;
        Date date = null;
        if (StringUtils.isNotEmpty(dateValue)) {
            df = new SimpleDateFormat(format);
            try {
                date = df.parse(dateValue);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return date;
    }

    public static String formatDate(Date date, String format) {
        SimpleDateFormat df = null;
        String returnValue = "";
        if (date != null) {
            df = new SimpleDateFormat(format);
            returnValue = df.format(date);
        }
        return returnValue;
    }

    public static Timestamp getNowDateTime() {
        return new Timestamp(new Date().getTime());
    }

    // 判断两个时间戳(Timestamp)是否在同一天
    public static boolean isTheSameDate(Timestamp time1, Timestamp time2) {
        if (time1 != null && time2 != null) {
            Calendar c1 = Calendar.getInstance();
            c1.setTime(time1);
            int y1 = c1.get(Calendar.YEAR);
            int m1 = c1.get(Calendar.MONTH);
            int d1 = c1.get(Calendar.DATE);
            Calendar c2 = Calendar.getInstance();
            c2.setTime(time2);
            int y2 = c2.get(Calendar.YEAR);
            int m2 = c2.get(Calendar.MONTH);
            int d2 = c2.get(Calendar.DATE);
            if (y1 == y2 && m1 == m2 && d1 == d2) {
                return true;
            }
        } else {
            if (time1 == null && time2 == null) {
                return true;
            }
        }
        return false;
    }

    public static Timestamp getWeekFirstDay() {
        Calendar c = Calendar.getInstance();
        int weekday = c.get(7) - 1;
        if (weekday == 0)
            weekday = 7;
        c.add(5, -weekday);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return Timestamp.valueOf(sdf.format(c.getTime()));
    }

    /**
     * <p>
     * 获得和周一相差天数
     * </p>
     * 
     * @return
     *
     * @author : gxy
     * @date : 2014年8月11日
     */
    private static int getMondayDiffer() {
        Calendar cd = Calendar.getInstance();
        // 星期日是第一天
        int dayOfWeek = cd.get(Calendar.DAY_OF_WEEK);
        if (dayOfWeek == 1) {
            return -6;
        } else {
            return 2 - dayOfWeek;
        }
    }

    /**
     * 今天0时0分1秒
     */
    public static Date getTodayStart() {
        GregorianCalendar current = new GregorianCalendar();
        current.set(Calendar.HOUR_OF_DAY, 0);
        current.set(Calendar.MINUTE, 0);
        current.set(Calendar.SECOND, 1);
        return current.getTime();
    }

    /**
     * 今天23时59分59秒
     */
    public static Date getTodayEnd() {
        GregorianCalendar current = new GregorianCalendar();
        current.set(Calendar.HOUR_OF_DAY, 23);
        current.set(Calendar.MINUTE, 59);
        current.set(Calendar.SECOND, 59);
        return current.getTime();
    }

    /**
     * 上周一
     */
    public static Date getLastWeekStart() {
        int differ = getMondayDiffer();
        GregorianCalendar current = new GregorianCalendar();
        current.add(GregorianCalendar.DATE, differ - 7);
        current.set(Calendar.HOUR_OF_DAY, 0);
        current.set(Calendar.MINUTE, 0);
        current.set(Calendar.SECOND, 1);
        return current.getTime();
    }

    /**
     * 上周日
     */
    public static Date getLastWeekEnd() {
        int differ = getMondayDiffer();
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.add(Calendar.DATE, differ - 1);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        return calendar.getTime();
    }

    /**
     * 本周一
     */
    public static Date getCurrentWeekStart() {
        int differ = getMondayDiffer();
        GregorianCalendar current = new GregorianCalendar();
        current.add(GregorianCalendar.DATE, differ);
        current.set(Calendar.HOUR_OF_DAY, 0);
        current.set(Calendar.MINUTE, 0);
        current.set(Calendar.SECOND, 1);
        return current.getTime();
    }

    /**
     * 本周日
     */
    public static Date getCurrentWeekEnd() {
        int differ = getMondayDiffer();
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.add(GregorianCalendar.DATE, differ + 6);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        return calendar.getTime();
    }

    /**
     * 月初
     */
    public static Date getCurrentMonthStart() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMinimum(Calendar.DAY_OF_MONTH));
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 1);
        return calendar.getTime();
    }

    /**
     * 月末
     */
    public static Date getCurrentMonthEnd() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        return calendar.getTime();
    }

    /**
     * 上月初
     */
    public static Date getLastMonthStart() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMinimum(Calendar.DAY_OF_MONTH));
        calendar.add(Calendar.MONTH, -1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 1);
        return calendar.getTime();
    }

    /**
     * 上月月末
     */
    public static Date getLastMonthEnd() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        calendar.add(Calendar.MONTH, -1);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        return calendar.getTime();
    }

    /**
     * 上三个月初
     */
    public static Date getLastThreeMonthStart() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMinimum(Calendar.DAY_OF_MONTH));
        calendar.add(Calendar.MONTH, -3);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 1);
        return calendar.getTime();
    }

    /**
     * 上三个月月末
     */
    public static Date getLastThreeMonthEnd() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        calendar.add(Calendar.MONTH, -3);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        return calendar.getTime();
    }

    /**
     * 年初
     */
    public static Date getCurrentYearStart() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_YEAR, calendar.getActualMinimum(Calendar.DAY_OF_YEAR));
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 1);
        return calendar.getTime();
    }

    /**
     * 年末
     */
    public static Date getCurrentYearEnd() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_YEAR, calendar.getActualMaximum(Calendar.DAY_OF_YEAR));
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        return calendar.getTime();
    }

    /**
     * 当前季度的开始时间
     */
    public static Date getCurrentQuarterStart() {
        Calendar calendar = Calendar.getInstance();
        int currentMonth = calendar.get(Calendar.MONTH) + 1;
        if (currentMonth >= 1 && currentMonth <= 3)
            calendar.set(Calendar.MONTH, 0);
        else if (currentMonth >= 4 && currentMonth <= 6)
            calendar.set(Calendar.MONTH, 3);
        else if (currentMonth >= 7 && currentMonth <= 9)
            calendar.set(Calendar.MONTH, 6);
        else if (currentMonth >= 10 && currentMonth <= 12)
            calendar.set(Calendar.MONTH, 9);
        calendar.set(Calendar.DATE, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 1);
        return calendar.getTime();
    }

    /**
     * 当前季度的结束时间
     */
    public static Date getCurrentQuarterEnd() {
        Calendar c = Calendar.getInstance();
        int currentMonth = c.get(Calendar.MONTH) + 1;
        if (currentMonth >= 1 && currentMonth <= 3) {
            c.set(Calendar.MONTH, 2);
            c.set(Calendar.DATE, 31);
        } else if (currentMonth >= 4 && currentMonth <= 6) {
            c.set(Calendar.MONTH, 5);
            c.set(Calendar.DATE, 30);
        } else if (currentMonth >= 7 && currentMonth <= 9) {
            c.set(Calendar.MONTH, 8);
            c.set(Calendar.DATE, 30);
        } else if (currentMonth >= 10 && currentMonth <= 12) {
            c.set(Calendar.MONTH, 11);
            c.set(Calendar.DATE, 31);
        }
        c.set(Calendar.HOUR_OF_DAY, 23);
        c.set(Calendar.MINUTE, 59);
        c.set(Calendar.SECOND, 59);
        return c.getTime();
    }

    public static int dayForWeek(String pTime) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Calendar c = Calendar.getInstance();
        try {
            c.setTime(format.parse(pTime));
        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        }
        int dayForWeek = 0;
        if (c.get(Calendar.DAY_OF_WEEK) == 1) {
            dayForWeek = 7;
        } else {
            dayForWeek = c.get(Calendar.DAY_OF_WEEK) - 1;
        }
        return dayForWeek;
    }

    public static Date getDateTimeByStringUTC(String dateValue) {
    	dateValue = dateValue.replace("Z", " UTC");  
    	Date date = DateUtil.parseDate(dateValue, "yyyy-MM-dd'T'HH:mm:ss.SSS Z");
    	if(null == date){//兼容IE8
    		date = DateUtil.parseDate(dateValue, "yyyy-MM-dd'T'HH:mm:ss Z");
    	}
    	if(null == date){
    		date = DateUtil.parseDate(dateValue, "yyyy-MM-dd");
    	}
    	return date;
    }
}
