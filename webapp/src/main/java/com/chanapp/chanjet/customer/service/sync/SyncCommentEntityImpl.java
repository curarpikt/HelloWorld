package com.chanapp.chanjet.customer.service.sync;

import java.util.List;

import com.chanapp.chanjet.customer.constant.VF;
import com.chanapp.chanjet.customer.constant.metadata.CommentMetaData;
import com.chanapp.chanjet.customer.service.comment.CommentServiceItf;
import com.chanapp.chanjet.web.constant.SC;
import com.chanapp.chanjet.web.service.ServiceLocator;

public class SyncCommentEntityImpl extends BaseSyncEntity {
    private List<Long> parentIds;
    private List<Long> hierarchyOwners;
    private Long syncVersion;
    private SyncOprationLog log;

    public SyncCommentEntityImpl(List<Long> hierarchyOwners, Long syncVersion, SyncOprationLog log,
            List<Long> parentIds) {
        this.entityName = CommentMetaData.EOName;
        this.boHome = ServiceLocator.getInstance().lookup(CommentServiceItf.class).getBusinessObjectHome();
        this.parentIds = parentIds;
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
            // 移交或共享的客户下的工作记录
            addByParentEntity();
            // 通用逻辑
            setIdList(hierarchyOwners, log);
            // 工作记录新增，修改不记日志，按时间戳下发新增
            addByTs(syncVersion, SC.lastModifiedBy, "desc");
        }
    }

    private void addByParentEntity() {
        List<Long> ids = ServiceLocator.getInstance().lookup(CommentServiceItf.class)
                .getIdListByWorkRecordIdList(parentIds);
        if (ids != null)
            addIds.addAll(ids);
    }

}
