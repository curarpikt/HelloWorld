package com.chanapp.chanjet.customer.restlet.v2.web.importNew.record;

import com.chanapp.chanjet.customer.service.importrecordnew.ImportRecordNewServiceItf;
import com.chanapp.chanjet.web.restlet.BaseRestlet;
import com.chanapp.chanjet.web.service.ServiceLocator;

/**
 * 获取头信息
 * 
 * @author tds
 *
 */
public class Head extends BaseRestlet {
    @Override
    public Object run() {
        Long id = this.getId();
        Long sheetType = this.getParamAsLong("sheetType");

        return ServiceLocator.getInstance().lookup(ImportRecordNewServiceItf.class).getRecordHead(id, sheetType);
    }

}
