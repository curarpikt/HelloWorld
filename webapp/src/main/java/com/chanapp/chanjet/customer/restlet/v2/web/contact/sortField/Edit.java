package com.chanapp.chanjet.customer.restlet.v2.web.contact.sortField;

import java.util.List;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.chanapp.chanjet.customer.cache.CustomerMetaData;
import com.chanapp.chanjet.customer.cache.MetadataCacheBuilder;
import com.chanapp.chanjet.customer.cache.VersionInfo;
import com.chanapp.chanjet.customer.constant.EO;
import com.chanapp.chanjet.customer.constant.FT;
import com.chanapp.chanjet.customer.layout.LayoutManager;
import com.chanapp.chanjet.web.restlet.BaseRestlet;
import com.chanapp.chanjet.web.service.ServiceLocator;
import com.chanjet.csp.common.base.exception.AppException;

/**
 * 联系人字段排序：type 目前只有edit，没有view。
 * 
 * @author tds
 *
 */
public class Edit extends BaseRestlet {

    @Override
    public Object run() {
        String payload = this.getPayload();
        @SuppressWarnings("unchecked")
        List<String> sortList = dataManager.fromJSONString(payload, List.class);

        if (sortList == null || sortList.size() == 0) {
            throw new AppException("app.layout.sort.paraerror");
        }
        LayoutManager.sortFields(sortList, EO.Contact, FT.VIEW);
        MetadataCacheBuilder.newBuilder().buildCache().clear();
        VersionInfo.getInstance().setLastModifiedDate(new Date());
        Map<String, String> reslut = new HashMap<String, String>();
        reslut.put("result", "true");
        return reslut;
    }

}
