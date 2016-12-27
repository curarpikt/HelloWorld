package com.chanapp.chanjet.customer.service.workrecordhis;

import com.chanapp.chanjet.customer.businessobject.api.workrecordhis.IWorkRecordHisHome;
import com.chanapp.chanjet.customer.businessobject.api.workrecordhis.IWorkRecordHisRow;
import com.chanapp.chanjet.customer.businessobject.api.workrecordhis.IWorkRecordHisRowSet;
import com.chanapp.chanjet.web.service.BoBaseServiceItf;

public interface WorkRecordHisServiceItf
        extends BoBaseServiceItf<IWorkRecordHisHome, IWorkRecordHisRow, IWorkRecordHisRowSet> {
    void addWorkRecordHis(Long workRecordId);
}
