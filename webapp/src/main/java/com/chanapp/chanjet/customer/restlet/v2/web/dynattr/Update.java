package com.chanapp.chanjet.customer.restlet.v2.web.dynattr;

import com.chanapp.chanjet.customer.service.metadata.FiledSave;
import com.chanapp.chanjet.customer.service.metadata.MetaDataServiceItf;
import com.chanapp.chanjet.web.restlet.BaseRestlet;
import com.chanapp.chanjet.web.service.ServiceLocator;

public class Update extends BaseRestlet {

    @Override
    public Object run() {
        String payload = this.getPayload();
        FiledSave field = dataManager.fromJSONString(payload, FiledSave.class);
        MetaDataServiceItf metaDataService = ServiceLocator.getInstance().lookup(MetaDataServiceItf.class);
        metaDataService.updateCustomField(field);
        return null;
    }

}
