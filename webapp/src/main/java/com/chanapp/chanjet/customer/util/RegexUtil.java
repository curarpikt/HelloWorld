package com.chanapp.chanjet.customer.util;

import java.util.regex.Pattern;

public class RegexUtil {
    private static Pattern emailPattern = Pattern
            .compile("^[a-zA-Z0-9_-]{1,}+(.*)@[a-zA-Z0-9_-]{1,}+(.*)+(\\.)+[a-zA-Z]{1,}$");

    private static Pattern moblieSimplePattern = Pattern.compile("^(1)\\d{10}$");

    /**
     * 验证是否 字符串是email
     * 
     * @param email
     * @return true if email else false；
     */
    public static boolean validEmail(String email) {
        if (email == null || email.length() == 0 || email.indexOf(".") == -1 || email.indexOf("@") == -1
                || email.indexOf(" ") != -1) {
            return false;
        }
        return emailPattern.matcher(email).find();
    }

    public static boolean validSimpleMobile(String mobile) {
        if (mobile == null || mobile.length() == 0) {
            return false;
        }
        return moblieSimplePattern.matcher(mobile).find();
    }

}
