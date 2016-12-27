package com.chanapp.chanjet.customer.restlet.v2.web.init.mobile;

import java.util.HashMap;
import java.util.Map;

import com.chanapp.chanjet.web.restlet.BaseRestlet;

public class Test extends BaseRestlet{

	@Override
	public Object run() {
		Map<String, Object> resultMap = new HashMap<String, Object>();
        resultMap.put("success", true);
		return null;
	}

}
