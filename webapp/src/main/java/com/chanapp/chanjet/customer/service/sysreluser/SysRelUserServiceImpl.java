package com.chanapp.chanjet.customer.service.sysreluser;

import java.util.HashMap;
import java.util.Map;

import com.chanapp.chanjet.customer.businessobject.api.sysreluser.ISysRelUserHome;
import com.chanapp.chanjet.customer.businessobject.api.sysreluser.ISysRelUserRow;
import com.chanapp.chanjet.customer.businessobject.api.sysreluser.ISysRelUserRowSet;
import com.chanapp.chanjet.web.service.BoBaseServiceImpl;

public class SysRelUserServiceImpl extends BoBaseServiceImpl<ISysRelUserHome, ISysRelUserRow, ISysRelUserRowSet>
        implements SysRelUserServiceItf {

	@Override
	public Map<Long, String> getAllUserRoleMap() {
		Map<Long,String> retMap = new HashMap<Long,String>();
		ISysRelUserRowSet rowSet = this.queryAll("");
		if(rowSet!=null&&rowSet.getSysRelUserRows()!=null){
			for(ISysRelUserRow row:rowSet.getSysRelUserRows()){
				retMap.put(row.getUserId(), row.getUserRole());
			}			
		}
		return retMap;
	}
	
}
