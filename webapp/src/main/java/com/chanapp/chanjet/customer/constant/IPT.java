package com.chanapp.chanjet.customer.constant;

/**
 * @author tds
 *
 */
public interface IPT {
    String MSG_NULL = "不能为空";
    String ERRORTYPE_NULL = "vnull";
    /**
     * 外键关系 名称查找不存在
     */
    String MSG_NON = "不存在";
    String ERRORTYPE_NON = "non";
    /**
     * 外键关系 名称已存在
     */
    String MSG_CEXISTS = "客户名称已存在";
    String ERRORTYPE_CEXISTS = "cexists";
    String MSG_PEXISTS = "客户电话已存在";
    String ERRORTYPE_PEXISTS = "cexists";

    /**
     * 外键关系 名称查找存在多个
     */
    String MSG_MULTI = "有多个相同的用户";
    String ERRORTYPE_MULTI = "multi";

    String MSG_ENUM = "无效的枚举值";
    String ERRORTYPE_ENUM = "invalidEnum";
    String ERRORTYPE_TOLONG = "toLong";
    String MSG_FORMAT = "请填写正确格式的数据";
    String ERRORTYPE_FORMAT = "format";
}
