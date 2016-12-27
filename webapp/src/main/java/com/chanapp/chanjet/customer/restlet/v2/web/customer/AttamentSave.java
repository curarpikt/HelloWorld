package com.chanapp.chanjet.customer.restlet.v2.web.customer;

import java.util.Map;

import com.chanapp.chanjet.customer.service.attachment.AttachmentServiceItf;
import com.chanapp.chanjet.customer.util.Assert;
import com.chanapp.chanjet.web.restlet.BaseRestlet;
import com.chanapp.chanjet.web.service.ServiceLocator;
import com.chanjet.csp.common.base.exception.AppException;

/**
 * 保存客户附件
 * 
 * @author tds
 *
 */
public class AttamentSave extends BaseRestlet {

    @Override
    public Object run() {
        Long customerId = this.getId();
        Assert.notNull(customerId);
        try {
            String payload = this.getPayload();
            Map<String, Object> attachment = dataManager.jsonStringToMap(payload);
            ServiceLocator.getInstance().lookup(AttachmentServiceItf.class).uploadAttament(customerId, attachment);
            return payload;
        } catch (Exception e) {
            throw new AppException("app.common.server.error");
        }
    }

}