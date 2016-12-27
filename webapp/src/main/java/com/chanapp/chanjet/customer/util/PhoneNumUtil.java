package com.chanapp.chanjet.customer.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat;
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberType;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;

public class PhoneNumUtil {

    private static final Logger log = LoggerFactory.getLogger(PhoneNumUtil.class);

    private static String parsePhoneNum(String num, boolean containsAreaCode) throws NumberParseException {
        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
        String region = "CN";
        try {
            PhoneNumber phoneNumber = phoneUtil.parse(num, region);
            String tempPhone = phoneUtil.format(phoneNumber, PhoneNumberFormat.NATIONAL);
            if (phoneUtil.isValidNumber(phoneNumber)) {
                String phone = phoneUtil.format(phoneNumber, PhoneNumberFormat.NATIONAL);
                PhoneNumberType numberType = phoneUtil.getNumberType(phoneNumber);
                if (PhoneNumberType.FIXED_LINE.equals(numberType)) {
                    String realPhone = phone;
                    if (!containsAreaCode) {// 固话包含区号
                        realPhone = realPhone.substring(realPhone.indexOf(" "));
                    }
                    realPhone = realPhone.replace(" ", "");
                    return realPhone;
                } else {
                    String realPhone = phone.replace(" ", "");
                    return realPhone;
                }
            } else {
                tempPhone = tempPhone.replace(" ", "");
                String temp = tempPhone.substring(0, tempPhone.length() - 1);
                if (temp.length() > 6) {
                    return parsePhoneNum(temp, containsAreaCode);
                } else {
                    return "fail";
                }
            }
        } catch (NumberParseException e) {
            log.warn("parsePhoneNum exception:", num, e);
        }
        // 手机号码归属城市 referenceRegion
        return "fail";
    }

    public static String getEffectivePhoneNumString(String mobileString) {
        return getEffectivePhoneNumString(mobileString, false, true);
    }

    public static String getEffectivePhoneNumString(String mobileString, boolean containsAreaCode,
            boolean containsNotNumber) {
        List<String> listMobile = getEffectivePhoneNum(mobileString, containsAreaCode, containsNotNumber);
        String effectiveMobile = ",";
        for (String m : listMobile) {
            effectiveMobile += m + ",";
        }
        return effectiveMobile;
    }

    public static List<String> getEffectivePhoneNum(String mobileString) {
        return getEffectivePhoneNum(mobileString, false, true);
    }

    public static List<String> getEffectivePhoneNum(String mobileString, boolean containsAreaCode,
            boolean containsNotNumber) {
        // String mobileString =
        // "08331167-239-848,18911672319,+8618911672319,+86 189-11672-319,010 -
        // 11 67-239-848,11 67-239-848,324234324324234";
        List<String> numList = new ArrayList<String>();
        mobileString = mobileString.replace("，", ",");// 中文逗号 '，' to 英文 ','
        String[] mobileArray = mobileString.split(",");
        for (String mobile : mobileArray) {
            try {
                String effectiveNum = parsePhoneNum(mobile, containsAreaCode);
                if ("fail".equals(effectiveNum)) {
                    String tempPhone = null;
                    try {
                        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
                        String region = "CN";
                        PhoneNumber phoneNumber = phoneUtil.parse(mobile, region);
                        tempPhone = phoneUtil.format(phoneNumber, PhoneNumberFormat.NATIONAL);
                        //System.out.println("realPhone = " + tempPhone);
                    } catch (Exception e) {
                        // e.printStackTrace();
                        log.error(" phoneNum format exception:", mobile, e);
                    }
                    if (tempPhone == null) {
                        if (containsNotNumber) {
                            tempPhone = mobile;
                        } else {
                            String regEx = "[^0-9]";
                            Pattern p = Pattern.compile(regEx);
                            Matcher m = p.matcher(mobile);
                            tempPhone = m.replaceAll("").trim();
                        }
                    }
                    tempPhone = tempPhone.replace(" ", "");
                    numList.add(tempPhone);
                } else {
                    //System.out.println("realPhone = " + effectiveNum);
                    effectiveNum = effectiveNum.replace(" ", "");
                    numList.add(effectiveNum);
                }
            } catch (Exception e) {
                // e.printStackTrace();
                log.error(" getEffectivePhoneNum exception:", mobile, e);
            }
        }
        return numList;
    }
}