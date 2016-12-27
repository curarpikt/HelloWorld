package com.chanapp.chanjet.customer.restlet.v2.web.customer;

import com.chanapp.chanjet.customer.businessobject.api.customer.ICustomerRow;
import com.chanapp.chanjet.customer.service.attachment.AttachmentServiceItf;
import com.chanapp.chanjet.customer.service.customer.CustomerServiceItf;
import com.chanapp.chanjet.customer.util.Assert;
import com.chanapp.chanjet.customer.util.BoRowConvertUtil;
import com.chanapp.chanjet.web.restlet.BaseRestlet;
import com.chanapp.chanjet.web.service.ServiceLocator;

/**
 * 根据客户ID查询附件
 * 
 * @author tds
 *
 */
public class Attachments extends BaseRestlet {
    @Override
    public Object run() {
        Long customerId = this.getId();
        Assert.notNull(customerId);

        ICustomerRow customer = ServiceLocator.getInstance().lookup(CustomerServiceItf.class)
                .findByIdWithAuth(customerId);
        Assert.notNull(customer, "app.customer.object.notexist");

        return BoRowConvertUtil.toRowList(
                ServiceLocator.getInstance().lookup(AttachmentServiceItf.class).findCustomerAttachments(customerId));
    }

}
