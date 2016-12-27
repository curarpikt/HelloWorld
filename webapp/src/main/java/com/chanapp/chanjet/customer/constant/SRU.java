package com.chanapp.chanjet.customer.constant;

public interface SRU {
    // 用户状态
    String STATUS_ENABLE = "enable";
    String STATUS_DISABLE = "disable";
    // 用户角色类型
    // 老板
    String ROLE_BOSS = "boss";
    // 管理员
    String ROLE_MANAGER = "manager";
    // 主管
    String ROLE_SUPERISOR = "supervisor";
    // 业务员
    String ROLE_SALESMAN = "salesman";
    // 老板或管理员
    Long LEVEL_BOSS = 1l;
    // 默认
    Long LEVEL_DEFAULT = 0l;
    // 主管
    Long LEVEL_SUPERISOR = 2l;
    // 业务员
    Long LEVEL_SALESMAN = 3l;
    //启用字段
	Long FIELD_STATUS_ENABLE = 1L;
	//停用字段
	Long FIELD_STATUS_DISABLE = 0L;
}
