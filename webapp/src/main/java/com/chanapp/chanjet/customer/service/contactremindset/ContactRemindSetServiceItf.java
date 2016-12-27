package com.chanapp.chanjet.customer.service.contactremindset;

import java.util.Map;

import com.chanapp.chanjet.customer.businessobject.api.contactremindset.IContactRemindSetHome;
import com.chanapp.chanjet.customer.businessobject.api.contactremindset.IContactRemindSetRow;
import com.chanapp.chanjet.customer.businessobject.api.contactremindset.IContactRemindSetRowSet;
import com.chanapp.chanjet.web.service.BoBaseServiceItf;

public interface ContactRemindSetServiceItf
        extends BoBaseServiceItf<IContactRemindSetHome, IContactRemindSetRow, IContactRemindSetRowSet> {
    Map<String, Object> saveSets(String sets);

    ContactRemindSetsValue queryByModifyTime(Long modifyTime);

    Map<String, Object> getDefaultSetValue();
}
