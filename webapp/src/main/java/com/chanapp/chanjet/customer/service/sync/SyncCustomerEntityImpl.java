package com.chanapp.chanjet.customer.service.sync;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.chanapp.chanjet.customer.constant.VF;
import com.chanapp.chanjet.customer.constant.metadata.CustomerMetaData;
import com.chanapp.chanjet.customer.service.customer.CustomerServiceItf;
import com.chanapp.chanjet.customer.service.privilege.PrivilegeServiceItf;
import com.chanapp.chanjet.customer.service.workrecord.WorkRecordServiceItf;
import com.chanapp.chanjet.web.constant.SC;
import com.chanapp.chanjet.web.service.ServiceLocator;
import com.chanjet.csp.ccs.api.common.EnterpriseContext;

public class SyncCustomerEntityImpl extends BaseSyncEntity {

    private List<Long> hierarchyOwners;
    private Long syncVersion;
    private SyncOprationLog log;

    public SyncCustomerEntityImpl(List<Long> hierarchyOwners, Long syncVersion, final SyncOprationLog log) {
        this.entityName = CustomerMetaData.EOName;
        this.boHome = ServiceLocator.getInstance().lookup(CustomerServiceItf.class).getBusinessObjectHome();
        this.hierarchyOwners = hierarchyOwners;
        this.syncVersion = syncVersion;
        this.log = log;
        // 初始化下发的字段
        filedName = getFieldsName(entityName);
        filedName.add(VF.privilege);
        filedName.add(CustomerMetaData.grantIds);
        initPara();
    }

    /*
     * 加入参与人逻辑
     */
    @Override
    public List<Map<String, Object>> getEntityData() {
        List<Map<String, Object>> customerItems = super.getEntityData();
        if (customerItems == null || customerItems.size() == 0) {
            return customerItems;
        }
        // 加入参与人
        Map<Long, Set<Long>> grants = getCustomerRefGrants();
        for (Map<String, Object> row : customerItems) {
            Long customerId = (Long) row.get(SC.id);
            Set<Long> grantIds = grants.get(customerId);
            row.put(CustomerMetaData.grantIds, grantIds);
        }
        return customerItems;
    }

    public void initPara() {
        if (syncVersion == 0) {
            setAll();
        } else {
            List<Long> grantList = log.getGranteMap().get(entityName);
            List<Long> unGrantList = log.getUngranteMap().get(entityName);
            List<Long> tranlist = log.getTranCustomerIdList();
            Map<String, List<Long>> delMap = log.getDeleteMap();
            List<Long> delCustomers = delMap.get(entityName);
            List<Long> checkIds = new ArrayList<Long>();
            if (grantList != null)
                checkIds.addAll(grantList);
            if (tranlist != null)
                checkIds.addAll(tranlist);
            if (unGrantList != null)
                checkIds.addAll(unGrantList);
            List<Long> hasPriUsers = ServiceLocator.getInstance().lookup(PrivilegeServiceItf.class)
                    .checkSelectDataAuthList(entityName, checkIds, EnterpriseContext.getCurrentUser().getUserLongId());
            for (Long userId : checkIds) {
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
            // 通用逻辑
            setIdList(hierarchyOwners, log);
            if (delIds != null && delIds.size() > 0) {
                List<Long> delCustomerIds = new ArrayList<Long>(delIds);
                Map<Long, List<Long>> nodeleteIds = ServiceLocator.getInstance().lookup(WorkRecordServiceItf.class)
                        .getIdMapByCustomer(delCustomerIds);
                this.noDeleteIds = nodeleteIds;
            }
        }
    }

    private Map<Long, Set<Long>> getCustomerRefGrants() {
        List<Long> customerIds = new ArrayList<Long>();
        customerIds.addAll(addIds);
        return ServiceLocator.getInstance().lookup(CustomerServiceItf.class).customersRefGrants(customerIds);
    }

}
