package com.chanapp.chanjet.customer.service.idmove;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.chanapp.chanjet.customer.util.DateUtil;
import com.chanapp.chanjet.web.constant.SC;
import com.chanapp.chanjet.web.context.AppContext;
import com.chanapp.chanjet.web.util.QueryLimitUtil;
import com.chanjet.csp.appmanager.AppWorkManager;
import com.chanjet.csp.bo.api.BoDataAccessManager;

public class IDMoveUtil{
    private static  IDMoveUtil instance=new IDMoveUtil();
    private static Long movedTs = null;
    protected static final BoDataAccessManager boDataAccessManager = AppWorkManager.getBoDataAccessManager();

	
	public static IDMoveUtil getInstance(){
		 return instance;
	}
	
	public  Long getLastMovedTS(){
		if(movedTs==null)
		  movedTs=getMovedTS();
		return movedTs;
	}

	private static Long getMovedTS(){		
		String hql = "select MAX("+SC.createdDate+") as createDate from com.chanjet.system.systemapp.businessobject.CspIdMoveLog where entityId like :entityId";
		HashMap<String, Object> paraMap = new HashMap<String, Object>();
		String entityId = "com.chanapp.chanjet.customer.entity%";
		paraMap.put("entityId", entityId);
		List<Map<String, Object>> list =QueryLimitUtil.runCQLQuery(null, AppContext.session(), hql, paraMap);
		if (list != null) {
			int size = list.size();
			for (int i = 0; i < size; i++) {
				Object obj = list.get(i).get("createDate");
				if (obj != null) {			
					String tempTs = obj.toString();
					return DateUtil.getDateTimeByString(tempTs).getTime();
					//movedIds.add(Long.parseLong(obj.toString()));
				}
			}
		}
		return null;	
	}
}