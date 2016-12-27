package com.chanapp.chanjet.customer.service.todotips;

import java.util.Map;

import com.chanapp.chanjet.customer.businessobject.api.todotips.ITodoTipsHome;
import com.chanapp.chanjet.customer.businessobject.api.todotips.ITodoTipsRow;
import com.chanapp.chanjet.customer.businessobject.api.todotips.ITodoTipsRowSet;
import com.chanapp.chanjet.web.service.BoBaseServiceItf;

public interface TodoTipsServiceItf extends BoBaseServiceItf<ITodoTipsHome, ITodoTipsRow, ITodoTipsRowSet> {
    Map<String, Object> findTodoTips();

    Map<String, Object> deleteTodoTips(Long id);

    Map<String, Object> addTodoTips(String todoTips);

    Map<String, Object> sortTodoTips(String ids);

    Map<String, Object> updateTodoTips(Long id, String todotips);
}
