package com.chanapp.chanjet.customer.restlet.v2.web.importNew.record;

import com.chanapp.chanjet.customer.service.importrecordnew.ImportRecordNewServiceItf;
import com.chanapp.chanjet.web.restlet.BaseRestlet;
import com.chanapp.chanjet.web.service.ServiceLocator;

/**
 * 查询单条导入记录下的关联记录明细
 * 
 * @author tds
 *
 */
public class Details extends BaseRestlet {
    @Override
    public Object run() {
        Long parentId = this.getParamAsLong("parentId");
        Long sheetType = this.getParamAsLong("sheetType");
        Long pageno = this.getParamAsLong("pageno");
        Long pagesize = this.getParamAsLong("pagesize");

        return ServiceLocator.getInstance().lookup(ImportRecordNewServiceItf.class).recordDetail(parentId, sheetType,
                pageno, pagesize);
    }

}
