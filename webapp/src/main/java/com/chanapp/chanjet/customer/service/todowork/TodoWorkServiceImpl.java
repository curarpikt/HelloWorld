package com.chanapp.chanjet.customer.service.todowork;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.chanapp.chanjet.customer.businessobject.api.contact.IContactRow;
import com.chanapp.chanjet.customer.businessobject.api.contact.IContactRowSet;
import com.chanapp.chanjet.customer.businessobject.api.customer.ICustomerRow;
import com.chanapp.chanjet.customer.businessobject.api.todowork.ITodoWorkHome;
import com.chanapp.chanjet.customer.businessobject.api.todowork.ITodoWorkRow;
import com.chanapp.chanjet.customer.businessobject.api.todowork.ITodoWorkRowSet;
import com.chanapp.chanjet.customer.cache.CSPEnum;
import com.chanapp.chanjet.customer.cache.CustomerMetaData;
import com.chanapp.chanjet.customer.cache.MetadataCacheBuilder;
import com.chanapp.chanjet.customer.constant.BI;
import com.chanapp.chanjet.customer.constant.CT;
import com.chanapp.chanjet.customer.constant.TD;
import com.chanapp.chanjet.customer.constant.metadata.TodoWorkMetaData;
import com.chanapp.chanjet.customer.service.contact.ContactServiceItf;
import com.chanapp.chanjet.customer.service.customer.CustomerServiceItf;
import com.chanapp.chanjet.customer.service.operationlog.OperationLogServiceItf;
import com.chanapp.chanjet.customer.service.workrecord.WorkRecordServiceItf;
import com.chanapp.chanjet.customer.util.Assert;
import com.chanapp.chanjet.customer.util.BoRowConvertUtil;
import com.chanapp.chanjet.customer.util.DateUtil;
import com.chanapp.chanjet.customer.vo.Row;
import com.chanapp.chanjet.customer.vo.RowSet;
import com.chanapp.chanjet.web.constant.SC;
import com.chanapp.chanjet.web.service.BoBaseServiceImpl;
import com.chanapp.chanjet.web.service.ServiceLocator;
import com.chanapp.chanjet.web.util.ConvertUtil;
import com.chanjet.csp.appmanager.AppWorkManager;
import com.chanjet.csp.bo.api.IBusinessObjectRow;
import com.chanjet.csp.ccs.api.common.EnterpriseContext;
import com.chanjet.csp.common.base.exception.AppException;
import com.chanjet.csp.common.base.usertype.DynamicEnum;
import com.chanjet.csp.common.base.util.StringUtils;
import com.chanjet.csp.ui.util.Criteria;
import com.chanjet.csp.ui.util.JsonQueryBuilder;

