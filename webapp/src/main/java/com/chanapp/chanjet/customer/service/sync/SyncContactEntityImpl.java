package com.chanapp.chanjet.customer.service.sync;

import java.util.List;

import com.chanapp.chanjet.customer.constant.VF;
import com.chanapp.chanjet.customer.constant.metadata.ContactMetaData;
import com.chanapp.chanjet.customer.service.contact.ContactServiceItf;
import com.chanapp.chanjet.web.service.ServiceLocator;

public class SyncContactEntityImpl extends BaseSyncEntity {
    private List<Long> parentIds;
    private List<Long> hierarchyOwners;
    private Long syncVersion;
    private SyncOprationLog log;

    public SyncContactEntityImpl(List<Long> hierarchyOwners, Long syncVersion, final SyncOprationLog log,
            List<Long> parentIds) {
        this.parentIds = parentIds;
        this.entityName = ContactMetaData.EOName;
        this.boHome = ServiceLocator.getInstance().lookup(ContactServiceItf.class).getBusinessObjectHome();
        this.hierarchyOwners = hierarchyOwners;
        this.syncVersion = syncVersion;
        this.log = log;
        filedName = getFieldsName(entityName);
        filedName.add(VF.privilege);
        initPara();
    }

    @Override
    public void initPara() {
        if (syncVersion == 0) {
            setAll();
        } else {
            // 移交或共享的客户下的联系人
            addByParentEntity();
            // 通用逻辑
            setIdList(hierarchyOwners, log);
        }
    }

    public void addByParentEntity() {
        List<Long> ids = ServiceLocator.getInstance().lookup(ContactServiceItf.class)
                .getIdListByCustomerIdList(parentIds);
        if (ids != null)
            addIds.addAll(ids);
    }
}
