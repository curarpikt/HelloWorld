package com.chanapp.chanjet.customer.service.todotips;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.chanapp.chanjet.customer.businessobject.api.todotips.ITodoTipsHome;
import com.chanapp.chanjet.customer.businessobject.api.todotips.ITodoTipsRow;
import com.chanapp.chanjet.customer.businessobject.api.todotips.ITodoTipsRowSet;
import com.chanapp.chanjet.customer.businessobject.api.usersetting.IUserSettingRow;
import com.chanapp.chanjet.customer.service.usersetting.UserSettingServiceItf;
import com.chanapp.chanjet.web.constant.SC;
import com.chanapp.chanjet.web.service.BoBaseServiceImpl;
import com.chanapp.chanjet.web.service.ServiceLocator;
import com.chanjet.csp.ccs.api.common.EnterpriseContext;
import com.chanjet.csp.common.base.exception.AppException;
import com.chanjet.csp.common.base.util.StringUtils;
import com.chanjet.csp.ui.util.Criteria;
import com.chanjet.csp.ui.util.JsonQueryBuilder;

public class TodoTipsServiceImpl extends BoBaseServiceImpl<ITodoTipsHome, ITodoTipsRow, ITodoTipsRowSet>
        implements TodoTipsServiceItf {

    @Override
    public void postUpsert(ITodoTipsRow row, ITodoTipsRow origRow) {
        if (this.isInsert(row, false)) {
            JsonQueryBuilder jsonQueryBuilder = JsonQueryBuilder.getInstance();
            Criteria criteria = Criteria.AND();
            criteria.eq("userId", row.getUserId());
            //criteria.addChild(Criteria.OR().empty(SC.isDeleted).eq(SC.isDeleted, false));
            jsonQueryBuilder.addCriteria(criteria);
            String jsonQuerySpec = jsonQueryBuilder.toJsonQuerySpec();

            this.batchIncrementalUpdate(jsonQuerySpec, new String[] { "sortBy" }, new Object[] { 1L }, true);
        }
    }

    @Override
    public void preUpsert(ITodoTipsRow row, ITodoTipsRow origRow) {
        long userId = EnterpriseContext.getCurrentUser().getUserLongId();
        String todoTips = row.getTodoTips();
        if (userId <= 0) {
            throw new AppException("app.privilege.user.invalid");
        }
        if (todoTips == null || "".equals(todoTips.trim())) {
            throw new AppException("app.todoWork.content.required");
        }
        if (todoTips.length() > 255) {
            throw new AppException("app.todoWork.content.overLimit");
        }
        // 新增
        if (this.isInsert(row, true)) {
            row.setUserId(userId);
            row.setSortBy(0L);
        }
        // 修改
        else {
            Long id = row.getId();
            Long orig_userId = origRow.getUserId();
            if (userId != orig_userId) {
                throw new AppException("app.todoWork.userId.iderror");
            }
            if (id < 0) {
                throw new AppException("app.todoWork.id.iderror");
            }
        }
    }

    private ITodoTipsRowSet getTodoTipsByUserId(Long userId) {
        Criteria criteria = Criteria.AND();
        criteria.eq("userId", userId);
        //criteria.addChild(Criteria.OR().empty(SC.isDeleted).eq(SC.isDeleted, false));
        String jsonQuerySpec = JsonQueryBuilder.getInstance().addCriteria(criteria).addOrderAsc("sortBy")
                .toJsonQuerySpec();
        ITodoTipsRowSet rowSet = query(jsonQuerySpec);
        return rowSet;
    }

    private ITodoTipsRowSet getDefalutTodoTips() {
        Criteria criteria = Criteria.AND().empty("userId");
       // criteria.addChild(Criteria.OR().empty(SC.isDeleted).eq(SC.isDeleted, false));
        String jsonQuerySpec = JsonQueryBuilder.getInstance().addOrderAsc("sortBy").addCriteria(criteria)
                .toJsonQuerySpec();
        ITodoTipsRowSet rowSet = query(jsonQuerySpec);
        return rowSet;
    }

    private TodoTipsValue copyDefalutTip(ITodoTipsRow row, Long userId) {
        ITodoTipsRow userTip = createRow();
        userTip.setTodoTips(row.getTodoTips());
        userTip.setUserId(userId);
        upsert(userTip);
        return getTodoTipsValue(userTip);
    }

    private TodoTipsValue getTodoTipsValue(ITodoTipsRow row) {
        TodoTipsValue value = new TodoTipsValue();
        value.setId(row.getId());
        value.setSortBy(row.getSortBy());
        value.setTodoTips(row.getTodoTips());
        return value;
    }

    @Override
    public Map<String, Object> findTodoTips() {
        Map<String, Object> result = new HashMap<String, Object>();
        Long userId = EnterpriseContext.getCurrentUser().getUserLongId();
        List<TodoTipsValue> retData = new ArrayList<TodoTipsValue>();
        ITodoTipsRowSet tips = null;
        synchronized (this) {
            tips = getTodoTipsByUserId(userId);
        }
        IUserSettingRow userSetRow = ServiceLocator.getInstance().lookup(UserSettingServiceItf.class)
                .getByKeyUserIdAndType(userId, "ToDoTipsFinish", null);
        if (userSetRow == null) {
            if (tips == null || tips.size() == 0) {
                ITodoTipsRowSet defalutTips = getDefalutTodoTips();

                List<ITodoTipsRow> tempList = defalutTips.getTodoTipsRows();
                Collections.reverse(tempList);
                for (ITodoTipsRow row : tempList) {
                    retData.add(copyDefalutTip(row, userId));
                }
                Collections.reverse(retData);
                ServiceLocator.getInstance().lookup(UserSettingServiceItf.class).generate("ToDoTipsFinish", null,
                        userId, null);
            }
        } else {
            for (ITodoTipsRow row : tips.getTodoTipsRows()) {
                retData.add(getTodoTipsValue(row));
            }
        }
        result.put("result", true);
        result.put("data", retData);
        return result;
    }

    @Override
    public Map<String, Object> deleteTodoTips(Long id) {
        Map<String, Object> result = new HashMap<String, Object>();
        this.deleteRowWithRecycle(id);
        //  upsert(row);
        result.put("status", 1);
        result.put("message", "保存成功。");
        result.put("result", true);
        return result;
    }

    @Override
    public Map<String, Object> addTodoTips(String todoTips) {
        Map<String, Object> result = new HashMap<String, Object>();

        ITodoTipsRow row = createRow();
        row.setTodoTips(todoTips);
        upsert(row);
        result.put("status", 1);
        result.put("message", "保存成功。");
        result.put("result", true);
        return result;
    }

    @Override
    public Map<String, Object> sortTodoTips(String ids) {
        long userId = EnterpriseContext.getCurrentUser().getUserLongId();
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("result", false);
        if (StringUtils.isEmpty(ids)) {
            result.put("message", "排序成功。");
            result.put("result", true);
            return result;
        }
        String[] tipIds = ids.split(",");
        List<String> list = Arrays.asList(tipIds);
        ITodoTipsRowSet tips = getTodoTipsByUserId(userId);
        if (list == null || tips == null) {
            throw new AppException("app.todoTips.para.error");
        }
        if (list.size() != tips.getTodoTipsRows().size()) {
            throw new AppException("app.todoTips.para.changed");
        }
        for (ITodoTipsRow row : tips.getTodoTipsRows()) {
            String tipId = row.getId().toString();
            int index = list.indexOf(tipId);
            if (index == -1) {
                throw new AppException("app.todoTips.para.error");
            }
            Long sortBy = Long.parseLong(index + 1 + "");
            row.setSortBy(sortBy);
            row.getBusinessObjectHome().upsert(row);
        }
        result.put("message", "排序成功。");
        result.put("result", true);
        return result;
    }

    @Override
    public Map<String, Object> updateTodoTips(Long id, String todotips) {
        long userId = EnterpriseContext.getCurrentUser().getUserLongId();
        Map<String, Object> result = new HashMap<String, Object>();
        ITodoTipsRow row = query(id);
        row.setTodoTips(todotips);
        row.setUserId(userId);
        upsert(row);
        result.put("status", 1);
        result.put("message", "更新成功。");
        result.put("result", true);
        return result;
    }
}
