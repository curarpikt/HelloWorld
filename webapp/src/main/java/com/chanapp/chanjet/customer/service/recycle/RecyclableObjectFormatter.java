package com.chanapp.chanjet.customer.service.recycle;

import java.util.List;

import com.alibaba.fastjson.JSONObject;

public interface RecyclableObjectFormatter {
	public JSONObject format(List<BatchRecyclableObject> recyclables);
	public JSONObject format(List<BatchRecyclableObject> recyclables,Integer count);
}
