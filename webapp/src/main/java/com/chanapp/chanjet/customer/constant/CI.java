package com.chanapp.chanjet.customer.constant;

public interface CI {
    // 签到状态 - 正常
    Long STATUS_NORMAL = 0l;
    // 签到状态 - 迟到
    Long STATUS_LATE = 1l;
    // 签到状态 - 早退
    Long STATUS_EARLY = 2l;
    // 签到状态 - 缺勤
    Long STATUS_ABSENSE = 3l;
}
