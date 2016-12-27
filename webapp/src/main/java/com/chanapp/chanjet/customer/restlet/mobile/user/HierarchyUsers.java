package com.chanapp.chanjet.customer.restlet.mobile.user;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.chanapp.chanjet.customer.service.user.UserServiceItf;
import com.chanapp.chanjet.customer.vo.UserValue;
import com.chanapp.chanjet.web.restlet.BaseRestlet;
import com.chanapp.chanjet.web.service.ServiceLocator;
import com.chanjet.csp.appmanager.AppWorkManager;
import com.chanjet.csp.bo.api.exception.BOApplicationException;

public class HierarchyUsers extends BaseRestlet {

	@Override
	public Object run() {
		if (this.getMethod().equals(MethodEnum.GET)) {
			try {
				return GetHierarchyUsers();
			} catch (Exception e) {
				throw new BOApplicationException(e.getMessage());
			}
		}
		return null;
	}

	private String GetHierarchyUsers() throws IOException {
		Map<String, Object> retMap = new HashMap<String, Object>();
		Long userId = AppWorkManager.getCurrAppUserId();
		List<Map<String,Object>> userList = new ArrayList<Map<String,Object>>();
		List<UserValue> users =ServiceLocator.getInstance().lookup(UserServiceItf.class).getHierarchyUsers(userId);
		for (UserValue user : users) {
			Map<String, Object> userMap = new HashMap<String, Object>();
			userMap.put("name", user.getName());
			userMap.put("id", user.getId());
			userMap.put("headPicture", user.getHeadPicture());
			userMap.put("parentId", user.getParentId());
			userMap.put("fullSpell", user.getFullSpell());
			userMap.put("shortSpell", user.getShortSpell());
			userList.add(userMap);
		}
		retMap.put("items", userList);
		return AppWorkManager.getDataManager().toJSONString(retMap);
	}

}
