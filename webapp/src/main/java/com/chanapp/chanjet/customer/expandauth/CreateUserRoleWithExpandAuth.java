package com.chanapp.chanjet.customer.expandauth;

import com.chanapp.chanjet.customer.util.EnterpriseUtil;
import com.chanapp.chanjet.customer.vo.system.UserRole;
import com.chanjet.csp.bo.api.BoSession;
import com.chanjet.csp.enterprise.ext.ExpandAuthorization;

public class CreateUserRoleWithExpandAuth implements ExpandAuthorization {

    @Override
    public void process(Object[] arg0) {

        // TODO 相同类型合并
        UserRole userRole = null;
       	BoSession  session = null;
        if (arg0 != null) {
            userRole = (UserRole) arg0[0]; // 绑定用户角色
            session = (BoSession) arg0[1]; 
        }
        EnterpriseUtil.createUserRole(userRole,session);
    }

}
