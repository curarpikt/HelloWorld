package com.chanapp.chanjet.customer.expandauth;

import com.chanapp.chanjet.customer.service.dataauth.DataAuthServiceItf;
import com.chanapp.chanjet.web.service.ServiceLocator;
import com.chanjet.csp.enterprise.ext.ExpandAuthorization;

public class DataAuthWithExpandAuth implements ExpandAuthorization {

    @Override
    public void process(Object[] args) {
        Long userId = null;
        String dataPrivi = null;
        if (args != null) {
            userId = (Long) args[0];
            dataPrivi = (String) args[1];
        }
        ServiceLocator.getInstance().lookup(DataAuthServiceItf.class).checkAndUpdateDataAuth(userId, dataPrivi);
    }

}
