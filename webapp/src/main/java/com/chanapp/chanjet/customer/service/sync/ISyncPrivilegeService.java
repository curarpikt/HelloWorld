package com.chanapp.chanjet.customer.service.sync;

import java.util.List;

import com.chanapp.chanjet.customer.vo.RowSet;
import com.chanjet.csp.common.base.dataauth.DataAuthPrivilege;

public interface ISyncPrivilegeService {
    public void setRowPrivilegeField(List<Long> ids, String entityName, RowSet set);

    List<Long> isAuthorizedList(List<Long> ids, String entityName, DataAuthPrivilege privilege);
}
