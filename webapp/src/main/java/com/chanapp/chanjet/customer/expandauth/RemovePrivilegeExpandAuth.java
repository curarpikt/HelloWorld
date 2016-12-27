package com.chanapp.chanjet.customer.expandauth;

import java.util.Set;

import com.chanjet.csp.appmanager.AppWorkManager;
import com.chanjet.csp.bo.api.BoSession;
import com.chanjet.csp.common.base.dataauth.Assignment;
import com.chanjet.csp.enterprise.ext.ExpandAuthorization;

public class RemovePrivilegeExpandAuth implements ExpandAuthorization {

    @SuppressWarnings("unchecked")
    @Override
    public void process(Object[] arg0) {
        Set<Assignment> assignments = null;
        BoSession session = null;
        if (arg0 != null) {
            assignments = (Set<Assignment>) arg0[0]; // 要删除的权限
            session = (BoSession) arg0[1];
        }
        if (assignments != null) {
            for (Assignment assignment : assignments) {
                AppWorkManager.getBoDataAccessManager().getDataAuthManagement().removeAssignment(assignment.getId(),
                        session);
            }
        }

    }

}
