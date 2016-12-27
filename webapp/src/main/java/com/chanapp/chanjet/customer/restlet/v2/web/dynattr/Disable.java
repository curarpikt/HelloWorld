package com.chanapp.chanjet.customer.restlet.v2.web.dynattr;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.chanapp.chanjet.customer.cache.MetadataCacheBuilder;
import com.chanapp.chanjet.customer.cache.VersionInfo;
import com.chanapp.chanjet.customer.constant.SRU;
import com.chanapp.chanjet.customer.layout.LayoutManager;
import com.chanapp.chanjet.web.restlet.BaseRestlet;
import com.chanapp.chanjet.web.service.ServiceLocator;

public class Disable extends BaseRestlet {
    @SuppressWarnings("unchecked")
    @Override
    public Object run() {
        Map<String, String> reslut = new HashMap<String, String>();
        reslut.put("result", "false");
        String payload = this.getPayload();
        Map<String, Object> paraMap = dataManager.jsonStringToMap(payload);
      //  FieldDataServiceItf fieldDataService = ServiceLocator.getInstance().lookup(FieldDataServiceItf.class);
        for (Map.Entry<String, Object> entry : paraMap.entrySet()) {
            String tableName = entry.getKey();
            List<String> fields = (List<String>) entry.getValue();
            LayoutManager.changeFieldStatus(tableName, fields, SRU.FIELD_STATUS_DISABLE);
        }
        MetadataCacheBuilder.newBuilder().buildCache().clear();
        VersionInfo.getInstance().setLastModifiedDate(new Date());
        reslut.put("result", "true");
        return reslut;
    }
}
