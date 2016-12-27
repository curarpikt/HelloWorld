package com.chanapp.chanjet.customer.util;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import com.chanapp.chanjet.customer.constant.BO;
import com.chanapp.chanjet.customer.service.user.UserServiceItf;
import com.chanapp.chanjet.web.service.ServiceLocator;
import com.chanjet.csp.common.base.exception.AppException;
import com.chanjet.csp.common.base.util.StringUtils;

public class Assert {

    public static void isNull(Object object, String message) {
        if (object != null) {
            throw new AppException(message);
        }
    }

    public static void notNull(Object object, String message) {
        if (object == null) {
            throw new AppException(message);
        }
    }

    public static void notNull(Object object) {
        notNull(object, "[Assertion failed] - this argument is required; it must not be null");
    }

    public static void hasLength(String text, String message) {
        if (!StringUtils.hasLength(text)) {
            throw new AppException(message);
        }
    }

    public static void notEmpty(Map<?, ?> col, String message) {
        if (col == null || col.isEmpty()) {
            throw new AppException(message);
        }
    }

    public static void notEmpty(Map<?, ?> map) {
        notEmpty(map, "[Assertion failed] - this map must not be empty: it must contain at least 1 element");
    }

    public static void largePage(Integer pageSize, String message) {
        if (pageSize >= 1000) {
            throw new AppException(message);
        }
    }

    private static List<String> conditions = new ArrayList<String>();

    static {
        conditions.add(BO.Customer);
        conditions.add(BO.WorkRecord);
    }

    public static void inConditions(String codition, String message) {
        if (!conditions.contains(codition)) {
            throw new AppException(message);
        }
    }

    public static void maxAttachment(Integer pageSize) {
        if (pageSize >= 100) {
            throw new AppException("app.upload.maxAttachment.illege");
        }
    }

    public static void customerRepeat(Map<String, Object> repeatInfo) {
        if (repeatInfo != null) {
            Object[] paraMap = new Object[2];
            String msg = "app.customer.name.duplicated";
            try {
                String repeat = (String) repeatInfo.get("repeat");
                if ("all".equals(repeat)) {
                    msg = "app.customer.namephone.duplicated";
                } else if ("name".equals(repeat)) {
                    msg = "app.customer.name.duplicated";
                } else {
                    msg = "app.customer.phone.duplicated";
                }
                Timestamp createdDate = (Timestamp) repeatInfo.get("createdDate");
                String date = DateUtil.formatTimeStamp(createdDate, "yyyy年MM月dd日");
                String name = (String) repeatInfo.get("createdByName");
                paraMap[0] = date;
                paraMap[1] = name;
            } catch (Exception e) {
                e.printStackTrace();
            }
            throw new AppException(msg, paraMap);
            // throw new
            // ParamException("app.customer.name.duplicated",customer.getOwner().getName(),customer.getCreatedDate());
        }
    }

 

    public static void authBoss(Long userId) throws AppException {
        UserServiceItf userService = ServiceLocator.getInstance().lookup(UserServiceItf.class);
        if (!userService.isBoss(userId)) {
            throw new AppException("app.sysreluser.isnot.boss");
        }
    }

    public static void checkCountDate(String countDate, String errorMsg) {
        boolean countDateCheck = Pattern.matches("[12]\\d{3}-(0[1-9]|1[0-2]{1})", countDate);
        if (!countDateCheck) {
            throw new AppException(errorMsg);
        }
    }

    public static void checkCountDate(String countDate) {
        boolean countDateCheck = Pattern.matches("[12]\\d{3}-(0[1-9]|1[0-2]{1})", countDate);
        if (!countDateCheck) {
            throw new AppException("app.report.attendanceCount.countDate.illege");
        }
    }
}
