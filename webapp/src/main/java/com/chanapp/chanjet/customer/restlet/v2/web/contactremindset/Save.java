package com.chanapp.chanjet.customer.restlet.v2.web.contactremindset;

import java.util.LinkedHashMap;

import com.chanapp.chanjet.customer.service.contactremindset.ContactRemindSetServiceItf;
import com.chanapp.chanjet.customer.util.Assert;
import com.chanapp.chanjet.customer.vo.AppextResult;
import com.chanapp.chanjet.web.restlet.BaseRestlet;
import com.chanapp.chanjet.web.service.ServiceLocator;

/**
 * 联络提醒设置保存
 * 
 * @author tds
 *
 */
public class Save extends BaseRestlet {

    @Override
    public Object run() {

        String payload = this.getPayload();
        Assert.notNull(payload);

        LinkedHashMap<String, Object> param = (LinkedHashMap<String, Object>) dataManager.jsonStringToMap(payload);

        return new AppextResult(ServiceLocator.getInstance().lookup(ContactRemindSetServiceItf.class)
                .saveSets((String) param.get("sets")));
    }

}
