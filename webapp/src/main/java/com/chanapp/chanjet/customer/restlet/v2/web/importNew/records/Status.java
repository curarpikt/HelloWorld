package com.chanapp.chanjet.customer.restlet.v2.web.importNew.records;

import com.chanapp.chanjet.customer.service.importrecordnew.ImportRecordNewServiceItf;
import com.chanapp.chanjet.web.restlet.BaseRestlet;
import com.chanapp.chanjet.web.service.ServiceLocator;

/**
 * 获得所有导入记录状态
 * 
 * @author tds
 *
 */
public class Status extends BaseRestlet {
    @Override
    public Object run() {
        return ServiceLocator.getInstance().lookup(ImportRecordNewServiceItf.class).recordsStatus(this.getParam("ids"));
    }

}
