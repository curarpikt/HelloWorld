package com.chanapp.chanjet.customer.restlet.v2.web.importNew;

import com.chanapp.chanjet.customer.service.importrecordnew.ImportRecordNewServiceItf;
import com.chanapp.chanjet.web.restlet.BaseRestlet;
import com.chanapp.chanjet.web.service.ServiceLocator;

/**
 * 重新导入一条文本记录
 * 
 * @author tds
 *
 */
public class Text extends BaseRestlet {
    @Override
    public Object run() {
        return ServiceLocator.getInstance().lookup(ImportRecordNewServiceItf.class).text(this.getPayload(),
                this.getParamAsLong("sheetType"));
    }

}
