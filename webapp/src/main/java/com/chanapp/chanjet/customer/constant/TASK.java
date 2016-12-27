package com.chanapp.chanjet.customer.constant;

public interface TASK {
    /**
     * 备份任务状态<b>0-当日无任务，</b>1-当日任务新建，2-当日任务执行中，3-当日任务执行成功，9-当日任务执行失败
     * 99-后台数据异常当日产生了多个任务
     */
    public final static Long STATUS_0 = 0L;
    /**
     * 备份任务状态 0-当日无任务，<b>1-当日任务新建，</b>2-当日任务执行中，3-当日任务执行成功，9-当日任务执行失败
     * 99-后台数据异常当日产生了多个任务
     */
    public final static Long STATUS_1 = 1L;
    /**
     * 备份任务状态0-当日无任务，1-当日任务新建，<b>2-当日任务执行中，</b>3-当日任务执行成功，9-当日任务执行失败
     * 99-后台数据异常当日产生了多个任务
     */
    public final static Long STATUS_2 = 2L;
    /**
     * 备份任务状态 0-当日无任务，1-当日任务新建，2-当日任务执行中，<b>3-当日任务执行成功，</b>9-当日任务执行失败
     * 99-后台数据异常当日产生了多个任务
     */
    public final static Long STATUS_3 = 3L;
    /**
     * 备份任务状态 0-当日无任务，1-当日任务新建，2-当日任务执行中，3-当日任务执行成功，<b>9-当日任务执行失败</b>
     * 99-后台数据异常当日产生了多个任务
     */
    public final static Long STATUS_9 = 9L;
    /**
     * 备份任务状态 0-当日无任务，1-当日任务新建，2-当日任务执行中，3-当日任务执行成功，9-当日任务执行失败
     * <b> 99-后台数据异常当日产生了多个任务</b>
     */
    public final static Long STATUS_99 = 99L;
}