public class TodoWorkServiceImpl extends BoBaseServiceImpl<ITodoWorkHome, ITodoWorkRow, ITodoWorkRowSet>
        implements TodoWorkServiceItf {

    @Override
    public List<ITodoWorkRow> findSetByWorkrecordIds(List<Long> workrecordIds) {
        List<ITodoWorkRow> rowList = new ArrayList<ITodoWorkRow>();
        if (workrecordIds != null && workrecordIds.size() > 0) {
            Criteria criteria = Criteria.AND();
            //criteria.addChild(Criteria.OR().empty(SC.isDeleted).eq(SC.isDeleted, false));
            criteria.in(TodoWorkMetaData.workRecord, workrecordIds.toArray());

            JsonQueryBuilder jsonQueryBuilder = JsonQueryBuilder.getInstance();
            jsonQueryBuilder.addCriteria(criteria);

            ITodoWorkRowSet rowSet = queryAll(jsonQueryBuilder.toJsonQuerySpec());

            if (rowSet != null && rowSet.getTodoWorkRows() != null) {
                rowList = rowSet.getTodoWorkRows();
            }
        }
        return rowList;
    }

    @Override
    public void postUpsert(ITodoWorkRow row, ITodoWorkRow origRow) {
        if (this.isInsert(row, false)) {
            OperationLogServiceItf operationLogService = ServiceLocator.getInstance()
                    .lookup(OperationLogServiceItf.class);
            operationLogService.writeMsg2BigData(row.getId(), BI.TODOWORK_ADD, 0);
        }
    }

    @Override
    public void preUpsert(ITodoWorkRow row, ITodoWorkRow origRow) {
        if (!this.isInsert(row, true)) { 
            preUpdate(row, origRow);           
            // 新增
        } else {
            preInsert(row);
        }
    }

    private void preInsert(ITodoWorkRow row) {
        preUpsertCheck(row);
        preInsertCheck(row);
        preUpsertSet(row);
    }

    private void preUpdate(ITodoWorkRow row, ITodoWorkRow origRow) {
        preUpsertCheck(row);
        preUpdateCheck(row, origRow);
        preUpsertSet(row);
        preUpdateSet(row);
    }

    private void preLogicDel(ITodoWorkRow origRow) {
        preDeleteCheck(origRow);

        OperationLogServiceItf operationLogService = ServiceLocator.getInstance().lookup(OperationLogServiceItf.class);
        operationLogService.generate(origRow, "DELETE");
    }

    /**
     * 删除待办的通用检查。
     * 
     * @param row
     */
    private void preDeleteCheck(ITodoWorkRow row) {
        //checkRowIsDeleted(row);
        checkOwner(row.getOwner());
    }

    /**
     * 检查当前待办是否是非伦理删除
     * 
     * @param row
     */
/*    private void checkRowIsDeleted(ITodoWorkRow row) {
        ITodoWorkRow checkRow = null;
        if (!row.getIsDeleted()) {
            checkRow = row;
        }
        Assert.notNull(checkRow, "app.todoWork.object.notexist");
    }*/

    /**
     * 检查当前用户是不是待办的所有者
     * 
     * @param owner
     */
    private void checkOwner(Long owner) {
        Long userId = EnterpriseContext.getCurrentUser().getUserLongId();
        if (owner != null && !userId.equals(owner)) {
            throw new AppException("app.privilege.user.invalid.invalidoper");
        }
    }

    /**
     * 新增待办和变更待办的通用检查。
     * 
     * @param row
     */
    private void preUpsertCheck(ITodoWorkRow row) {
        // 内容
        Assert.notNull(row.getWorkContent(), "app.todoWork.content.required");
    }

    /**
     * 新增待办前数据检查
     * 
     * @param row
     */
    private void preInsertCheck(ITodoWorkRow row) {
        // 检查客户权限
        customerAuthCheck(row);
        workRecordAuthCheck(row);
    }

    /**
     * 根据客户ID检查当前用户对该客户是否有选择权限 且如果该客户是伦理删除的话，则清空待办里面的客户
     * 
     * @param row 客户ROW对象
     */
    private void customerAuthCheck(ITodoWorkRow row) {
        CustomerServiceItf customerService = ServiceLocator.getInstance().lookup(CustomerServiceItf.class);
        if (row.getCustomer() != null) {
            ICustomerRow customer = customerService.query(row.getCustomer());
            if (customer == null) {
                row.setCustomer(null);
            }
        }
    }

    /**
     * 根据工作记录ID检查当前用户对该工作记录是否有选择权限。 且如果该工作记录是伦理删除的话，则清空待办里面的工作记录。
     * 
     * @param row 客户ROW对象
     */
    private void workRecordAuthCheck(ITodoWorkRow row) {
        if (row.getWorkRecord() != null) {
            WorkRecordServiceItf workRecordService = ServiceLocator.getInstance().lookup(WorkRecordServiceItf.class);
            IBusinessObjectRow workRecord = workRecordService.findByIdAndCusWithAuth(row.getWorkRecord());
            if (workRecord == null) {
                row.setWorkRecord(null);
            }
        }
    }

    /**
     * 新增待办和变更待办的通用设置。
     * 
     * @param row
     */
    private void preUpsertSet(ITodoWorkRow row) {
        // 状态
        DynamicEnum status = row.getStatus();
        if (status == null || StringUtils.isBlank(status.getValue())) {
            try {
                row.setStatus(boDataAccessManager.createDynamicEnumValue("todoStatus", TD.STATUS_TODO));
            } catch (Exception e) {
                throw new AppException("app.common.invalid.enum");
            }
        }
    }

    /**
     * 变更待办前数据检查
     * 
     * @param row
     */
    private void preUpdateCheck(ITodoWorkRow row, ITodoWorkRow origRow) {
        // 检查客户权限
        Long customerIdNew = row.getCustomer();
        Long customerIdOld = origRow.getCustomer();
        if (customerIdNew == null)
            customerIdNew = 0L;
        if (customerIdOld == null)
            customerIdOld = 0L;
        if (!customerIdNew.equals(0l) && customerIdNew.compareTo(customerIdOld) != 0) {
            customerAuthCheck(row);
        }
        // 检查工作记录权限
        Long workRecordIdNew = row.getWorkRecord();
        Long workRecordIdOld = origRow.getWorkRecord();
        if (workRecordIdNew == null)
            workRecordIdNew = 0L;
        if (workRecordIdOld == null)
            workRecordIdOld = 0L;
        if (!workRecordIdNew.equals(0l) && workRecordIdNew.compareTo(workRecordIdOld) != 0) {
            workRecordAuthCheck(row);
        }
       // checkRowIsDeleted(origRow);
        checkOwner(origRow.getOwner());
    }

    /**
     * 变更待办的通用设置。（不包含伦理删除）
     * 
     * @param row
     */
    private void preUpdateSet(ITodoWorkRow row) {
        Timestamp finishTime = row.getFinishTime();
        if (TD.STATUS_DONE.equals(row.getStatus().getValue()) && finishTime != null) {
            row.setFinishTime(finishTime);
        } else {
            row.setFinishTime(null);
        }
    }

    @Override
    public Map<Long, List<ITodoWorkRow>> findTodoWorkByWorkRecordId(List<Long> workrecordIds) {
        Map<Long, List<ITodoWorkRow>> map = new HashMap<Long, List<ITodoWorkRow>>();
        if (workrecordIds != null && workrecordIds.size() > 0) {
            List<ITodoWorkRow> todoWorks = findSetByWorkrecordIds(workrecordIds);
            for (ITodoWorkRow todoWork : todoWorks) {
                Long wrId = todoWork.getWorkRecord();
                if (!map.containsKey(wrId)) {
                    List<ITodoWorkRow> tmp = new ArrayList<ITodoWorkRow>();
                    tmp.add(todoWork);
                    map.put(wrId, tmp);
                } else {
                    map.get(wrId).add(todoWork);
                }
            }
        }
        return map;
    }

    @Override
    public Row addTodoWork(LinkedHashMap<String, Object> todoWorkParam) {
        String localId = todoWorkParam.containsKey(TodoWorkMetaData.localId)
                ? todoWorkParam.get(TodoWorkMetaData.localId).toString() : "";
        ITodoWorkRow todoWork = getRowByLocalId(localId);
        parseValueToRow(todoWorkParam, todoWork);
        upsert(todoWork);
        return getContactsForTodoWork(todoWork);
    }

    @Override
    public Row updateTodoWork(LinkedHashMap<String, Object> todoWorkParam) {
        ITodoWorkRow todoWorkDb = findByIdWithAuth(ConvertUtil.toLong(todoWorkParam.get(SC.id).toString()));
        Assert.notNull(todoWorkDb, "app.todoWork.object.notexist");
        customerAuthCheck(todoWorkDb);
        parseValueToRow(todoWorkParam, todoWorkDb);

        upsert(todoWorkDb);
        return getContactsForTodoWork(todoWorkDb);
    }

    private Row getContactsForTodoWork(ITodoWorkRow todoWorkDb) {
        Row row = BoRowConvertUtil.toRow(todoWorkDb);
        Long customerId = todoWorkDb.getCustomer();
        if (customerId == null) {
            return row;
        }
        ICustomerRow customer = ServiceLocator.getInstance().lookup(CustomerServiceItf.class)
                .query(todoWorkDb.getCustomer());
        if (null != customer) {
            Row customerRow = new Row();
            customerRow.put("id", customerId);
            customerRow.put("name", customer.getName());
            customerRow.put("phone", customer.getPhone());
            DynamicEnum dynamicEnum = (DynamicEnum) customer.getStatus();
            if (dynamicEnum != null) {
                Row enumRow = new Row();
                CustomerMetaData customerMetaData = MetadataCacheBuilder.newBuilder().buildCache().get("metadata");
                CSPEnum cspEnum = customerMetaData.getEnums().get(dynamicEnum.getCspEnumName());
                if (cspEnum.getEnumValue(dynamicEnum.getValue()) != null) {
                    enumRow.put("value", dynamicEnum.getValue());
                    enumRow.put("label", cspEnum.getEnumValue(dynamicEnum.getValue()).getEnumLabel());
                } else {
                    enumRow.put("value", dynamicEnum.getValue());
                    enumRow.put("label", dynamicEnum.getLabel());
                }
                customerRow.put("status", enumRow);
            }
            boolean myCustomer = true;// 是否我的客户
            if (null != customer.getOwner()) {
                Long userId = AppWorkManager.getCurrAppUserId();
                if (!userId.equals(customer.getOwner())) {
                    myCustomer = false;
                }
            }
            customerRow.put("myCustomer", myCustomer);
            customerRow.put("isDeleted", customer.getIsDeleted());
            List<Row> contacts = new ArrayList<Row>();
            IContactRowSet contactList = ServiceLocator.getInstance().lookup(ContactServiceItf.class)
                    .findContactsByCustomer(customerId);
            if (null != contactList && null != contactList.getContactRows()) {
                for (IContactRow contact : contactList.getContactRows()) {
                    Row contactRow = new Row();
                    contactRow.put("id", contact.getId());
                    contactRow.put("name", contact.getName());
                    contactRow.put("mobile", contact.getMobile());
                    contactRow.put("email", contact.getEmail());
                    contacts.add(contactRow);
                }
            }
            customerRow.put("contacts", contacts);
            row.put("customer", customerRow);
        }
        return row;
    }

    private void parseValueToRow(LinkedHashMap<String, Object> todoWorkParam, ITodoWorkRow row) {
        row.setWorkContent((String) todoWorkParam.get(TodoWorkMetaData.workContent));
        Long customerId = ConvertUtil.toLong(ConvertUtil.getStringFromMap(todoWorkParam, "customerId"));
        if (customerId != null && !customerId.equals(0L)) {
            row.setCustomer(customerId);
        }

        Long workRecordId = ConvertUtil.toLong(ConvertUtil.getStringFromMap(todoWorkParam, "workrecordId"));
        if (workRecordId != null && !workRecordId.equals(0L)) {
            row.setWorkRecord(workRecordId);
        }

        Long planTime = todoWorkParam.containsKey(TodoWorkMetaData.planTime)
                ? ConvertUtil.toLong(todoWorkParam.get(TodoWorkMetaData.planTime).toString()) : 0L;
        if (planTime != null && planTime != 0l) {
            Timestamp planTimeTmp = new Timestamp(planTime);
            row.setPlanTime(planTimeTmp);
        } else {
            row.setPlanTime(null);
        }
        String status = (String) todoWorkParam.get(TodoWorkMetaData.status);
        if (StringUtils.isEmpty(status)) {
            row.setStatus(null);
        } else {
            row.setStatus(boDataAccessManager.createDynamicEnumValue("todoStatus", status));
        }
        Long finishTime = todoWorkParam.containsKey(TodoWorkMetaData.finishTime)
                ? ConvertUtil.toLong(todoWorkParam.get(TodoWorkMetaData.finishTime).toString()) : 0L;
        if (TD.STATUS_DONE.equals(status) && finishTime != null && finishTime > 0l) {
            Timestamp finishTimeTmp = new Timestamp(finishTime);
            row.setFinishTime(finishTimeTmp);
        } else {
            row.setFinishTime(null);
        }

        Long remindType = -1l;
        if (null != todoWorkParam.get(TodoWorkMetaData.remindType)) {
            remindType = ConvertUtil.toLong(todoWorkParam.get(TodoWorkMetaData.remindType).toString());
        }
        row.setRemindType(remindType);
        if(todoWorkParam.get(TodoWorkMetaData.localId)!=null)
        	row.setLocalId(todoWorkParam.get(TodoWorkMetaData.localId).toString());
        Long remindTime = todoWorkParam.containsKey(TodoWorkMetaData.remindTime)
                ? ConvertUtil.toLong(todoWorkParam.get(TodoWorkMetaData.remindTime).toString()) : 0L;
        if (remindTime != null && remindTime != 0l) {
            Timestamp remindTimeTmp = new Timestamp(remindTime);
            row.setRemindTime(remindTimeTmp);
        } else {
            row.setRemindTime(null);
        }

    }

    @Override
    public void deleteTodoWork(Long todoWorkId) {
        Assert.notNull(todoWorkId, "app.todoWork.object.required");
        ITodoWorkRow todoWorkDb = findByIdWithAuth(todoWorkId);
        Assert.notNull(todoWorkDb, "app.todoWork.object.notexist");
        ITodoWorkRow row = query(todoWorkId);
        OperationLogServiceItf operationLogService = ServiceLocator.getInstance().lookup(OperationLogServiceItf.class);
        operationLogService.generate(row, "DELETE");
        deleteRowWithRecycle(todoWorkId);
    }

    @Override
    public Row getTodoWork(Long todoWorkId) {
        ITodoWorkRow todoWorkDb = findByIdWithAuth(todoWorkId);
        return BoRowConvertUtil.toRow(todoWorkDb);
    }

    @Override
    public void checkParams(String status) {
        if (StringUtils.isNotBlank(status)) {
            if (!status.equals(TD.STATUS_TODO) && !status.equals(TD.STATUS_DONE)) {
                throw new AppException("app.common.params.invalid");
            }
        }
    }

    @Override
    public Long countTodoWorks(Long startDate, Long endDate, String status) {
        List<Object> params = new ArrayList<Object>();
        String cqlQueryString = " select count(t.id) as NUM from " + this.getBusinessObjectId()
                + " t where (t.isDeleted is null or t.isDeleted =?)  and t.planTime is not null and t.owner.id = ? ";
        params.add(false);
        params.add(EnterpriseContext.getCurrentUser().getUserLongId());
        if (StringUtils.isNotBlank(status)) {
            cqlQueryString += " and t.status = ? ";
            DynamicEnum dynamicEnum = boDataAccessManager.createDynamicEnumValue("todoStatus", status);
            params.add(dynamicEnum);
        }
        if (startDate != null && !startDate.equals(0L)) {
            cqlQueryString += " and t.planTime > ? ";
            Timestamp start = new Timestamp(startDate);
            params.add(start);
        }
        if (endDate != null && !endDate.equals(0L)) {
            cqlQueryString += " and t.planTime < ? ";
            Timestamp endTimestamp = new Timestamp(endDate);
            params.add(endTimestamp);
        }
        List<Map<String, Object>> rs = runCQLQuery(cqlQueryString, params);
        if (!rs.isEmpty()) {
            return (Long) rs.get(0).get("NUM");
        }
        return 0l;
    }

    @Override
    public ITodoWorkRowSet findByWorkrecordId(Long workrecordId) {
        Criteria criteria = Criteria.AND();
       // criteria.addChild(Criteria.OR().empty(SC.isDeleted).eq(SC.isDeleted, false));
        criteria.eq(TodoWorkMetaData.workRecord, workrecordId);

        JsonQueryBuilder jsonQueryBuilder = JsonQueryBuilder.getInstance();
        jsonQueryBuilder.addCriteria(criteria);

        return queryAll(jsonQueryBuilder.toJsonQuerySpec());
    }

    @Override
    public List<Map<String, Object>> getTodoRemindType() {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("id", -1L);
        map.put("label", "不提醒");
        list.add(map);
        map = new HashMap<String, Object>();
        map.put("id", 0L);
        map.put("label", "准时提醒");
        list.add(map);
        map = new HashMap<String, Object>();
        map.put("id", 30l);
        map.put("label", "提前30分钟");
        list.add(map);
        map = new HashMap<String, Object>();
        map.put("id", 60l);
        map.put("label", "提前1小时");
        list.add(map);
        map = new HashMap<String, Object>();
        map.put("id", Long.valueOf(24 * 60));
        map.put("label", "提前1天");
        list.add(map);
        map = new HashMap<String, Object>();
        map.put("id", Long.valueOf(7 * 24 * 60));
        map.put("label", "提前1周");
        list.add(map);
        map = new HashMap<String, Object>();
        map.put("id", Long.valueOf(30 * 24 * 60));
        map.put("label", "提前1月");
        list.add(map);
        return list;
    }

    @Override
    public void handleTodoWork(LinkedHashMap<String, Object> todoWorkParam) {
        Assert.notNull(todoWorkParam, "app.todoWork.object.required");
        Assert.notNull(todoWorkParam.get(SC.id), "app.todoWork.object.required");
        Assert.notNull(todoWorkParam.get(TodoWorkMetaData.status), "app.todoWork.status.required");

        ITodoWorkRow todoWorkDb = findByIdWithAuth(ConvertUtil.toLong(todoWorkParam.get(SC.id).toString()));
        Assert.notNull(todoWorkDb, "app.todoWork.object.notexist");
        customerAuthCheck(todoWorkDb);

        todoWorkDb.setStatus(boDataAccessManager.createDynamicEnumValue("todoStatus",
                (String) todoWorkParam.get(TodoWorkMetaData.status)));

        if (TD.STATUS_DONE.equals((String) todoWorkParam.get(TodoWorkMetaData.status))) {
            Timestamp timestamp = new Timestamp(new Date().getTime());
            todoWorkDb.setFinishTime(timestamp);
        } else {
            todoWorkDb.setFinishTime(null);
        }

        upsert(todoWorkDb);
    }

    @Override
    public Map<String, Object> getTodoTodoWorks() {
        checkParams(TD.STATUS_TODO);
        ITodoWorkRowSet todoWorks = findTodoWorks(TD.STATUS_TODO);
        Map<String, Object> result = new HashMap<String, Object>();
        List<Row> list = new ArrayList<Row>();
        if (todoWorks != null) {
            for (int i = 0; i < todoWorks.size(); i++) {
                ITodoWorkRow todoWork = todoWorks.getRow(i);
                Row row = BoRowConvertUtil.toRow(todoWork);
                Long customerid = todoWork.getCustomer();
                if (customerid != null) {
                    ICustomerRow customer = ServiceLocator.getInstance().lookup(CustomerServiceItf.class)
                            .query(customerid);
                    if (customer != null) {
                        Row tempRow = new Row();
                        tempRow.put(SC.id, customerid);
                        tempRow.put(com.chanapp.chanjet.customer.constant.metadata.CustomerMetaData.name,
                                customer.getName());
                        row.put(TodoWorkMetaData.customer, tempRow);
                    }
                    DynamicEnum dynamicEnum = null;
                    if (customer != null && customer.getStatus() != null) {
                        dynamicEnum = customer.getStatus();
                    }
                    Row enumRow = new Row();
                    Row customerRow = (Row) row.get("customer");
                    if (dynamicEnum != null && customerRow != null) {
                        CustomerMetaData customerMetaData = MetadataCacheBuilder.newBuilder().buildCache()
                                .get("metadata");
                        CSPEnum cspEnum = customerMetaData.getEnums().get(dynamicEnum.getCspEnumName());
                        if (cspEnum.getEnumValue(dynamicEnum.getValue()) != null) {
                            enumRow.put("value", dynamicEnum.getValue());
                            enumRow.put("label", cspEnum.getEnumValue(dynamicEnum.getValue()).getEnumLabel());
                        } else {
                            enumRow.put("value", dynamicEnum.getValue());
                            enumRow.put("label", dynamicEnum.getLabel());
                        }
                        customerRow.put("status", enumRow);
                    }
                }
                Timestamp planTime = todoWork.getPlanTime();
                if (planTime != null) {
                    if (planTime.getTime() < new Date().getTime()) {
                        row.put("overDue", true);
                    } else {
                        row.put("overDue", false);
                    }
                }
                list.add(row);
            }
        }
        result.put("total", list.size());
        result.put("result", true);
        result.put("items", list);
        return result;
    }

    @Override
    public Map<String, Object> findTodoWorks(String timeType, String status, Integer pageNo, Integer pageSize) {
        checkParams(status);
        Long start = parseToStartDate(timeType);
        Long end = parseToEndDate(timeType);
        if (start == null && end == null) {
            throw new AppException("app.common.params.invalid");
        }
        Long total = _countDoneTodoWorks(start, end, status);
        long allPage = ((total - 1) / pageSize) + 1;
        if (allPage < pageNo) {
            pageNo = (int) allPage;
        }
        ITodoWorkRowSet todoWorks = findDoneTodoWorks(status, start, end, pageNo, pageSize);

        List<Row> list = new ArrayList<Row>();
        if (todoWorks != null) {
            for (int i = 0; i < todoWorks.size(); i++) {
                ITodoWorkRow todoWork = todoWorks.getRow(i);
                Row row = BoRowConvertUtil.toRow(todoWork);
                Long customerid = todoWork.getCustomer();
                if (customerid != null) {
                    ICustomerRow customer = ServiceLocator.getInstance().lookup(CustomerServiceItf.class)
                            .query(customerid);
                    if (customer != null) {
                        Row tempRow = new Row();
                        tempRow.put(SC.id, customerid);
                        tempRow.put(com.chanapp.chanjet.customer.constant.metadata.CustomerMetaData.name,
                                customer.getName());
                        row.put(TodoWorkMetaData.customer, tempRow);
                    }
                }
                list.add(row);
            }
        }

        Map<String, Object> result = new HashMap<String, Object>();
        if (total > (pageNo * pageSize)) {
            result.put("hasMore", true);
        } else {
            result.put("hasMore", false);
        }
        result.put("total", total);
        result.put("result", true);
        result.put("items", list);
        return result;
    }

    private ITodoWorkRowSet findTodoWorks(String status) {
        Criteria criteria = Criteria.AND();
        //criteria.addChild(Criteria.OR().empty(SC.isDeleted).eq(SC.isDeleted, false));
        criteria.eq(SC.owner, EnterpriseContext.getCurrentUser().getUserLongId());
        if (StringUtils.isNotBlank(status)) {
            // DynamicEnum dynamicEnum =
            // boDataAccessManager.createDynamicEnumValue("todoStatus", status);
            criteria.eq(TodoWorkMetaData.status, status);
        }
        JsonQueryBuilder jsonQueryBuilder = JsonQueryBuilder.getInstance();
        jsonQueryBuilder.addCriteria(criteria);
        jsonQueryBuilder.addOrderAsc(TodoWorkMetaData.planTime);
        jsonQueryBuilder.addOrderAsc(SC.lastModifiedDate);

        return queryAll(jsonQueryBuilder.toJsonQuerySpec());
    }

    @Override
    public Row countDoneTodoWorks(String timeType) {
        Long start = parseToStartDate(timeType);
        Long end = parseToEndDate(timeType);
        if (start == null && end == null) {
            throw new AppException("app.common.params.invalid");
        }
        Long total = _countDoneTodoWorks(start, end, TD.STATUS_DONE);
        Row row = new Row();
        row.put("total", total);
        row.put("result", true);
        return row;
    }

    private Long _countDoneTodoWorks(Long startDate, Long endDate, String status) {
        List<Object> params = new ArrayList<Object>();
        String cqlQueryString = " select count(t.id) as cnt from " + this.getBusinessObjectId() + " t "
                + " where (t.isDeleted is null or t.isDeleted = ?) and t.owner.id = ? ";
        params.add(false);
        params.add(EnterpriseContext.getCurrentUser().getUserLongId());
        if (StringUtils.isNotBlank(status)) {
            cqlQueryString += " and t.status = ? ";
            DynamicEnum dynamicEnum = boDataAccessManager.createDynamicEnumValue("todoStatus", status);
            params.add(dynamicEnum);
        }
        if (startDate != null && !startDate.equals(0L)) {
            cqlQueryString += " and t.lastModifiedDate > ? ";
            Timestamp start = new Timestamp(startDate);
            params.add(start);
        }
        if (endDate != null && !endDate.equals(0L)) {
            cqlQueryString += " and t.lastModifiedDate < ? ";
            Timestamp end = new Timestamp(endDate);
            params.add(end);
        }
        List<Map<String, Object>> list = runCQLQuery(cqlQueryString, params);
        if (list != null && list.size() > 0) {
            return (Long) list.get(0).get("cnt");
        }
        return 0l;
    }

    private Long parseToStartDate(String timeType) {
        Long start = null;
        switch (timeType) {
            case CT.COUNTTYPE_TODAY:
                start = DateUtil.getTodayStart().getTime();
                break;
            case CT.COUNTTYPE_LASTWEEK:
                start = DateUtil.getLastWeekStart().getTime();
                break;
            case CT.COUNTTYPE_WEEK:
                start = DateUtil.getCurrentWeekStart().getTime();
                break;
            case CT.COUNTTYPE_LASTMONTH:
                start = DateUtil.getLastMonthStart().getTime();
                break;
            case CT.COUNTTYPE_MONTH:
                start = DateUtil.getCurrentMonthStart().getTime();
                break;
        }
        return start;
    }

    private Long parseToEndDate(String timeType) {
        Long end = null;
        switch (timeType) {
            case CT.COUNTTYPE_TODAY:
                end = DateUtil.getTodayEnd().getTime();
                break;
            case CT.COUNTTYPE_LASTWEEK:
                end = DateUtil.getLastWeekEnd().getTime();
                break;
            case CT.COUNTTYPE_WEEK:
                end = DateUtil.getCurrentWeekEnd().getTime();
                break;
            case CT.COUNTTYPE_LASTMONTH:
                end = DateUtil.getLastMonthEnd().getTime();
                break;
            case CT.COUNTTYPE_MONTH:
                end = DateUtil.getCurrentMonthEnd().getTime();
                break;
            case CT.COUNTTYPE_MORE:
                end = DateUtil.getLastMonthStart().getTime();
                break;
        }
        return end;
    }

    private ITodoWorkRowSet findDoneTodoWorks(String status, Long startDate, Long endDate, Integer pageNo,
            Integer pageSize) {
        Criteria criteria = Criteria.AND();
        //criteria.addChild(Criteria.OR().empty(SC.isDeleted).eq(SC.isDeleted, false));
        criteria.eq(SC.owner, EnterpriseContext.getCurrentUser().getUserLongId());

        if (StringUtils.isNotBlank(status)) {
            // DynamicEnum dynamicEnum =
            // boDataAccessManager.createDynamicEnumValue("todoStatus", status);
            criteria.eq(TodoWorkMetaData.status, status);
        }
        if (startDate != null && !startDate.equals(0L)) {
            criteria.gt(SC.lastModifiedDate, new Timestamp(startDate));
        }
        if (endDate != null && !endDate.equals(0L)) {
            criteria.lt(SC.lastModifiedDate, new Timestamp(endDate));
        }
        JsonQueryBuilder jsonQueryBuilder = JsonQueryBuilder.getInstance();
        jsonQueryBuilder.addCriteria(criteria);
        jsonQueryBuilder.addOrderDesc(SC.lastModifiedDate);
        jsonQueryBuilder.addOrderDesc(SC.id);
        jsonQueryBuilder.setFirstResult((pageNo - 1) * pageSize);
        jsonQueryBuilder.setMaxResult(pageSize);

        return query(jsonQueryBuilder.toJsonQuerySpec());
    }

    @Override
    public RowSet getAllTodoWorks(Long startDate, Long endDate, String status) {
        checkParams(status);
        ITodoWorkRowSet todoWorks = findTodoWorks(startDate, endDate, status);
        return todoWorksToSet(todoWorks);
    }

    @SuppressWarnings("unchecked")
    private RowSet todoWorksToSet(ITodoWorkRowSet todoWorks) {
        RowSet set = new RowSet();
        set.setTotal(todoWorks.size());
        List<Long> customerIds = new ArrayList<Long>();
        for (int i = 0; i < todoWorks.size(); i++) {
            ITodoWorkRow todoWork = todoWorks.getRow(i);
            Long customerid = todoWork.getCustomer();
            if (null != customerid) {
                customerIds.add(customerid);
            }
        }
        List<ICustomerRow> customers = null;
        if (customerIds.size() > 0) {
            customers = ServiceLocator.getInstance().lookup(CustomerServiceItf.class).getCustomerByIds(customerIds);
        }
        Map<Long, Object> contactMap = new HashMap<Long, Object>();
        for (int i = 0; i < todoWorks.size(); i++) {
            ITodoWorkRow todoWork = todoWorks.getRow(i);
            Row row = BoRowConvertUtil.toRow(todoWork);
            Long customerId = todoWork.getCustomer();
            if (null != customerId) {
                Row customerRow = new Row();
                customerRow.put("id", customerId);
                ICustomerRow customer = ServiceLocator.getInstance().lookup(CustomerServiceItf.class)
                        .findByIdWithOutAuth(customerId);
                boolean myCustomer = true;// 是否我的客户
                if(customer!=null){
                    customerRow.put("name", customer.getName());
                    customerRow.put("phone", customer.getPhone());
                    if (null != customer.getOwner()) {
                        Long userId = AppWorkManager.getCurrAppUserId();
                        if (!userId.equals(customer.getOwner())) {
                            myCustomer = false;
                        }
                    }
                }
       
                boolean isDeleted = true;
                if (customers != null) {
                    for (ICustomerRow customerDb : customers) {
                        if (customerId.equals(customerDb.getId())) {
                            isDeleted = false;
                        }
                    }
                }
                customerRow.put("myCustomer", myCustomer);
                customerRow.put("isDeleted", isDeleted);
                if (contactMap.containsKey(customerId)) {
                    customerRow.put("contacts", contactMap.get(customerId));
                } else {
                    List<Map<String, Object>> contacts = new ArrayList<Map<String, Object>>();
                    contactMap.put(customerId, contacts);
                    customerRow.put("contacts", contacts);
                }
                row.put("customer", customerRow);
            }

            DynamicEnum statusEnum = todoWork.getStatus();
            if (null != statusEnum) {
                String status = statusEnum.getValue();
                if (!TD.STATUS_DONE.equals(status)) {
                    Timestamp planTime = todoWork.getPlanTime();
                    if (planTime != null) {
                        if (planTime.getTime() < new Date().getTime()) {
                            row.put("overDue", true);
                        } else {
                            row.put("overDue", false);
                        }
                    }
                }
            }
            set.add(row);
        }
        if (customerIds.size() > 0) {
            IContactRowSet contacts = ServiceLocator.getInstance().lookup(ContactServiceItf.class)
                    .findByCustomrIds(customerIds);
            if (contacts != null && contacts.size() > 0) {
                for (int i = 0; i < contacts.size(); i++) {
                    IContactRow contact = contacts.getRow(i);
                    Long customerId = contact.getCustomer();
                    List<Map<String, Object>> list = (List<Map<String, Object>>) contactMap.get(customerId);
                    if (null != list) {
                        Map<String, Object> map = new HashMap<String, Object>();
                        map.put("id", contact.getId());
                        map.put("name", contact.getName());
                        map.put("mobile", contact.getMobile());
                        map.put("email", contact.getEmail());
                        list.add(map);
                    }
                }
            }

        }
        return set;
    }

    private ITodoWorkRowSet findTodoWorks(Long startDate, Long endDate, String status) {
        Criteria criteria = Criteria.AND();
        //criteria.addChild(Criteria.OR().empty(SC.isDeleted).eq(SC.isDeleted, false));
        criteria.eq(SC.owner, EnterpriseContext.getCurrentUser().getUserLongId());
        criteria.addChild(Criteria.NOT().empty(TodoWorkMetaData.planTime));
        if (StringUtils.isNotBlank(status)) {
            criteria.eq(TodoWorkMetaData.status, status);
        }

        if (startDate != null && !startDate.equals(0L)) {
            criteria.gt(TodoWorkMetaData.planTime, new Timestamp(startDate).getTime());
        }
        if (endDate != null && !endDate.equals(0L)) {
            criteria.lt(TodoWorkMetaData.planTime, new Timestamp(endDate).getTime());
        }

        JsonQueryBuilder jsonQueryBuilder = JsonQueryBuilder.getInstance();
        jsonQueryBuilder.addCriteria(criteria);
        jsonQueryBuilder.addOrderDesc(TodoWorkMetaData.planTime);
        jsonQueryBuilder.addOrderDesc(SC.id);

        return queryAll(jsonQueryBuilder.toJsonQuerySpec());
    }
}
