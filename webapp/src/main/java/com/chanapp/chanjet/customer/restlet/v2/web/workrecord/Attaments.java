package com.chanapp.chanjet.customer.restlet.v2.web.workrecord;

import com.chanapp.chanjet.customer.businessobject.api.attachment.IAttachmentRowSet;
import com.chanapp.chanjet.customer.service.attachment.AttachmentServiceItf;
import com.chanapp.chanjet.customer.util.Assert;
import com.chanapp.chanjet.customer.util.BoRowConvertUtil;
import com.chanapp.chanjet.customer.vo.RowSet;
import com.chanapp.chanjet.web.restlet.BaseRestlet;
import com.chanapp.chanjet.web.service.ServiceLocator;

/**
 * 根据工作记录ID返回附件
 * 
 * @author tds
 *
 */
public class Attaments extends BaseRestlet {
    @Override
    public Object run() {
        Long workRecordId = this.getId();
        Assert.notNull(workRecordId);

        IAttachmentRowSet attachments = ServiceLocator.getInstance().lookup(AttachmentServiceItf.class)
                .findWRAttachmentsSet(workRecordId);
        RowSet rowset = BoRowConvertUtil.toRowSet(attachments);
        rowset.setTotal(rowset.getItems().size());
        return rowset;
    }

}