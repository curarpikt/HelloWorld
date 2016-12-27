package com.chanapp.chanjet.customer.restlet.v2.web.importNew.record;

import com.chanapp.chanjet.customer.service.importrecordnew.ImportRecordNewServiceItf;
import com.chanapp.chanjet.web.restlet.BaseRestlet;
import com.chanapp.chanjet.web.service.ServiceLocator;

/**
 * 清除单条导入记录
 * 
 * @author tds
 *
 */
public class Remove extends BaseRestlet {
    @Override
    public Object run() {
        Long id = this.getId();

        return ServiceLocator.getInstance().lookup(ImportRecordNewServiceItf.class).recordRemove(id);
    }

}
