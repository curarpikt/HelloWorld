package com.chanapp.chanjet.customer.service.todowork;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.chanapp.chanjet.customer.businessobject.api.todowork.ITodoWorkHome;
import com.chanapp.chanjet.customer.businessobject.api.todowork.ITodoWorkRow;
import com.chanapp.chanjet.customer.businessobject.api.todowork.ITodoWorkRowSet;
import com.chanapp.chanjet.customer.vo.Row;
import com.chanapp.chanjet.customer.vo.RowSet;
import com.chanapp.chanjet.web.service.BoBaseServiceItf;

public interface TodoWorkServiceItf extends BoBaseServiceItf<ITodoWorkHome, ITodoWorkRow, ITodoWorkRowSet> {
    /**
     * 根据工作记录Id查询待办提醒
     */
    List<ITodoWorkRow> findSetByWorkrecordIds(List<Long> workrecordIds);

    Map<Long, List<ITodoWorkRow>> findTodoWorkByWorkRecordId(List<Long> workrecordIds);

    Row addTodoWork(LinkedHashMap<String, Object> todoWorkParam);

    Row updateTodoWork(LinkedHashMap<String, Object> todoWorkParam);

    void deleteTodoWork(Long todoWorkId);

    Row getTodoWork(Long todoWorkId);

    void checkParams(String status);

    Long countTodoWorks(Long startDate, Long endDate, String status);

    ITodoWorkRowSet findByWorkrecordId(Long workrecordId);

    List<Map<String, Object>> getTodoRemindType();

    void handleTodoWork(LinkedHashMap<String, Object> todoWorkParam);

    Map<String, Object> getTodoTodoWorks();

    Row countDoneTodoWorks(String timeType);

    Map<String, Object> findTodoWorks(String timeType, String status, Integer pageNo, Integer pageSize);

    RowSet getAllTodoWorks(Long startDate, Long endDate, String status);
}
