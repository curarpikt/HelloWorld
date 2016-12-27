package com.chanapp.chanjet.customer.constant;

import java.util.HashMap;
import java.util.Map;

public class IM {
    // 客户共享
    public final static String CUSTOMER_SHARE_ID = "50000002005";
    public final static String CUSTOMER_SHARE = "CustomerShare";
    // 取消客户共享
    public final static String CUSTOMER_CANCEL_SHARE_ID = "50000002006";
    public final static String CUSTOMER_CANCEL_SHARE = "CustomerCancelShare";

    // 消息类型——工作记录的回复评论
    public final static String COMMENT_ID = "60000005587";
    public final static String COMMENT = "Comment";

    // 消息类型——系统通知
    public final static String SYSTEM_ID = "50000002000";
    public final static String SYSTEM = "Sys";

    // 权限变更
    public final static String PERMISSIONS_CHANGE_ID = "50000002001";
    public final static String PERMISSIONS_CHANGE = "PermissionsChange";

    // 客户移交
    public final static String CUSTOMER_TRANSFER_ID = "50000002002";
    public final static String CUSTOMER_TRANSFER = "CustomerTransfer";

    // 账号停用
    public final static String ACCOUNT_STOP_ID = "50000002003";
    public final static String ACCOUNT_STOP = "AccountStop";

    // 工作记录@类型
    public final static String WORK_RECORD_AT_ID = "50000002004";
    public final static String WORK_RECORD_AT = "WorkRecordAt";

    // 工作记录评论@类型
    public final static String COMMENT_AT_ID = "50000002007";
    public final static String COMMENT_AT = "CommentAt";

    // 工作记录评论回复类型
    public final static String COMMENT_REPLY_ID = "50000002008";
    public final static String COMMENT_REPLY = "CommentReply";

    public final static Map<String, String> localMsgType = new HashMap<String, String>();

    static {
        // 10-系统消息；20-应用系统消息；30-评论消息；40-@消息。

        // 系统消息
        localMsgType.put(SYSTEM_ID, "10.10");
        // 权限变更
        localMsgType.put(PERMISSIONS_CHANGE_ID, "20.10");
        // 账号停用
        localMsgType.put(ACCOUNT_STOP_ID, "20.20");
        // 客户共享
        localMsgType.put(CUSTOMER_SHARE_ID, "20.30");
        // 取消共享
        localMsgType.put(CUSTOMER_CANCEL_SHARE_ID, "20.40");
        // 客户移交
        localMsgType.put(CUSTOMER_TRANSFER_ID, "20.50");
        // 评论
        localMsgType.put(COMMENT_ID, "30.10");
        // 评论回复
        localMsgType.put(COMMENT_REPLY_ID, "30.20");
        // 工作记录AT
        localMsgType.put(WORK_RECORD_AT_ID, "40.10");
        // 评论AT
        localMsgType.put(COMMENT_AT_ID, "40.20");
    }
}
