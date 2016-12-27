package com.chanapp.chanjet.customer.service.sync;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.chanapp.chanjet.customer.constant.OP;
import com.chanapp.chanjet.customer.constant.metadata.ContactMetaData;
import com.chanapp.chanjet.customer.constant.metadata.CustomerMetaData;
import com.chanapp.chanjet.customer.service.operationlog.OperationLogServiceItf;
import com.chanapp.chanjet.customer.util.Assert;
import com.chanapp.chanjet.web.constant.SC;
import com.chanapp.chanjet.web.service.ServiceLocator;
import com.chanjet.csp.bo.api.IBusinessObjectRow;
import com.chanjet.csp.bo.api.IBusinessObjectRowSet;
import com.chanjet.csp.ui.util.Criteria;
import com.chanjet.csp.ui.util.JsonQueryBuilder;

public class SyncOprationLog {

    private List<Long> tranCustomerIdList = new ArrayList<Long>();
    private Map<String, List<Long>> granteMap = new HashMap<String, List<Long>>();
    private Map<String, List<Long>> ungranteMap = new HashMap<String, List<Long>>();
    private List<Long> addCustomerIdsList = new ArrayList<Long>();
    private List<Long> addContactIdsList = new ArrayList<Long>();
    private Map<String, List<Long>> deleteMap = new HashMap<String, List<Long>>();

    public List<Long> getTranCustomerIdList() {
        return tranCustomerIdList;
    }

    public void setTranCustomerIdList(List<Long> tranCustomerIdList) {
        this.tranCustomerIdList = tranCustomerIdList;
    }

    public Map<String, List<Long>> getGranteMap() {
        return granteMap;
    }

    public void setGranteMap(Map<String, List<Long>> granteMap) {
        this.granteMap = granteMap;
    }

    public Map<String, List<Long>> getUngranteMap() {
        return ungranteMap;
    }

    public void setUngranteMap(Map<String, List<Long>> ungranteMap) {
        this.ungranteMap = ungranteMap;
    }

    public List<Long> getAddCustomerIdsList() {
        return addCustomerIdsList;
    }

    public void setAddCustomerIdsList(List<Long> addCustomerIdsList) {
        this.addCustomerIdsList = addCustomerIdsList;
    }

    public List<Long> getAddContactIdsList() {
        return addContactIdsList;
    }

    public void setAddContactIdsList(List<Long> addContactIdsList) {
        this.addContactIdsList = addContactIdsList;
    }

    public Map<String, List<Long>> getDeleteMap() {
        return deleteMap;
    }

    public void setDeleteMap(Map<String, List<Long>> deleteMap) {
        this.deleteMap = deleteMap;
    }

    public List<Long> getAddIdsByBOName(String entityName) {
        if (entityName.equals(CustomerMetaData.EOName)) {
            return addCustomerIdsList;
        }
        if (entityName.equals(ContactMetaData.EOName)) {
            return addContactIdsList;
        }
        return null;
    }

    public SyncOprationLog(Long syncVersion) {
        List<Map<String, Object>> logs = ServiceLocator.getInstance().lookup(OperationLogServiceItf.class)
                .findAllLastLog(syncVersion);
        for (Map<String, Object> log : logs) {
            // entityID, entityType , operateType,operator
            Long entityId = (Long) log.get("entityID");
            String entityType = (String) log.get("entityType");
            String operationType = (String) log.get("operateType");
            if (operationType.equals(OP.TRANS)) {
                tranCustomerIdList.add(entityId);
            } else if (operationType.equals(OP.GRANT)) {
                if (granteMap.containsKey(entityType)) {
                    List<Long> ids = granteMap.get(entityType);
                    ids.add(entityId);
                } else {
                    List<Long> ids = new ArrayList<Long>();
                    ids.add(entityId);
                    granteMap.put(entityType, ids);
                }
            } else if (operationType.equals(OP.UNGRANT)) {
                if (ungranteMap.containsKey(entityType)) {
                    List<Long> ids = ungranteMap.get(entityType);
                    ids.add(entityId);
                } else {
                    List<Long> ids = new ArrayList<Long>();
                    ids.add(entityId);
                    ungranteMap.put(entityType, ids);
                }
            } else if (operationType.equals(OP.DELETE)) {
                if (deleteMap.containsKey(entityType)) {
                    List<Long> ids = deleteMap.get(entityType);
                    ids.add(entityId);
                } else {
                    List<Long> ids = new ArrayList<Long>();
                    ids.add(entityId);
                    deleteMap.put(entityType, ids);
                }
            } else if (operationType.equals(OP.MUTIDELETE)) {
                continue;
            } else if (entityType.equals(CustomerMetaData.EOName)) {
                addCustomerIdsList.add(entityId);
            } else if (entityType.equals(ContactMetaData.EOName)) {
                addContactIdsList.add(entityId);
            }
        }
       // checkDelMap();
    }

/*    private List<Long> getRealDelIdsByIds(List<Long> ids, String boName) {
        List<Long> delIds = new ArrayList<Long>();
        Assert.notNull(ids, "app.common.params.invalid");
        Assert.notNull(boName, "app.common.params.invalid");
        JsonQueryBuilder jsonQueryBuilder = JsonQueryBuilder.getInstance();
        Criteria criteria = Criteria.AND();
       // criteria.eq(SC.isDeleted, true);
        criteria.in(SC.id, ids.toArray());
        jsonQueryBuilder.addCriteria(criteria);
        IBusinessObjectRowSet rows = ServiceLocator.getInstance().lookup(boName)
                .queryAll(jsonQueryBuilder.toJsonQuerySpec());
        for (IBusinessObjectRow row : rows.getRows()) {
            Long delId = (Long) row.getFieldValue(SC.id);
            delIds.add(delId);
        }
        return delIds;
    }*/

/*    private void checkDelMap() {
        for (Map.Entry<String, List<Long>> entry : deleteMap.entrySet()) {
            String entityName = entry.getKey();
            List<Long> ids = entry.getValue();
            List<Long> realDelIds = getRealDelIdsByIds(ids, entityName);
            entry.setValue(realDelIds);
        }
    }*/
}
