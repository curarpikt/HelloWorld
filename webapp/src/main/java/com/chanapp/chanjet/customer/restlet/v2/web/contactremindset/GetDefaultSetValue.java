package com.chanapp.chanjet.customer.restlet.v2.web.contactremindset;

import com.chanapp.chanjet.customer.service.contactremindset.ContactRemindSetServiceItf;
import com.chanapp.chanjet.customer.vo.AppextResult;
import com.chanapp.chanjet.web.restlet.BaseRestlet;
import com.chanapp.chanjet.web.service.ServiceLocator;

/**
 * 获取联络提醒设置默认值
 * 
 * @author tds
 *
 */
public class GetDefaultSetValue extends BaseRestlet {

    @Override
    public Object run() {
        return new AppextResult(
                ServiceLocator.getInstance().lookup(ContactRemindSetServiceItf.class).getDefaultSetValue());
    }

}
