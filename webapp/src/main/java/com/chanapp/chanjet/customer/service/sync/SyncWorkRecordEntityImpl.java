package com.chanapp.chanjet.customer.service.sync;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.chanapp.chanjet.customer.constant.VF;
import com.chanapp.chanjet.customer.constant.metadata.CustomerMetaData;
import com.chanapp.chanjet.customer.constant.metadata.WorkRecordMetaData;
import com.chanapp.chanjet.customer.service.privilege.PrivilegeServiceItf;
import com.chanapp.chanjet.customer.service.workrecord.WorkRecordServiceItf;
import com.chanapp.chanjet.web.constant.SC;
import com.chanapp.chanjet.web.service.ServiceLocator;
import com.chanjet.csp.bo.api.IBusinessObjectRow;
import com.chanjet.csp.ccs.api.common.EnterpriseContext;

public class SyncWorkRecordEntityImpl extends BaseSyncEntity {
    private List<Long> parentIds;
    private List<Long> hierarchyOwners;
    private Long syncVersion;
    private SyncOprationLog log;

    public SyncWorkRecordEntityImpl(List<Long> hierarchyOwners, Long syncVersion, SyncOprationLog log,
            List<Long> parentIds) {
        this.entityName = WorkRecordMetaData.EOName;
        this.boHome = ServiceLocator.getInstance().lookup(WorkRecordServiceItf.class).getBusinessObjectHome();
        this.parentIds = parentIds;
        this.hierarchyOwners = hierarchyOwners;
        this.syncVersion = syncVersion;
        this.log = log;
        // 初始化下发的字段
        filedName = getFieldsName(entityName);
        filedName.add(VF.privilege);
        filedName.add(VF.customerName);
        initPara();
    }

    @Override
    public void setAll() {
        this.isAll = true;
        entitySet= ServiceLocator.getInstance().lookup(WorkRecordServiceItf.class).getAllWorkReocrdSet();
        if (entitySet != null) {
            for (IBusinessObjectRow row : entitySet) {
                Long id = (Long) row.getFieldValue(SC.id);
                this.addIds.add(id);
            }
        }
    }

    @Override
    public List<Map<String, Object>> getEntityData() {

        List<Map<String, Object>> workRecordItems = super.getEntityData();
        if (workRecordItems == null || workRecordItems.size() == 0) {
            return workRecordItems;
        }
        Map<Long, String> customerMap = getCustomerName();

        for (Map<String, Object> row : workRecordItems) {
            // 查询客户名称
            if (row.get(WorkRecordMetaData.customer) != null) {
                Long customerId = new Long(row.get(WorkRecordMetaData.customer).toString());
                row.put(VF.customerName, customerMap.get(customerId));
            } else {
                row.put(VF.customerName, "");
            }
        }
        return workRecordItems;
    }

    private Map<Long, String> getCustomerName() {
        Map<Long, String> customerNameMap = new HashMap<Long, String>();
        List<IBusinessObjectRow> rows = entitySet;
        for (IBusinessObjectRow row : rows) {
            Map<?, ?> customerMap = (Map<?, ?>) row.getFieldValue(WorkRecordMetaData.customer);
            if (customerMap != null && customerMap.get(SC.id) != null
                    && customerMap.get(CustomerMetaData.name) != null) {
                customerNameMap.put((Long) customerMap.get(SC.id), customerMap.get(CustomerMetaData.name).toString());
            }

        }
        return customerNameMap;
    }

    public void initPara() {

        if (syncVersion == 0) {
            setAll();
        } else {
            List<Long> grantList = log.getGranteMap().get(entityName);
            Map<String, List<Long>> delMap = log.getDeleteMap();
            List<Long> delCustomers = delMap.get(entityName);
            List<Long> hasPriUsers = ServiceLocator.getInstance().lookup(PrivilegeServiceItf.class)
                    .checkSelectDataAuthList(entityName, grantList, EnterpriseContext.getCurrentUser().getUserLongId());
            if (grantList != null) {
                for (Long userId : grantList) {
                    // 有权限当做新增
                    if (hasPriUsers != null && hasPriUsers.contains(userId)) {
                        // 如果已经被删除
                        if (delCustomers != null && delCustomers.contains(userId)) {
                            continue;
                        }
                        sharedIds.add(userId);
                        addIds.add(userId);
                    }
                    // 无权限当做删除
                    else {
                        delIds.add(userId);
                    }
                }
            }

            // 移交或共享的客户下的工作记录
            addByParentEntity();
            // 通用逻辑
            setIdList(hierarchyOwners, log);

            // 工作记录新增，修改不记日志，按时间戳下发新增
            addByTs(syncVersion, SC.lastModifiedBy, "desc");

        }

    }

    private void addByParentEntity() {
        List<Long> ids = ServiceLocator.getInstance().lookup(WorkRecordServiceItf.class)
                .getIdListByCustomerIdList(parentIds);
        if (ids != null) {
            sharedIds.addAll(ids);
            addIds.addAll(ids);
        }

    }

}
