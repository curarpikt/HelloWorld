package com.chanapp.chanjet.customer.restlet.v2.web.importNew.records;

import com.chanapp.chanjet.customer.service.importrecordnew.ImportRecordNewServiceItf;
import com.chanapp.chanjet.web.restlet.BaseRestlet;
import com.chanapp.chanjet.web.service.ServiceLocator;

/**
 * 清除所有导入记录
 * 
 * @author tds
 *
 */
public class Clean extends BaseRestlet {
    @Override
    public Object run() {
        return ServiceLocator.getInstance().lookup(ImportRecordNewServiceItf.class).recordsClean();
    }

}
