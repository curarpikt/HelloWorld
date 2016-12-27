package com.chanapp.chanjet.customer.constant;

import java.util.HashMap;
import java.util.Map;

public class EO {
    public static final String Customer = "Customer";
    public static final String Contact = "Contact";
    public static final String WorkRecord = "WorkRecord";
    public static final String Checkin = "Checkin";
    public static final String Comment = "Comment";
    public static final String Attachment = "Attachment";
    public static final String User = "User";
    public static final String SysRelUser = "SysRelUser";
  //  public static final String ImportRecord = "ImportRecord";
    public static final String TodoTips = "TodoTips";
    public static final String TodoWork = "TodoWork";
    public static final String ExportTask = "ExportTask";
  //  public static final String FieldData = "FieldData";
    public static final String RecycleRelation = "RecycleRelation";
    public static final String UserSetting = "UserSetting";
    public static final String AppUser = "AppUser";
    final static Map<String, String> CNNames = new HashMap<String, String>();

    static {
        CNNames.put(Customer, "客户");
        CNNames.put(Contact, "联系人");
        CNNames.put(WorkRecord, "工作记录");
        CNNames.put(Checkin, "签到");
    }

    /**
     * 根据entityName返回其中文名称
     * 
     * @param entityName
     * @return
     */
    public static String cnName(String entityName) {
        return CNNames.containsKey(entityName) ? CNNames.get(entityName) : "";
    }
    
    public static final String[] NeedRecycles ={Contact,WorkRecord,Customer,Checkin};
    public static final String[] UnNeedRecycles ={Comment,Attachment,TodoWork,TodoTips};
}
