package com.chanapp.chanjet.customer.restlet.v2.web.importNew.record.detail;

import com.chanapp.chanjet.customer.service.importrecordnew.ImportRecordNewServiceItf;
import com.chanapp.chanjet.web.restlet.BaseRestlet;
import com.chanapp.chanjet.web.service.ServiceLocator;

/**
 * 删除单条导入详情记录
 * 
 * @author tds
 *
 */
public class Remove extends BaseRestlet {
    @Override
    public Object run() {
        Long id = this.getId();

        return ServiceLocator.getInstance().lookup(ImportRecordNewServiceItf.class).recordDetailRemove(id);
    }

}
