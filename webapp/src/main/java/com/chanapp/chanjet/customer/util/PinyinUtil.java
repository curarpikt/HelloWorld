package com.chanapp.chanjet.customer.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.chanjet.csp.common.base.util.StringUtils;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;

public class PinyinUtil {

    /**
     * 将字符串转换成拼音数组
     * 
     * @param src
     * @param isPolyphone 是否查出多音字的所有拼音
     * @param separator 多音字拼音之间的分隔符
     * @return
     */
    private static String[] stringToPinyin(String src, boolean isPolyphone, String separator) {
        // 判断字符串是否为空
        if ("".equals(src) || null == src) {
            return null;
        }
        char[] srcChar = src.toCharArray();
        int srcCount = srcChar.length;
        String[] srcStr = new String[srcCount];

        for (int i = 0; i < srcCount; i++) {
            srcStr[i] = charToPinyin(srcChar[i], isPolyphone, separator);
        }
        return srcStr;
    }

    /**
     * 将单个字符转换成拼音
     * 
     * @param src
     * @return
     */
    private static String charToPinyin(char src, boolean isPolyphone, String separator) {
        // 创建汉语拼音处理类
        HanyuPinyinOutputFormat defaultFormat = new HanyuPinyinOutputFormat();
        // 输出设置，大小写，音标方式
        defaultFormat.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        defaultFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);

