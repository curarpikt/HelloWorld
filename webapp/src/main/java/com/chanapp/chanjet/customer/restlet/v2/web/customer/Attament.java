package com.chanapp.chanjet.customer.restlet.v2.web.customer;

import java.util.HashMap;
import java.util.Map;

import com.chanapp.chanjet.customer.businessobject.api.attachment.IAttachmentRow;
import com.chanapp.chanjet.customer.service.attachment.AttachmentServiceItf;
import com.chanapp.chanjet.customer.util.Assert;
import com.chanapp.chanjet.web.restlet.BaseRestlet;
import com.chanapp.chanjet.web.service.ServiceLocator;
import com.chanjet.csp.ccs.api.common.EnterpriseContext;
import com.chanjet.csp.common.base.exception.AppException;

/**
 * 删除附件
 * 
 * @author tds
 *
 */
public class Attament extends BaseRestlet {
    @Override
    public Object run() {
        Long attachmentId = this.getId();
        Assert.notNull(attachmentId);

        Map<String, Object> reslut = new HashMap<String, Object>();
        reslut.put("result", false);

        AttachmentServiceItf attachmentService = ServiceLocator.getInstance().lookup(AttachmentServiceItf.class);

        IAttachmentRow attvo = attachmentService.findByIdWithAuth(attachmentId);
        Assert.notNull(attvo, "app.attachment.object.notexist");
        Long userId = EnterpriseContext.getCurrentUser().getUserLongId();
        Long ownerId = attvo.getOwner();
        if (!userId.equals(ownerId)) {
            throw new AppException("app.delete.Attachment.notOwner");
        }
        attachmentService.delete(attachmentId);

        reslut.put("result", true);
        return reslut;
    }

}
