package com.chanapp.chanjet.customer.service.sync;

import java.util.List;

import com.chanapp.chanjet.customer.constant.VF;
import com.chanapp.chanjet.customer.constant.metadata.TodoWorkMetaData;
import com.chanapp.chanjet.customer.service.todowork.TodoWorkServiceItf;
import com.chanapp.chanjet.web.constant.SC;
import com.chanapp.chanjet.web.service.ServiceLocator;

public class SyncTodoEntityImpl extends BaseSyncEntity {

    private List<Long> hierarchyOwners;
    private Long syncVersion;
    private SyncOprationLog log;

    public SyncTodoEntityImpl(List<Long> hierarchyOwners, Long syncVersion, SyncOprationLog log) {
        this.entityName = TodoWorkMetaData.EOName;
        this.boHome = ServiceLocator.getInstance().lookup(TodoWorkServiceItf.class).getBusinessObjectHome();
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
            // 通用逻辑
            setIdList(hierarchyOwners, log);
            // 代办新增，修改不记日志，按时间戳下发新增
            addByTs(syncVersion, SC.lastModifiedBy, "desc");
        }

    }

}