        StringBuffer tempPinying = new StringBuffer();
        // 如果是中文
        if (src > 128) {
            try {
                // 转换得出结果
                String[] strs = PinyinHelper.toHanyuPinyinStringArray(src, defaultFormat);
                List<String> repeatList = new ArrayList<String>();
                // 是否查出多音字，默认是查出多音字的第一个字符
                if (isPolyphone && null != separator && strs != null) {
                    for (int i = 0; i < strs.length; i++) {
                        if (!repeatList.contains(strs[i])) {
                            repeatList.add(strs[i]);
                            if (tempPinying.length() > 0) {
                                // 多音字之间用特殊符号间隔起来
                                tempPinying.append(separator);
                            }
                            tempPinying.append(strs[i]);
                            /*
                             * if (strs.length != (i + 1)) { //
                             * tempPinying.append(separator); }
                             */
                        }
                    }
                } else {
                    if (strs != null) {// alter nullpointer
                        tempPinying.append(strs[0]);
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            tempPinying.append(src);
        }

        return tempPinying.toString();

    }

    private static List<String> combineArray(Set<String> a, String[] b) {
        List<String> c = new ArrayList<String>();
        if (a == null || a.size() < 1) {
            for (String bb : b) {
                c.add(bb);
            }
        }
        if (b == null || b.length < 1) {
            c.addAll(a);
        }
        // alter nullpointer
        if (a != null && b != null) {
            for (String aa : a) {
                for (String bb : b) {
                    c.add(aa + bb);
                }
            }
        }
        if (a != null) {
            a.clear();
        }
        return c;
    }

    public static String hanziToPinyin(String hanzi, boolean isPolyphone) {
        if (StringUtils.isEmpty(hanzi)) {
            return "";
        }
        return hanziToPinyin(hanzi, "", isPolyphone); // 20150313这里去掉空格分割，搜索时用户输入不带空格的汉语拼音搜不到。
    }

    /**
     * 将汉字转换成拼音
     * 
     * @param hanzi
     * @param separator
     * @return
     */
    @SuppressWarnings("deprecation")
    public static String hanziToPinyin(String hanzi, String separator, boolean isPolyphone) {
        // 创建汉语拼音处理类
        HanyuPinyinOutputFormat defaultFormat = new HanyuPinyinOutputFormat();
        // 输出设置，大小写，音标方式
        defaultFormat.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        defaultFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);

        StringBuffer pinyingStr = new StringBuffer();
        try {
            if (isPolyphone) {
                String polySeparator = ";";
                String[] pinyin1 = stringToPinyin(hanzi, isPolyphone, polySeparator);
                Set<String> result = new HashSet<String>();
                for (int i = 0; i < pinyin1.length; i++) {
                    String[] polyPinyin2 = pinyin1[i].split(polySeparator);
                    result.addAll(combineArray(result, polyPinyin2));
                }
                for (String r : result) {
                    pinyingStr.append(r).append(";");// 两组多音字添加分隔符
                }
            } else {
                pinyingStr.append(PinyinHelper.toHanyuPinyinString(hanzi, defaultFormat, separator));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return pinyingStr.toString();
    }

    /**
     * 将字符串数组转换成字符串
     * 
     * @param str
     * @param separator 各个字符串之间的分隔符
     * @return
     */
    private static String stringArrayToString(String[] str, String separator) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < str.length; i++) {
            sb.append(str[i]);
            if (str.length != (i + 1)) {
                sb.append(separator);
            }
        }
        return sb.toString();
    }

    /**
     * 简单的将各个字符数组之间连接起来
     * 
     * @param str
     * @return
     */
    private static String stringArrayToString(String[] str) {
        return stringArrayToString(str, "");
    }

    /**
     * 取汉字的首字母
     * 
     * @param src
     * @param isCapital 是否是大写
     * @return
     */
    private static char[] getHeadByChar(char src, boolean isCapital) {
        // if (!Character.toString(src).matches("[//u4E00-//u9FA5]+")) {
        // return new char[]{src};
        // }
        // 如果不是汉字直接返回
        if (src <= 128) {
            return new char[] { src };
        }
        // 获取所有的拼音
        String[] pinyingStr = PinyinHelper.toHanyuPinyinStringArray(src);
        if (pinyingStr != null) {
            // 创建返回对象
            int polyphoneSize = pinyingStr.length;
            char[] headChars = new char[polyphoneSize];
            int i = 0;
            // 截取首字符
            for (String s : pinyingStr) {
                char headChar = s.charAt(0);
                // 首字母是否大写，默认是小写
                if (isCapital) {
                    headChars[i] = Character.toUpperCase(headChar);
                } else {
                    headChars[i] = headChar;
                }
                i++;
            }
            return headChars;
        }
        return null;
    }

    /**
     * 查找字符串首字母
     * 
     * @param src
     * @param isCapital 是否大写
     * @return
     */
    private static String[] getHeadByString(String src, boolean isCapital) {
        return getHeadByString(src, isCapital, null);
    }

    /**
     * 查找字符串首字母
     * 
     * @param src
     * @param isCapital 是否大写
     * @param separator 分隔符
     * @return
     */
    private static String[] getHeadByString(String src, boolean isCapital, String separator) {
        char[] chars = src.toCharArray();
        String[] headString = new String[chars.length];
        int i = 0;
        for (char ch : chars) {
            char[] chs = getHeadByChar(ch, isCapital);
            StringBuffer sb = new StringBuffer();
            if (null != separator) {
                int j = 1;
                for (char ch1 : chs) {
                    sb.append(ch1);
                    if (j != chs.length) {
                        sb.append(separator);
                    }
                    j++;
                }
            } else {
                if (chs != null && chs.length > 0) {
                    sb.append(chs[0]);
                }
            }
            headString[i] = sb.toString();
            i++;
        }
        return headString;
    }

    /**
     * 获取字符串的首字母串
     * 
     * @param src
     * @return
     */
    private static String getHeadsByString(String src) {
        if (StringUtils.isEmpty(src)) {
            return "";
        }
        String heads = getHeadsByString(src, false);
        return heads;
    }

    /**
     * 获取字符串的首字母串
     * 
     * @param src
     * @param isCapital
     * @return
     */
    private static String getHeadsByString(String src, boolean isCapital) {
        if (StringUtils.isEmpty(src)) {
            return "";
        }
        src = src.trim().replace(" ", "");// 除去空格， 去除中文空格
        String[] words = getHeadByString(src, isCapital);
        if (null == words || words.length == 0) {
            return "";
        }
        String heads = stringArrayToString(words);
        return heads;
    }

    /**
     * 
     * <p>
     * 全拼
     * </p>
     */
    public static String hanziToPinyinFull(String sentence, boolean polyphonic) {
        // 使用智能平台组件改成开源组件 线上有问题 改为使用开源组件
        return hanziToPinyin(sentence, false);
    }

    /**
     * 
     * <p>
     * 简拼
     * </p>
     */
    public static String hanziToPinyinSimple(String sentence, boolean polyphonic) {
        // 使用智能平台组件改成开源组件 线上有问题 改为使用开源组件
        return getHeadsByString(sentence);
    }

}