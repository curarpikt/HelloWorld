package com.chanapp.chanjet.customer.service.sync;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.chanapp.chanjet.customer.constant.metadata.CheckinMetaData;
import com.chanapp.chanjet.customer.constant.metadata.CommentMetaData;
import com.chanapp.chanjet.customer.constant.metadata.WorkRecordMetaData;

public class SyncQueryDataImpl implements ISyncQueryData {
    private Long syncVersion;
    private List<Long> hierarchyOwners;
    private List<ISyncEntity> entityDate;

    public List<ISyncEntity> getEntityDate() {
        return entityDate;
    }

    private void setEntityDate(List<ISyncEntity> entityDate) {
        this.entityDate = entityDate;
    }

    private static final Log logger = LogFactory.getLog(SyncQueryDataImpl.class);

    public SyncQueryDataImpl(Long syncVersion, List<Long> hierarchyOwners) {
        this.syncVersion = syncVersion;
        this.hierarchyOwners = hierarchyOwners;
        initEntitys();
    }

    @Override
    public void initEntitys() {
        SyncOprationLog log = new SyncOprationLog(syncVersion);
        List<ISyncEntity> entitys = new ArrayList<ISyncEntity>();
        // 同步客户
        Long start = System.currentTimeMillis();
        SyncCustomerEntityImpl customerSync = new SyncCustomerEntityImpl(hierarchyOwners, syncVersion, log);
        entitys.add(customerSync);
        logger.info("loadCustomerData use time = " + (System.currentTimeMillis() - start) + " ms ,ADDCount:"
                + customerSync.getAddIds().size());
        start = System.currentTimeMillis();
        // 同步工作记录，依赖客户
        SyncWorkRecordEntityImpl workRecordSync = new SyncWorkRecordEntityImpl(hierarchyOwners, syncVersion, log,
                customerSync.getSharedIds());
        entitys.add(workRecordSync);
        logger.info("loadWorkRecordData use time = " + (System.currentTimeMillis() - start) + " ms,ADDCount:"
                + workRecordSync.getAddIds().size());
        start = System.currentTimeMillis();
        // 同步联系人，依赖客户
        SyncContactEntityImpl contactSync = new SyncContactEntityImpl(hierarchyOwners, syncVersion, log,
                customerSync.getSharedIds());
        entitys.add(contactSync);
        logger.info("loadContactData use time = " + (System.currentTimeMillis() - start) + " ms,ADDCount:"
                + contactSync.getAddIds().size());
        start = System.currentTimeMillis();
        // 同步评论,依赖工作记录
        SyncCommentEntityImpl commetSync = new SyncCommentEntityImpl(hierarchyOwners, syncVersion, log,
                workRecordSync.getSharedIds());
        entitys.add(commetSync);
        logger.info("loadCommetData use time = " + (System.currentTimeMillis() - start) + " ms,ADDCount:"
                + commetSync.getAddIds().size());
        start = System.currentTimeMillis();
        // 同步待办
        SyncTodoEntityImpl todoSync = new SyncTodoEntityImpl(hierarchyOwners, syncVersion, log);
        entitys.add(todoSync);
        logger.info("loadTodoWorkData use time = " + (System.currentTimeMillis() - start) + " ms,ADDCount:"
                + todoSync.getAddIds().size());
        start = System.currentTimeMillis();
        // 同步签到
        SyncCheckInEntityImpl checkInSync = new SyncCheckInEntityImpl(hierarchyOwners, syncVersion, log);
        logger.info("loadCheckInData use time = " + (System.currentTimeMillis() - start) + " ms,ADDCount:"
                + checkInSync.getAddIds().size());

        entitys.add(checkInSync);

        // 同步附件，依赖工作记录和评论
        Map<String, List<Long>> attachMap = new HashMap<String, List<Long>>();
        attachMap.put(WorkRecordMetaData.EOName, workRecordSync.getAddIds());
        attachMap.put(CommentMetaData.EOName, commetSync.getAddIds());
        attachMap.put(CheckinMetaData.EOName, checkInSync.getAddIds());
        SyncAttachmentEntityImpl attachmentSync = new SyncAttachmentEntityImpl(attachMap);
        entitys.add(attachmentSync);
        // 同步用户
        SyncUserEntityImpl userSync = new SyncUserEntityImpl(syncVersion);
        entitys.add(userSync);
        setEntityDate(entitys);
    }

    @Override
    public List<ISyncEntity> getSyncEntityList() {
        return getEntityDate();
    }

}
