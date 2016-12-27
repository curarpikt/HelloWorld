package com.chanapp.chanjet.customer.restlet.v2.rest.contact;

import java.util.Map;

import com.chanapp.chanjet.customer.service.contact.ContactServiceItf;
import com.chanapp.chanjet.customer.util.Assert;
import com.chanapp.chanjet.web.restlet.BaseRestlet;
import com.chanapp.chanjet.web.service.ServiceLocator;
import com.chanjet.csp.common.base.util.StringUtils;

/**
 * @author tds
 *
 */
public class GetContactsByMobileAndPhone extends BaseRestlet {
    @Override
    public Object run() {
        String mobile = this.getParam("mobile");
        String phone = this.getParam("phone");

        String phoneNum = "";
        if (StringUtils.isNotEmpty(mobile)) {
            phoneNum += mobile;
        }
        if (StringUtils.isNotEmpty(phone)) {
            phoneNum += phone;
        }
        Assert.hasLength(phoneNum, "app.contact.phone.invalid");
        Map<String, Object> contacts = ServiceLocator.getInstance().lookup(ContactServiceItf.class)
                .getContactsByMobileAndPhone(mobile, phone);
        return contacts;
    }

}