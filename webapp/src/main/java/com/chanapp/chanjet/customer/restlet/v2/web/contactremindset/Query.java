package com.chanapp.chanjet.customer.restlet.v2.web.contactremindset;

import com.chanapp.chanjet.customer.service.contactremindset.ContactRemindSetServiceItf;
import com.chanapp.chanjet.customer.util.Assert;
import com.chanapp.chanjet.customer.vo.AppextResult;
import com.chanapp.chanjet.web.restlet.BaseRestlet;
import com.chanapp.chanjet.web.service.ServiceLocator;

/**
 * 获取联络提醒设置数据
 * 
 * @author tds
 *
 */
public class Query extends BaseRestlet {

    @Override
    public Object run() {
        Long modifyTime = this.getParamAsLong("modifyTime");
        Assert.notNull(modifyTime);

        return new AppextResult(
                ServiceLocator.getInstance().lookup(ContactRemindSetServiceItf.class).queryByModifyTime(modifyTime));
    }

}
