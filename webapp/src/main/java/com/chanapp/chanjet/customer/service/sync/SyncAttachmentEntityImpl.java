package com.chanapp.chanjet.customer.service.sync;

import java.util.List;
import java.util.Map;
import com.chanapp.chanjet.customer.constant.VF;
import com.chanapp.chanjet.customer.constant.metadata.AttachmentMetaData;
import com.chanapp.chanjet.customer.service.attachment.AttachmentServiceItf;
import com.chanapp.chanjet.web.service.ServiceLocator;

public class SyncAttachmentEntityImpl extends BaseSyncEntity {
    private Map<String, List<Long>> reletaMap;

    public SyncAttachmentEntityImpl(Map<String, List<Long>> reletaMap) {
        this.reletaMap = reletaMap;
        this.entityName = AttachmentMetaData.EOName;
        filedName = getFieldsName(entityName);
        this.boHome = ServiceLocator.getInstance().lookup(AttachmentServiceItf.class).getBusinessObjectHome();
        filedName.add(VF.privilege);
        initPara();
    }

    @Override
    public void initPara() {
        for (Map.Entry<String, List<Long>> entry : reletaMap.entrySet()) {
            String relateToType = entry.getKey();
            List<Long> relateToIds = entry.getValue();
            List<Long> addIds = ServiceLocator.getInstance().lookup(AttachmentServiceItf.class)
                    .findAttachmentIdsByRelate(relateToType, relateToIds);
            if (addIds != null) {
                this.addIds.addAll(addIds);
            }
        }
        // 附件同步，没有删除逻辑。端上根据附件所属对象操作。

    }

}
