package com.chanapp.chanjet.customer.service.sysreluser;

import java.util.Map;

import com.chanapp.chanjet.customer.businessobject.api.sysreluser.ISysRelUserHome;
import com.chanapp.chanjet.customer.businessobject.api.sysreluser.ISysRelUserRow;
import com.chanapp.chanjet.customer.businessobject.api.sysreluser.ISysRelUserRowSet;
import com.chanapp.chanjet.web.service.BoBaseServiceItf;

public interface SysRelUserServiceItf extends BoBaseServiceItf<ISysRelUserHome, ISysRelUserRow, ISysRelUserRowSet> {
    Map<Long,String> getAllUserRoleMap();
}
